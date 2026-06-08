//
//  LoginViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import KakaoSDKCommon
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
            UserApi.shared.loginWithKakaoTalk { [weak self] oauthToken, error in
                if let error = error {
                    // 사용자가 직접 취소한 경우는 중단
                    if let sdkError = error as? SdkError, case .ClientFailed(let reason, _) = sdkError, reason == .Cancelled {
                        return
                    }
                    // 카카오톡 로그인 실패(계정 미연결 등) → 카카오계정 로그인으로 전환
                    self?.loginWithKakaoAccount()
                    return
                }
                guard let token = oauthToken?.accessToken else { return }
                self?.socialLogin(provider: "kakao", accessToken: token)
            }
        } else {
            loginWithKakaoAccount()
        }
    }

    private func loginWithKakaoAccount() {
        UserApi.shared.loginWithKakaoAccount { [weak self] oauthToken, error in
            if error != nil {
                self?.error = "카카오 로그인에 실패했습니다. 다시 시도해 주세요."
                return
            }
            guard let token = oauthToken?.accessToken else { return }
            self?.socialLogin(provider: "kakao", accessToken: token)
        }
    }

    // MARK: - 네이버 로그인

    func loginWithNaver() {
        NidOAuth.shared.requestLogin(callback: { [weak self] result in
            if case .success(let token) = result {
                self?.socialLogin(provider: "naver", accessToken: token.accessToken.tokenString)
            }
        })
    }

    func clearError() {
        error = nil
    }
}
