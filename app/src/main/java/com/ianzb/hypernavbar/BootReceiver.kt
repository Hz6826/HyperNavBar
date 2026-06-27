package com.ianzb.hypernavbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.ianzb.hypernavbar.rules.RootApplier
import com.ianzb.hypernavbar.rules.RuleCombiner
import com.ianzb.hypernavbar.rules.RuleConverter
import com.ianzb.hypernavbar.rules.RuleFetcher
import com.ianzb.hypernavbar.rules.RulesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!AppSettings.load(context).autoApplyOnBoot) return

        val currentBootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val prefs = context.getSharedPreferences("boot_state", Context.MODE_PRIVATE)
        if (prefs.getLong("last_boot_time", 0L) == currentBootTime) return

        prefs.edit().putLong("last_boot_time", currentBootTime).apply()

        Thread {
            try {
                runBlocking { applyRules(context) }
            } catch (_: Exception) { }
        }.start()
    }

    private suspend fun applyRules(context: Context) = withContext(Dispatchers.IO) {
        val configs = RulesManager.loadAll(context).sortedBy { it.priority }
        if (configs.isEmpty()) return@withContext

        val fetchResults = mutableMapOf<String, RuleFetcher.FetchResult>()
        for (config in configs) {
            RuleFetcher.fetch(config).onSuccess { result ->
                fetchResults[config.id] = result
            }
        }
        if (fetchResults.isEmpty()) return@withContext

        val mergedJson = RuleCombiner.combine(configs, fetchResults)
        val mode = RuleConverter.detectOsMode()
        val targetContent = RuleConverter.convert(mergedJson, mode)
        val targetPath = RuleConverter.getTargetPath(mode)

        if (RootHelper.isRootAvailable || RootHelper.checkRoot()) {
            RootApplier.applyRules(targetContent, targetPath, context.cacheDir)
        }
    }
}
