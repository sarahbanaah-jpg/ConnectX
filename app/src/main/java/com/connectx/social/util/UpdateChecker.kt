package com.connectx.social.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.connectx.social.BuildConfig
import com.connectx.social.data.model.AppConfig
import com.connectx.social.data.repository.AdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateChecker(
    private val context: Context,
    private val adminRepository: AdminRepository = AdminRepository()
) {
    data class UpdateInfo(
        val available: Boolean,
        val force: Boolean,
        val versionName: String,
        val message: String,
        val downloadUrl: String
    )

    suspend fun check(): UpdateInfo = withContext(Dispatchers.IO) {
        val result = adminRepository.getAppConfig()
        if (result.isFailure) return@withContext UpdateInfo(false, false, "", "", "")
        val config = result.getOrNull() ?: AppConfig()
        val available = config.latestVersionCode > BuildConfig.VERSION_CODE
        UpdateInfo(available, config.forceUpdate && available, config.latestVersionName, config.updateMessage, config.downloadUrl)
    }

    fun openDownload(url: String) {
        if (url.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) { }
    }
}
