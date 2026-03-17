package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.network.dto.AuthData
import com.alphacity.stamptour.network.dto.LoginRequest
import com.alphacity.stamptour.network.dto.RegisterRequest
import com.alphacity.stamptour.network.dto.ResetPasswordRequest
import com.alphacity.stamptour.network.dto.SendCodeRequest
import com.alphacity.stamptour.network.dto.SocialLoginRequest
import com.alphacity.stamptour.network.dto.VerifyCodeRequest
import android.content.Context
import com.alphacity.stamptour.network.FcmTokenManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context,
) {
    private fun registerFcmToken(accessToken: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            FcmTokenManager.registerTokenToServer(context, fcmToken, accessToken)
        }
    }

    suspend fun login(email: String, password: String): Result<AuthData> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.token, response.data.refreshToken)
                registerFcmToken(response.data.token)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "로그인에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, nickname: String): Result<AuthData> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, nickname))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.token, response.data.refreshToken)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "회원가입에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun socialLogin(provider: String, accessToken: String): Result<AuthData> {
        return try {
            android.util.Log.d("AuthRepository", "소셜 로그인 요청: provider=$provider")
            val response = apiService.socialLogin(SocialLoginRequest(provider, accessToken))
            if (response.success && response.data != null) {
                tokenManager.saveTokens(response.data.token, response.data.refreshToken)
                registerFcmToken(response.data.token)
                android.util.Log.d("AuthRepository", "소셜 로그인 성공: ${response.data.user}")
                Result.success(response.data)
            } else {
                android.util.Log.e("AuthRepository", "소셜 로그인 실패: ${response.error?.message}")
                Result.failure(Exception(response.error?.message ?: "소셜 로그인에 실패했습니다"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "소셜 로그인 예외", e)
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, newPassword))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error?.message ?: "비밀번호 변경에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendCode(phone: String): Result<Unit> {
        return try {
            val response = apiService.sendCode(SendCodeRequest(phone))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error?.message ?: "SMS 전송에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyCode(phone: String, code: String): Result<Unit> {
        return try {
            val response = apiService.verifyCode(VerifyCodeRequest(phone, code))
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error?.message ?: "인증번호가 올바르지 않습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        val accessToken = tokenManager.accessToken
        if (accessToken != null) {
            FcmTokenManager.clearTokenFromServer(context, accessToken)
        }
        tokenManager.clearTokens()
    }

    val isLoggedIn: Boolean
        get() = tokenManager.isLoggedIn
}
