package com.ianzb.hypernavbar.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.io.File

object RootApplier {

    private const val MARKER_PATH = "/data/system/MiNavBarImmerse"

    suspend fun applyRules(
        jsonContent: String,
        targetPath: String,
        tempDir: File,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(tempDir, "hypernavbar_rules.tmp")
            tempFile.writeText(jsonContent, Charsets.UTF_8)

            val process = Runtime.getRuntime().exec("su")
            val stream = DataOutputStream(process.outputStream)

            val bakPath = "$targetPath.bak"
            stream.writeBytes("if [ ! -f $MARKER_PATH ]; then cp -f $targetPath $bakPath 2>/dev/null; touch $MARKER_PATH; fi\n")

            stream.writeBytes("cp -f ${tempFile.absolutePath} $targetPath\n")
            stream.writeBytes("chmod 600 $targetPath\n")
            stream.writeBytes("chown system:system $targetPath\n")
            stream.writeBytes("rm -f ${tempFile.absolutePath}\n")
            stream.flush()

            stream.writeBytes("cmd miui_navigation_bar_immersive update\n")
            stream.flush()

            stream.writeBytes("exit\n")
            stream.flush()

            process.waitFor()
            val exitCode = process.exitValue()
            process.destroy()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun restoreBackup(targetPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bakPath = "$targetPath.bak"
            val process = Runtime.getRuntime().exec("su")
            val stream = DataOutputStream(process.outputStream)

            stream.writeBytes("if [ -f $bakPath ]; then cp -f $bakPath $targetPath && rm -f $bakPath && rm -f $MARKER_PATH; fi\n")
            stream.writeBytes("chmod 600 $targetPath\n")
            stream.writeBytes("chown system:system $targetPath\n")
            stream.flush()

            stream.writeBytes("cmd miui_navigation_bar_immersive update\n")
            stream.flush()

            stream.writeBytes("exit\n")
            stream.flush()

            process.waitFor()
            val exitCode = process.exitValue()
            process.destroy()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }

    fun isCustomRulesApplied(targetPath: String): Boolean {
        val bakPath = "$targetPath.bak"
        return try {
            val process = Runtime.getRuntime().exec("su")
            val stream = DataOutputStream(process.outputStream)
            stream.writeBytes("test -f $bakPath && echo YES || echo NO\n")
            stream.writeBytes("exit\n")
            stream.flush()
            process.waitFor()
            val reader = process.inputStream.bufferedReader()
            reader.readLine()?.trim() == "YES"
        } catch (_: Exception) {
            false
        }
    }

    fun getCurrentRuleCount(): Int {
        return try {
            val targetPath = RuleConverter.getTargetPath(RuleConverter.detectOsMode())
            val process = Runtime.getRuntime().exec("su")
            val stream = DataOutputStream(process.outputStream)
            stream.writeBytes("cat $targetPath 2>/dev/null | grep -c '\"name\"'\n")
            stream.writeBytes("exit\n")
            stream.flush()
            process.waitFor()
            val reader = process.inputStream.bufferedReader()
            reader.readLine()?.trim()?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
