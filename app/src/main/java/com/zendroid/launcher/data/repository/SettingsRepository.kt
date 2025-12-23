package com.zendroid.launcher.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.zendroid.launcher.ui.intervention.friction.FrictionEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun isProtectionEnabled(): Boolean {
        return prefs.getBoolean(KEY_PROTECTION_ENABLED, true)
    }

    fun setProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }

    fun getFrictionLevel(): FrictionEngine.FrictionLevel {
        val levelStr = prefs.getString(KEY_FRICTION_LEVEL, FrictionEngine.FrictionLevel.HIGH.name)
        return try {
            FrictionEngine.FrictionLevel.valueOf(levelStr!!)
        } catch (e: Exception) {
            FrictionEngine.FrictionLevel.HIGH
        }
    }

    fun setFrictionLevel(level: FrictionEngine.FrictionLevel) {
        prefs.edit().putString(KEY_FRICTION_LEVEL, level.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "zendroid_settings"
        private const val KEY_PROTECTION_ENABLED = "protection_enabled"
        private const val KEY_FRICTION_LEVEL = "friction_level"
    }
}
