package com.example.simplebutton.remote

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Manages remote configuration, APK update detection with weekly cooldown,
 * and custom in-app notifications.
 *
 * Operates purely without Google Play Services or Play Store APIs.
 */
object RemoteConfigManager {

    private const val PREFS_NAME = "lornshill_remote_config_prefs"

    // SharedPreferences Keys
    private const val KEY_CONFIG_URL = "remote_config_url"
    private const val KEY_LAST_UPDATE_SHOWN_VERSION = "last_update_shown_version"
    private const val KEY_LAST_UPDATE_SHOWN_TIMESTAMP = "last_update_shown_timestamp"
    private const val KEY_DISMISSED_NOTIFICATIONS = "dismissed_notification_ids"
    private const val KEY_CACHED_CONFIG_JSON = "cached_remote_config_json"
    private const val KEY_LAST_FETCH_TIME = "last_fetch_timestamp"

    // 7 days weekly cooldown in milliseconds
    const val COOLDOWN_7_DAYS_MILLIS = 7L * 24L * 60L * 60L * 1000L

    // Default remote configuration URL (hosted on your GitHub repository)
    const val DEFAULT_CONFIG_URL = "https://raw.githubusercontent.com/Tullibody/lornshill-timetable/main/app-config.json"

    // Observable Compose States
    val isChecking = mutableStateOf(false)
    val lastCheckStatus = mutableStateOf("Not checked yet")
    val lastCheckTimestamp = mutableStateOf<Long?>(null)
    val latestRemoteConfig = mutableStateOf<RemoteConfigData?>(null)
    val activeUpdateInfo = mutableStateOf<AppUpdateInfo?>(null)
    val activeNotification = mutableStateOf<RemoteAppNotification?>(null)
    val isUpdateAvailable = mutableStateOf(false)
    val isUpdatePromptDue = mutableStateOf(false)

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Gets the configured remote JSON URL, or default if not set.
     */
    fun getConfigUrl(context: Context): String {
        return getPrefs(context).getString(KEY_CONFIG_URL, DEFAULT_CONFIG_URL)?.trim()
            ?.ifBlank { DEFAULT_CONFIG_URL } ?: DEFAULT_CONFIG_URL
    }

    /**
     * Sets a custom remote JSON URL (e.g. from the Dev Panel).
     */
    fun setConfigUrl(context: Context, url: String) {
        val clean = url.trim().ifBlank { DEFAULT_CONFIG_URL }
        getPrefs(context).edit().putString(KEY_CONFIG_URL, clean).apply()
    }

    /**
     * Resets the remote JSON URL to the default value.
     */
    fun resetConfigUrl(context: Context) {
        getPrefs(context).edit().remove(KEY_CONFIG_URL).apply()
    }

