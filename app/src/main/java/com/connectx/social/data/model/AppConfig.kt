package com.connectx.social.data.model

data class AppConfig(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val forceUpdate: Boolean = false,
    val updateMessage: String = "A new version of ConnectX is available.",
    val downloadUrl: String = "",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = ""
) {
    companion object {
        fun fromMap(map: Map<String, Any?>): AppConfig = AppConfig(
            latestVersionCode = (map["latestVersionCode"] as? Long)?.toInt() ?: 1,
            latestVersionName = map["latestVersionName"] as? String ?: "1.0.0",
            forceUpdate = map["forceUpdate"] as? Boolean ?: false,
            updateMessage = map["updateMessage"] as? String ?: "A new version of ConnectX is available.",
            downloadUrl = map["downloadUrl"] as? String ?: "",
            maintenanceMode = map["maintenanceMode"] as? Boolean ?: false,
            maintenanceMessage = map["maintenanceMessage"] as? String ?: ""
        )
    }
}

data class SecurityFlag(
    val id: String = "",
    val type: String = "",
    val targetId: String = "",
    val reason: String = "",
    val severity: String = "medium",
    val reportedBy: String = "ai_assistant",
    val createdAt: com.google.firebase.Timestamp? = null,
    val resolved: Boolean = false,
    val resolvedBy: String = ""
)
