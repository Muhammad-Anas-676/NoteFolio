package com.anas.notefolio.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

private val ALIAS_MAP = mapOf(
    "default" to "com.anas.notefolio.IconDefault",
    "alt1" to "com.anas.notefolio.IconAlt1",
    "alt2" to "com.anas.notefolio.IconAlt2",
    "alt3" to "com.anas.notefolio.IconAlt3"
)

object IconManager {

    /**
     * Enables the chosen launcher-icon alias and disables the rest.
     * Note: the launcher (home screen) will show the new icon after the
     * next app-drawer refresh — this is normal Android behavior, not a bug.
     */
    fun applyIcon(context: Context, iconKey: String) {
        val pm = context.packageManager
        val target = ALIAS_MAP[iconKey] ?: ALIAS_MAP.getValue("default")

        ALIAS_MAP.forEach { (key, className) ->
            val state = if (className == target) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                ComponentName(context.packageName, className),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
