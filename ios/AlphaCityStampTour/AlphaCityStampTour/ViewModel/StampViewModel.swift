//
//  StampViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class StampViewModel: ObservableObject {
    @Published var stamps: [StampData] = []
    @Published var totalStampCount: Int = 0
    @Published var userStampCount: Int = 0
    @Published var collectedStampIds: Set<Int> = []
    @Published var isLoading = false

    private let repository = StampRepository.shared

    var progress: Float {
        guard totalStampCount > 0 else { return 0 }
        return min(Float(userStampCount) / Float(totalStampCount), 1.0)
    }

    var percentText: String {
        "\(Int(progress * 100))%"
    }

    func fetchStampData() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                let fetchedStamps = try await repository.fetchStamps()
                stamps = fetchedStamps
                totalStampCount = max(fetchedStamps.count, 1)
            } catch {
                print("[StampVM] 스탬프 로드 실패: \(error)")
            }

            if TokenManager.shared.isLoggedIn {
                // 유저 수집 스탬프 목록
                do {
                    let userStamps = try await repository.fetchUserStamps()
                    collectedStampIds = Set(userStamps.map { $0.stampId })
                    userStampCount = userStamps.count
                } catch {
                    print("[StampVM] 유저 스탬프 로드 실패: \(error)")
                    // fallback: 프로필에서 수집 수만
                    do {
                        let profile = try await repository.fetchUserProfile()
                        userStampCount = profile.stampCount ?? 0
                    } catch {
                        print("[StampVM] 프로필 로드 실패: \(error)")
                    }
                }
            }

            isLoading = false
        }
    }
}
