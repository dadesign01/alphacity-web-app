//
//  AuthRepository.swift
//  AlphaCityStampTour
//

import Foundation

final class AuthRepository {
    static let shared = AuthRepository()
    private let client = APIClient.shared
    private let tokenManager = TokenManager.shared

    private init() {}

    func login(email: String, password: String) async throws -> AuthData {
        let data: AuthData = try await client.request(
            path: "auth/login",
            method: "POST",
            body: LoginRequest(email: email, password: password)
        )
        tokenManager.saveTokens(access: data.token, refresh: data.refreshToken)
        registerFcmToken()
        return data
    }

    func register(email: String, password: String, nickname: String, phone: String? = nil, name: String? = nil) async throws -> AuthData {
        let data: AuthData = try await client.request(
            path: "auth/register",
            method: "POST",
            body: RegisterRequest(email: email, password: password, nickname: nickname, phone: phone, name: name)
        )
        tokenManager.saveTokens(access: data.token, refresh: data.refreshToken)
        registerFcmToken()
        return data
    }

    func socialLogin(provider: String, accessToken: String, nickname: String? = nil) async throws -> AuthData {
        let data: AuthData = try await client.request(
            path: "auth/social",
            method: "POST",
            body: SocialLoginRequest(provider: provider, accessToken: accessToken, nickname: nickname)
        )
        tokenManager.saveTokens(access: data.token, refresh: data.refreshToken)
        registerFcmToken()
        return data
    }

    func logout() {
        FcmTokenManager.shared.clearTokenFromServer()
        tokenManager.clearTokens()
    }

    private func registerFcmToken() {
        if let fcmToken = FcmTokenManager.shared.currentToken {
            FcmTokenManager.shared.registerTokenToServer(fcmToken: fcmToken)
        }
    }

    var isLoggedIn: Bool {
        tokenManager.isLoggedIn
    }
}
