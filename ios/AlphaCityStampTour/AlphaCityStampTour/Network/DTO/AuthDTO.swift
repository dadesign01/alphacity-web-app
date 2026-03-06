//
//  AuthDTO.swift
//  AlphaCityStampTour
//

import Foundation

// MARK: - 요청

struct LoginRequest: Encodable {
    let email: String
    let password: String
}

struct RegisterRequest: Encodable {
    let email: String
    let password: String
    let nickname: String
}

struct SocialLoginRequest: Encodable {
    let provider: String
    let accessToken: String
}

struct RefreshRequest: Encodable {
    let refreshToken: String
}

struct ResetPasswordRequest: Encodable {
    let email: String
    let newPassword: String
}

struct SendCodeRequest: Encodable {
    let phone: String
}

struct VerifyCodeRequest: Encodable {
    let phone: String
    let code: String
}

// MARK: - 응답

struct AuthData: Decodable {
    let token: String
    let refreshToken: String
    let user: UserData
}

struct UserData: Decodable {
    let id: Int
    let email: String
    let nickname: String
    let profileImage: String?
}

struct TokenData: Decodable {
    let token: String
    let refreshToken: String
}

struct EmptyData: Decodable {}
