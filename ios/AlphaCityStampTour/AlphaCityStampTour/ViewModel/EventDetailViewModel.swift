//
//  EventDetailViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class EventDetailViewModel: ObservableObject {
    @Published var event: EventData?
    @Published var isLoading = false
    @Published var isParticipating = false
    @Published var myRaffleNumber: Int?
    @Published var message: String?

    private let repository = HomeRepository.shared

    func fetchEventDetail(eventId: Int) {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                let data = try await repository.fetchEventDetail(eventId: eventId)
                event = data
                if data.isParticipated == true {
                    myRaffleNumber = data.myRaffleNumber
                }
            } catch {
                message = "이벤트 정보를 불러올 수 없습니다"
            }
            isLoading = false
        }
    }

    func participate(eventId: Int) {
        guard !isParticipating else { return }

        guard TokenManager.shared.isLoggedIn else {
            message = "로그인이 필요합니다"
            return
        }

        if event?.isParticipated == true || myRaffleNumber != nil {
            message = "이미 참여하셨습니다"
            return
        }

        isParticipating = true

        Task {
            do {
                let result = try await repository.participateInEvent(eventId: eventId)
                myRaffleNumber = result.raffleNumber
                // 이벤트 데이터 새로고침
                if var updated = event {
                    event = EventData(
                        id: updated.id,
                        name: updated.name,
                        description: updated.description,
                        imageUrl: updated.imageUrl,
                        type: updated.type,
                        startDate: updated.startDate,
                        endDate: updated.endDate,
                        reward: updated.reward,
                        winnerCount: updated.winnerCount,
                        participantLimit: updated.participantLimit,
                        participantCount: (updated.participantCount ?? 0) + 1,
                        status: updated.status,
                        program: updated.program,
                        isParticipated: true,
                        myRaffleNumber: result.raffleNumber,
                        price: updated.price,
                        duration: updated.duration,
                        capacity: updated.capacity,
                        location: updated.location
                    )
                }
                message = "참여 완료!"
            } catch {
                message = error.localizedDescription
            }
            isParticipating = false
        }
    }
}
