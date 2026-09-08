package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.AuthData
import com.alphacity.stamptour.web.WebTokenManager

class AuthRepository {

    suspend fun socialLogin(
        provider: String,
        accessToken: String? = null,
        code: String? = null,
    ): Result<AuthData> {
        return try {
            println(
                "[AuthRepository] 소셜 로그인 요청: provider=$provider"
            )

            val response = ApiService.socialLogin(
                provider = provider,
                accessToken = accessToken,
                code = code,
            )

            if (response.success && response.data != null) {
                val data = response.data

                WebTokenManager.saveTokens(
                    accessToken = data.token,
                    refreshToken = data.refreshToken,
                )

                println(
                    "[AuthRepository] 소셜 로그인 성공: provider=$provider"
                )

                Result.success(data)
            } else {
                Result.failure(
                    Exception(
                        response.error?.message
                            ?: "소셜 로그인에 실패했습니다"
                    )
                )
            }
        } catch (e: Exception) {
            println(
                "[AuthRepository] 소셜 로그인 예외: ${e.message}"
            )

            Result.failure(e)
        }
    }
}