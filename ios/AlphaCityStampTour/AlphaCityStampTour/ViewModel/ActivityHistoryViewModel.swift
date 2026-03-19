//
//  ActivityHistoryViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class ActivityHistoryViewModel: ObservableObject {
    @Published var activities: [ActivityItemDTO] = []
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    func fetchActivities() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                activities = try await repository.fetchActivityHistory()
            } catch {
                print("[ActivityHistoryVM] 활동 이력 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}
