//
//  RegisterViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class RegisterViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var isRegistered = false
    @Published var user: UserData?
    @Published var isCodeSent = false
    @Published var isPhoneVerified = false

    private let authRepository = AuthRepository.shared
    private let client = APIClient.shared

    // MARK: - 인증번호 발송

    func sendCode(phone: String) {
        guard !phone.isEmpty else {
            error = "휴대폰 번호를 입력하세요"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/send-code",
                    method: "POST",
                    body: SendCodeRequest(phone: phone)
                )
                isCodeSent = true
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    // MARK: - 인증번호 확인

    func verifyCode(phone: String, code: String) {
        guard !code.isEmpty else {
            error = "인증번호를 입력하세요"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/verify-code",
                    method: "POST",
                    body: VerifyCodeRequest(phone: phone, code: code)
                )
                isPhoneVerified = true
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    // MARK: - 회원가입

    func register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        guard !name.isEmpty else {
            error = "이름을 입력하세요"
            return
        }
        guard !email.isEmpty else {
            error = "이메일을 입력하세요"
            return
        }
        guard isPhoneVerified else {
            error = "휴대폰 인증을 완료하세요"
            return
        }
        guard password.count >= 8 else {
            error = "비밀번호는 8자 이상이어야 합니다"
            return
        }
        guard password == confirmPassword else {
            error = "비밀번호가 일치하지 않습니다"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let data = try await authRepository.register(email: email, password: password, nickname: name, phone: phone, name: name)
                isRegistered = true
                user = data.user
            } catch {
                self.error = error.localizedDescription
            }
            isLoading = false
        }
    }

    func clearError() {
        error = nil
    }
}
