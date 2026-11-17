package com.connectx.social

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class ConnectXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val defaultChannel = NotificationChannel(
                "connectx_default",
                "ConnectX Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications from ConnectX"
                enableVibration(true)
            }

            val messagesChannel = NotificationChannel(
                "connectx_messages",
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Private message notifications"
                enableVibration(true)
            }

            val updatesChannel = NotificationChannel(
                "connectx_updates",
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Software update notifications"
            }

            manager.createNotificationChannel(defaultChannel)
            manager.createNotificationChannel(messagesChannel)
            manager.createNotificationChannel(updatesChannel)
        }
    }
}