    /**
     * Returns the currently installed APK's versionCode.
     */
    fun getInstalledVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (_: Exception) {
            1L
        }
    }

    /**
     * Returns the currently installed APK's versionName.
     */
    fun getInstalledVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.5"
        } catch (_: Exception) {
            "0.5"
        }
    }

    /**
     * Returns the version code for which the update prompt was last shown.
     */
    fun getLastShownVersionCode(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_UPDATE_SHOWN_VERSION, 0L)
    }

    /**
     * Returns the timestamp (epoch ms) when the update prompt was last shown.
     */
    fun getLastShownTimestamp(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_UPDATE_SHOWN_TIMESTAMP, 0L)
    }

    /**
     * Records that an update prompt was displayed for the given version code.
     */
    fun recordUpdatePromptShown(context: Context, versionCode: Long) {
        getPrefs(context).edit()
            .putLong(KEY_LAST_UPDATE_SHOWN_VERSION, versionCode)
            .putLong(KEY_LAST_UPDATE_SHOWN_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Resets the weekly cooldown so the update popup can be tested immediately in Dev Panel.
     */
    fun resetUpdateCooldown(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_LAST_UPDATE_SHOWN_VERSION)
            .remove(KEY_LAST_UPDATE_SHOWN_TIMESTAMP)
            .apply()
    }

    /**
     * Returns the set of notification IDs that have been dismissed by the user.
     */
    fun getDismissedNotificationIds(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_DISMISSED_NOTIFICATIONS, emptySet())?.toSet() ?: emptySet()
    }

    /**
     * Marks a notification as dismissed so it will never be displayed again.
     */
    fun dismissNotification(context: Context, notificationId: String) {
        val current = getDismissedNotificationIds(context).toMutableSet()
        current.add(notificationId)
        getPrefs(context).edit().putStringSet(KEY_DISMISSED_NOTIFICATIONS, current).apply()

        // If this notification was the active one, clear it
        if (activeNotification.value?.id == notificationId) {
            activeNotification.value = null
        }
    }

    /**
     * Clears all dismissed notification IDs (useful for Dev Panel QA testing).
     */
    fun clearDismissedNotifications(context: Context) {
        getPrefs(context).edit().remove(KEY_DISMISSED_NOTIFICATIONS).apply()
    }

    /**
     * Checks whether an update prompt is due to be shown.
     *
     * Rules:
     * 1. installedVersionCode < latestVersionCode
     * 2. If a brand new version is released (latestVersionCode != lastShownVersion), show immediately.
     * 3. If the same version was already shown, enforce 7-day cooldown.
     */
    fun shouldShowUpdatePrompt(
        installedVersionCode: Long,
        latestVersionCode: Long,
        lastShownVersion: Long,
        lastShownTimestamp: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (installedVersionCode >= latestVersionCode) {
            return false
        }

        // New version released: bypass any previous cooldown
        if (latestVersionCode != lastShownVersion) {
            return true
        }

        // Same version previously shown: check if 7 days (1 week) have elapsed
        val elapsed = currentTimeMillis - lastShownTimestamp
        return elapsed >= COOLDOWN_7_DAYS_MILLIS
    }

    /**
     * Parses an ISO 8601 timestamp string into an Instant.
     * Supports UTC (Z), timezone offsets, and simple dates.
     */
    fun parseIsoInstant(dateStr: String): Instant? {
        val trimmed = dateStr.trim()
        if (trimmed.isBlank()) return null
        return try {
            Instant.parse(trimmed)
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(trimmed).toInstant()
            } catch (_: Exception) {
                try {
                    ZonedDateTime.parse(trimmed).toInstant()
                } catch (_: Exception) {
                    try {
                        LocalDate.parse(trimmed).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }
    }

    /**
     * Checks if a notification has expired based on its optional expiresAt field.
     */
    fun isNotificationExpired(expiresAt: String?, now: Instant = Instant.now()): Boolean {
        if (expiresAt.isNullOrBlank()) return false
        val expiry = parseIsoInstant(expiresAt) ?: return false
        return now.isAfter(expiry)
    }

    /**
     * Parses the JSON configuration string safely into [RemoteConfigData].
     */
    fun parseConfigJson(jsonString: String): RemoteConfigData {
        val root = JSONObject(jsonString)

        // Parse update info if present
        val updateInfo = if (root.has("latestVersionCode")) {
            AppUpdateInfo(
                latestVersionCode = root.optLong("latestVersionCode", 0L),
                latestVersionName = root.optString("latestVersionName", ""),
                apkUrl = root.optString("apkUrl", ""),
                whatsNew = root.optString("whatsNew", "")
            )
        } else null

        // Parse notifications array if present
        val notifications = mutableListOf<RemoteAppNotification>()
        val array = root.optJSONArray("notifications")
        if (array != null) {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                if (id.isBlank()) continue

                notifications.add(
                    RemoteAppNotification(
                        id = id,
                        enabled = item.optBoolean("enabled", true),
                        title = item.optString("title", "Notice"),
                        message = item.optString("message", ""),
                        buttonText = item.optString("buttonText", "Close").ifBlank { "Close" },
                        actionUrl = item.optString("actionUrl", "").takeIf { it.isNotBlank() },
                        actionText = item.optString("actionText", "").takeIf { it.isNotBlank() },
                        expiresAt = item.optString("expiresAt", "").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        return RemoteConfigData(
            updateInfo = updateInfo,
            notifications = notifications,
            rawJson = jsonString
        )
    }

    /**
     * Fetches raw JSON text from a URL via HttpURLConnection with redirect following.
     */
    suspend fun fetchJsonText(urlStr: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = urlStr
            var redirectCount = 0
            val maxRedirects = 5

            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    instanceFollowRedirects = false
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 LornshillTimetableAndroidApp/1.0 (Mobile)"
                    )
                    setRequestProperty("Accept", "application/json,text/plain,*/*")
                }

                val responseCode = connection.responseCode

                // Handle HTTP redirects (301, 302, 307, 308)
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: return@withContext Result.failure(Exception("Redirect without Location header (HTTP $responseCode)"))
                    currentUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                    redirectCount++
                    connection.disconnect()
                    continue
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        Exception("Server returned HTTP $responseCode")
                    )
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val content = reader.use { it.readText() }
                connection.disconnect()
                return@withContext Result.success(content)
            }

            Result.failure(Exception("Too many redirects"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error connecting to remote configuration"))
        }
    }

    /**
     * Performs a complete fetch, parse, and evaluation of updates and in-app notifications.
     * Designed to be called safely on app startup or manually from Dev Panel.
     * Never crashes and never prevents the app from functioning.
     */
    suspend fun fetchAndCheck(context: Context): RemoteConfigFetchResult {
        isChecking.value = true
        val url = getConfigUrl(context)

        val fetchResult = fetchJsonText(url)

        return fetchResult.fold(
            onSuccess = { jsonText ->
                try {
                    val config = parseConfigJson(jsonText)
                    latestRemoteConfig.value = config

                    // Cache the successful JSON
                    getPrefs(context).edit()
                        .putString(KEY_CACHED_CONFIG_JSON, jsonText)
                        .putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis())
                        .apply()

                    evaluateConfig(context, config)

                    lastCheckStatus.value = "Success"
                    lastCheckTimestamp.value = System.currentTimeMillis()
                    isChecking.value = false
                    RemoteConfigFetchResult.Success(config)
                } catch (e: Exception) {
                    val errorMsg = "JSON Parse Error: ${e.message}"
                    lastCheckStatus.value = errorMsg
                    isChecking.value = false
                    RemoteConfigFetchResult.Error(errorMsg)
                }
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Failed to connect"
                lastCheckStatus.value = errorMsg
                isChecking.value = false

                // Try to use cached configuration if network is offline
                val cached = getPrefs(context).getString(KEY_CACHED_CONFIG_JSON, null)
                if (!cached.isNullOrBlank()) {
                    try {
                        val config = parseConfigJson(cached)
                        latestRemoteConfig.value = config
                        evaluateConfig(context, config)
                    } catch (_: Exception) {
                        // Ignore cache parse failure
                    }
                }

                RemoteConfigFetchResult.Error(errorMsg)
            }
        )
    }

    /**
     * Evaluates a parsed configuration against the local device state.
     */
    fun evaluateConfig(context: Context, config: RemoteConfigData) {
        val installedCode = getInstalledVersionCode(context)
        val update = config.updateInfo

        if (update != null) {
            val available = installedCode < update.latestVersionCode
            isUpdateAvailable.value = available

            val lastShownVer = getLastShownVersionCode(context)
            val lastShownTime = getLastShownTimestamp(context)
            val due = shouldShowUpdatePrompt(
                installedVersionCode = installedCode,
                latestVersionCode = update.latestVersionCode,
                lastShownVersion = lastShownVer,
                lastShownTimestamp = lastShownTime
            )
            isUpdatePromptDue.value = due

            if (available && due) {
                activeUpdateInfo.value = update
            } else {
                activeUpdateInfo.value = null
            }
        } else {
            isUpdateAvailable.value = false
            isUpdatePromptDue.value = false
            activeUpdateInfo.value = null
        }

        // Evaluate in-app notifications
        val dismissed = getDismissedNotificationIds(context)
        val candidate = config.notifications.firstOrNull { notif ->
            notif.enabled && notif.id !in dismissed && !isNotificationExpired(notif.expiresAt)
        }

        activeNotification.value = candidate
    }

    /**
     * Launches the user's web browser / download manager to download the APK.
     * Safely catches any ActivityNotFoundException and notifies the user.
     */
    fun openApkDownload(context: Context, apkUrl: String): Boolean {
        val cleanUrl = apkUrl.trim()
        if (cleanUrl.isBlank()) {
            Toast.makeText(context, "No APK download URL was provided.", Toast.LENGTH_SHORT).show()
            return false
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No web browser found to open the download link.",
                Toast.LENGTH_LONG
            ).show()
            false
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open download link: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
