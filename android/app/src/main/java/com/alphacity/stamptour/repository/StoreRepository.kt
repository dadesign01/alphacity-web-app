package com.alphacity.stamptour.repository

import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.network.dto.StoreData
import com.alphacity.stamptour.network.dto.StoreRegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun registerStore(request: StoreRegisterRequest): Result<StoreData> {
        return try {
            val response = apiService.registerStore(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.error?.message ?: "상점 등록 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
