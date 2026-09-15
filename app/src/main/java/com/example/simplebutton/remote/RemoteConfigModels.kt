package com.example.simplebutton.remote

/**
 * Information about the latest APK release parsed from remote JSON.
 */
data class AppUpdateInfo(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val apkUrl: String,
    val whatsNew: String
)

/**
 * Information about a remote in-app announcement/notice parsed from remote JSON.
 */
data class RemoteAppNotification(
    val id: String,
    val enabled: Boolean = true,
    val title: String,
    val message: String,
    val buttonText: String = "Close",
    val actionUrl: String? = null,
    val actionText: String? = null,
    val expiresAt: String? = null
)

/**
 * Combined container holding both update metadata and custom notifications.
 */
data class RemoteConfigData(
    val updateInfo: AppUpdateInfo?,
    val notifications: List<RemoteAppNotification> = emptyList(),
    val rawJson: String = ""
)

/**
 * Result of a remote config fetch operation.
 */
sealed class RemoteConfigFetchResult {
    data class Success(val config: RemoteConfigData) : RemoteConfigFetchResult()
    data class Error(val message: String) : RemoteConfigFetchResult()
}
