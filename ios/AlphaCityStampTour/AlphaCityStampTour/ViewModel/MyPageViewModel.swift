//
//  MyPageViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MyPageViewModel: ObservableObject {
    @Published var userProfile: UserProfileData?
    @Published var isLoading = false

    private let repository = HomeRepository.shared
    private let tokenManager = TokenManager.shared

    var isLoggedIn: Bool {
        tokenManager.isLoggedIn
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

    func logout() {
        tokenManager.clearTokens()
        userProfile = nil
    }
}
