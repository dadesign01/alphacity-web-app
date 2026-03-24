//
//  ForgotPasswordViewModel.swift
//  AlphaCityStampTour
//

import Foundation

enum ForgotPasswordStep {
    case inputInfo     // 이메일 + 전화번호 입력
    case verifyCode    // 인증코드 입력
    case newPassword   // 새 비밀번호 설정
}

@MainActor
final class ForgotPasswordViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var isSuccess = false
    @Published var step: ForgotPasswordStep = .inputInfo

    private let client = APIClient.shared
    private var savedEmail = ""

    func sendCode(email: String, phone: String) {
        guard !email.isEmpty else {
            error = "이메일을 입력하세요"
            return
        }
        guard !phone.isEmpty else {
            error = "전화번호를 입력하세요"
            return
        }

        savedEmail = email
        isLoading = true
        error = nil

        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/send-code",
                    method: "POST",
                    body: ["email": email, "phone": phone]
                )
                step = .verifyCode
            } catch {
                self.error = "인증코드 발송에 실패했습니다"
            }
            isLoading = false
        }
    }

    func verifyCode(_ code: String) {
        guard code.count == 6 else {
            error = "6자리 인증코드를 입력하세요"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/verify-code",
                    method: "POST",
                    body: ["email": savedEmail, "code": code]
                )
                step = .newPassword
            } catch {
                self.error = "인증코드가 올바르지 않습니다"
            }
            isLoading = false
        }
    }

    func resetPassword(code: String, newPassword: String, confirmPassword: String) {
        guard newPassword.count >= 8 else {
            error = "비밀번호는 8자 이상이어야 합니다"
            return
        }
        guard newPassword == confirmPassword else {
            error = "비밀번호가 일치하지 않습니다"
            return
        }

        isLoading = true
        error = nil

        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/reset-password",
                    method: "POST",
                    body: ["email": savedEmail, "code": code, "newPassword": newPassword]
                )
                isSuccess = true
            } catch {
                self.error = "비밀번호 변경에 실패했습니다"
            }
            isLoading = false
        }
    }

    func clearError() {
        error = nil
    }
}
