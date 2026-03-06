//
//  ForgotPasswordViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class ForgotPasswordViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var isSuccess = false

    private let client = APIClient.shared

    func resetPassword(email: String, newPassword: String, confirmPassword: String) {
        guard !email.isEmpty else {
            error = "이메일을 입력하세요"
            return
        }
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
                    body: ResetPasswordRequest(email: email, newPassword: newPassword)
                )
                isSuccess = true
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
