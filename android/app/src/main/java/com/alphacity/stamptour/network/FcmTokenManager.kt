package com.alphacity.stamptour.network

import android.content.Context
import com.alphacity.stamptour.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

object FcmTokenManager {

    fun registerTokenToServer(context: Context, fcmToken: String, accessToken: String) {
        android.util.Log.d("FCM", "========== REGISTERING FCM TOKEN ==========")
        android.util.Log.d("FCM", "Token: $fcmToken")
        android.util.Log.d("FCM", "URL: ${BuildConfig.API_BASE_URL}/users/fcm-token")
        android.util.Log.d("FCM", "============================================")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.API_BASE_URL}/users/fcm-token")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.doOutput = true

                val body = """{"fcmToken":"$fcmToken"}"""
                conn.outputStream.use { it.write(body.toByteArray()) }

                val code = conn.responseCode
                android.util.Log.d("FCM", "FCM token register response: $code")
                conn.disconnect()
            } catch (e: Exception) {
                android.util.Log.e("FcmTokenManager", "FCM token registration failed", e)
            }
        }
    }

    fun clearTokenFromServer(context: Context, accessToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("${BuildConfig.API_BASE_URL}/users/fcm-token")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "DELETE"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                android.util.Log.e("FcmTokenManager", "FCM token clear failed", e)
            }
        }
    }
}
