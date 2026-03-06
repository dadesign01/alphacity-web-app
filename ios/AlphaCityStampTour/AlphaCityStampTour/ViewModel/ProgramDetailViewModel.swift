//
//  ProgramDetailViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class ProgramDetailViewModel: ObservableObject {
    @Published var isParticipated = false
    @Published var isLoading = false
    @Published var message: String? = nil

    private let repository = HomeRepository.shared

    func checkParticipation(program: ProgramData) {
        guard let events = program.events else { return }
        isParticipated = events.contains { $0.isParticipated == true }
    }

    func participate(program: ProgramData) {
        guard TokenManager.shared.isLoggedIn else {
            message = "로그인이 필요합니다"
            return
        }

        guard !isParticipated else {
            message = "이미 참여하셨습니다"
            return
        }

        // 첫 번째 in_progress 이벤트에 참여
        guard let event = program.events?.first(where: { $0.status == "in_progress" }) else {
            message = "참여 가능한 이벤트가 없습니다"
            return
        }

        isLoading = true
        message = nil

        Task {
            do {
                try await repository.participateInEvent(eventId: event.id)
                isParticipated = true
                message = "참여 완료!"
            } catch let error as APIError {
                switch error {
                case .serverError(let msg):
                    if msg.contains("이미 참여") {
                        isParticipated = true
                        message = "이미 참여하셨습니다"
                    } else if msg.contains("초과") {
                        message = "참여 인원이 초과되었습니다"
                    } else {
                        message = msg
                    }
                case .unauthorized:
                    message = "로그인이 필요합니다"
                default:
                    message = "참여에 실패했습니다"
                }
            } catch {
                message = "참여에 실패했습니다"
            }
            isLoading = false
        }
    }
}
