//
//  MyPageViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MyPageViewModel: ObservableObject {
    @Published var userProfile: UserProfileData?
    @Published var isLoading = false
    @Published var saveSuccess = false
    @Published var saveError: String?
    @Published var deleteSuccess = false
    @Published var deleteError: String?
    @Published var isCodeSent = false
    @Published var isPhoneVerified = false
    @Published var verificationError: String?

    private let repository = HomeRepository.shared
    private let tokenManager = TokenManager.shared
    private let client = APIClient.shared

    var isLoggedIn: Bool {
        tokenManager.isLoggedIn
    }

    var isSocialLogin: Bool {
        userProfile?.provider != nil
    }

    func fetchProfile() async {
        guard tokenManager.isLoggedIn else { return }
        isLoading = true
        do {
            userProfile = try await repository.fetchUserProfile()
        } catch {
            print("MyPageViewModel: 프로필 로드 실패 - \(error)")
        }
        isLoading = false
    }

    func sendCode(phone: String) {
        guard !phone.isEmpty else {
            verificationError = "휴대폰 번호를 입력하세요"
            return
        }
        isLoading = true
        verificationError = nil
        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/send-code",
                    method: "POST",
                    body: SendCodeRequest(phone: phone)
                )
                isCodeSent = true
            } catch {
                verificationError = error.localizedDescription
            }
            isLoading = false
        }
    }

    func verifyCode(phone: String, code: String) {
        guard !code.isEmpty else {
            verificationError = "인증번호를 입력하세요"
            return
        }
        isLoading = true
        verificationError = nil
        Task {
            do {
                let _: EmptyData? = try await client.request(
                    path: "auth/verify-code",
                    method: "POST",
                    body: VerifyCodeRequest(phone: phone, code: code)
                )
                isPhoneVerified = true
            } catch {
                verificationError = error.localizedDescription
            }
            isLoading = false
        }
    }

    func setPhoneVerifiedFromProfile() {
        isPhoneVerified = true
    }

    func resetVerificationState() {
        isCodeSent = false
        isPhoneVerified = false
        verificationError = nil
    }

    func updateProfile(nickname: String, currentPassword: String?, newPassword: String?, phone: String? = nil, name: String? = nil, address: String? = nil, addressDetail: String? = nil, birthDate: String? = nil, gender: String? = nil) async {
        isLoading = true
        saveError = nil
        do {
            let updated = try await repository.updateProfile(nickname: nickname, currentPassword: currentPassword, newPassword: newPassword, phone: phone, name: name, address: address, addressDetail: addressDetail, birthDate: birthDate, gender: gender)
            userProfile = updated
            saveSuccess = true
        } catch {
            saveError = error.localizedDescription
        }
        isLoading = false
    }

    func deleteAccount() async {
        isLoading = true
        deleteError = nil
        do {
            try await repository.deleteAccount()
            tokenManager.clearTokens()
            userProfile = nil
            deleteSuccess = true
        } catch {
            deleteError = error.localizedDescription
        }
        isLoading = false
    }

    func clearSaveState() {
        saveSuccess = false
        saveError = nil
    }

    func clearDeleteState() {
        deleteSuccess = false
        deleteError = nil
    }

    func logout() {
        tokenManager.clearTokens()
        userProfile = nil
    }
}
