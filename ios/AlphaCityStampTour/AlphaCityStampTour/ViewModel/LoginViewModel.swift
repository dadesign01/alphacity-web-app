//
//  LoginViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import KakaoSDKUser
import KakaoSDKAuth
import NidThirdPartyLogin

@MainActor
final class LoginViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var isLoggedIn = false
    @Published var user: UserData?
    @Published var failureCount = 0
    @Published var showForgotPasswordHint = false

    private let authRepository = AuthRepository.shared

    // MARK: - 이메일 로그인

    func login(email: String, password: String) {
        guard !email.isEmpty, !password.isEmpty else {
            error = "이메일과 비밀번호를 입력하세요"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let data = try await authRepository.login(email: email, password: password)
                isLoggedIn = true
                user = data.user
            } catch {
                self.error = error.localizedDescription
                failureCount += 1
                if failureCount >= 3 {
                    showForgotPasswordHint = true
                }
            }
            isLoading = false
        }
    }

    // MARK: - 소셜 로그인 (서버로 토큰 전달)

    func socialLogin(provider: String, accessToken: String) {
        isLoading = true
        error = nil

        Task {
            do {
                let data = try await authRepository.socialLogin(provider: provider, accessToken: accessToken)
                isLoggedIn = true
                user = data.user
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    // MARK: - 카카오 로그인

    func loginWithKakao() {
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk { [weak self] oauthToken, _ in
                guard let token = oauthToken?.accessToken else { return }
                self?.socialLogin(provider: "kakao", accessToken: token)
            }
        } else {
            UserApi.shared.loginWithKakaoAccount { [weak self] oauthToken, _ in
                guard let token = oauthToken?.accessToken else { return }
                self?.socialLogin(provider: "kakao", accessToken: token)
            }
        }
    }

    // MARK: - 네이버 로그인

    func loginWithNaver() {
        NidOAuth.shared.requestLogin(callback: { [weak self] result in
            if case .success(let token) = result {
                self?.socialLogin(provider: "naver", accessToken: "\(token.accessToken)")
            }
        })
    }

    func clearError() {
        error = nil
    }
}
