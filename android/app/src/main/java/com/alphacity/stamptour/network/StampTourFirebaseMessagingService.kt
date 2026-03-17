package com.alphacity.stamptour.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.alphacity.stamptour.MainActivity
import com.alphacity.stamptour.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StampTourFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM", "========== FCM TOKEN ==========")
        android.util.Log.d("FCM", token)
        android.util.Log.d("FCM", "================================")
        // 토큰이 갱신되면 SharedPreferences에 저장 (로그인 상태일 때 서버에 등록)
        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // 로그인 상태이면 서버에도 등록
        val accessToken = prefs.getString("access_token", null)
        if (accessToken != null) {
            FcmTokenManager.registerTokenToServer(applicationContext, token, accessToken)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "default"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "알림", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "스탬프 투어 알림"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
