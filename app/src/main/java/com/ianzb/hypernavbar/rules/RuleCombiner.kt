package com.ianzb.hypernavbar.rules

import org.json.JSONObject

object RuleCombiner {

    fun combine(configs: List<RuleConfigSource>, fetchResults: Map<String, RuleFetcher.FetchResult>): JSONObject {
        val sortedConfigs = configs.sortedByDescending { it.priority }

        val mergedNBIRules = JSONObject()
        val mergedRoot = JSONObject()

        for (config in sortedConfigs) {
            val result = fetchResults[config.id] ?: continue
            val nbiRules = result.nbiRules

            val keys = nbiRules.keys()
            while (keys.hasNext()) {
                val pkg = keys.next()
                val appRule = nbiRules.getJSONObject(pkg)

                if (mergedNBIRules.has(pkg)) {
                    val existing = mergedNBIRules.getJSONObject(pkg)
                    mergeAppRule(existing, appRule)
                } else {
                    mergedNBIRules.put(pkg, JSONObject(appRule.toString()))
                }
            }
        }

        val firstResult = sortedConfigs.firstNotNullOfOrNull { config ->
            fetchResults[config.id]
        }

        val rootJson = JSONObject(firstResult?.rawJson ?: "{}")
        mergedRoot.put("dataVersion", rootJson.optString("dataVersion", "999999"))
        mergedRoot.put("name", rootJson.optString("name", "沉浸规则"))
        mergedRoot.put("modules", rootJson.optString("modules", "navigation_bar_immersive_application_config_new"))
        mergedRoot.put("modifyApps", rootJson.optString("modifyApps", "modifyApps"))

        val sortedNBIRules = JSONObject()
        val keys = sortedSetOf<String>().also { set ->
            val iter = mergedNBIRules.keys()
            while (iter.hasNext()) set.add(iter.next())
        }
        for (key in keys) {
            sortedNBIRules.put(key, mergedNBIRules.get(key))
        }
        mergedRoot.put("NBIRules", sortedNBIRules)

        return mergedRoot
    }

    private fun mergeAppRule(existing: JSONObject, newRule: JSONObject) {
        val existingActivities = existing.optJSONObject("activityRules")
        val newActivities = newRule.optJSONObject("activityRules")

        if (existingActivities != null && newActivities != null) {
            val newKeys = newActivities.keys()
            while (newKeys.hasNext()) {
                val activity = newKeys.next()
                existingActivities.put(activity, newActivities.get(activity))
            }
        } else if (newActivities != null) {
            existing.put("activityRules", JSONObject(newActivities.toString()))
        }

        if (newRule.has("enable")) {
            existing.put("enable", newRule.getBoolean("enable"))
        }
        if (newRule.has("name") && newRule.getString("name").isNotEmpty()) {
            existing.put("name", newRule.getString("name"))
        }
    }

    fun getTotalAppCount(fetchResults: Map<String, RuleFetcher.FetchResult>): Int {
        val allPackages = mutableSetOf<String>()
        for ((_, result) in fetchResults) {
            val keys = result.nbiRules.keys()
            while (keys.hasNext()) {
                allPackages.add(keys.next())
            }
        }
        return allPackages.size
    }
}
