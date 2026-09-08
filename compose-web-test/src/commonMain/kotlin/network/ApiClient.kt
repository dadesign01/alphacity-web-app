package com.alphacity.stamptour.network

import com.alphacity.stamptour.web.WebTokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiClient {

    private const val BASE_URL = "http://192.168.0.12:1111/api/v1/"

    val client = HttpClient(Js) {

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        defaultRequest {
            url(BASE_URL)

            WebTokenManager.getAccessToken()?.let { token ->
                header(
                    HttpHeaders.Authorization,
                    "Bearer $token"
                )
            }
        }
    }
}