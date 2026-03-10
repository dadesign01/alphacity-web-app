//
//  EventHighlightViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class EventHighlightViewModel: ObservableObject {
    @Published var raffleEvents: [EventData] = []
    @Published var firstComeEvents: [EventData] = []
    @Published var experienceEvents: [EventData] = []
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    func fetchEvents() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                let events = try await repository.fetchEvents()
                raffleEvents = events.filter { $0.type == "raffle" }
                firstComeEvents = events.filter { $0.type == "first_come" }
                experienceEvents = events.filter { $0.type == "experience" }
            } catch {
                print("이벤트 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}
