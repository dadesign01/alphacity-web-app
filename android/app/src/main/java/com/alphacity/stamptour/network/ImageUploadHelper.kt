package com.alphacity.stamptour.network

import android.content.Context
import android.net.Uri
import com.alphacity.stamptour.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * 이미지를 서버 /upload 엔드포인트에 multipart/form-data 로 업로드하고
 * 저장된 이미지 URL을 반환한다. 실패 시 null.
 */
suspend fun uploadImageToServer(context: Context, uri: Uri, filePrefix: String = "image"): String? {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val boundary = "----${UUID.randomUUID()}"
            val url = URL("${BuildConfig.API_BASE_URL.removeSuffix("/api/v1")}/api/v1/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.doOutput = true

            val fileName = "${filePrefix}_${System.currentTimeMillis()}.jpg"
            conn.outputStream.use { os ->
                os.write("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray())
                os.write(bytes)
                os.write("\r\n--$boundary--\r\n".toByteArray())
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                Regex("\"imageUrl\"\\s*:\\s*\"([^\"]+)\"").find(response)?.groupValues?.get(1)
            } else null
        } catch (e: Exception) {
            android.util.Log.e("ImageUploader", "Image upload failed", e)
            null
        }
    }
}
