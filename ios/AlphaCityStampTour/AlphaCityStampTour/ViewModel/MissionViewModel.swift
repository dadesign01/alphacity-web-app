//
//  MissionViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import CoreLocation

@MainActor
final class MissionViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isCompleted = false
    @Published var message: String?
    @Published var showAlert = false
    @Published var alertMessage = ""

    // 체류시간 미션
    @Published var isTimerRunning = false
    @Published var remainingSeconds: Int = 0

    // 위치 인증 미션
    @Published var currentLocation: CLLocation?

    private let repository = HomeRepository.shared
    private let locationManager = CLLocationManager()
    private var timer: Timer?

    // MARK: - 퀴즈 미션 완료

    func completeQuizMission(missionId: Int, answer: String) {
        guard !isLoading else { return }
        guard !answer.trimmingCharacters(in: .whitespaces).isEmpty else {
            alertMessage = "답변을 입력해주세요"
            showAlert = true
            return
        }

        isLoading = true
        Task {
            do {
                _ = try await repository.completeMission(missionId: missionId, answer: answer)
                isCompleted = true
                message = "정답입니다! 미션 완료!"
            } catch let error as APIError {
                switch error {
                case .serverError(let msg):
                    if msg.contains("정답") {
                        alertMessage = "정답이 아닙니다"
                    } else if msg.contains("이미 완료") {
                        isCompleted = true
                        alertMessage = "이미 완료한 미션입니다"
                    } else {
                        alertMessage = msg
                    }
                default:
                    alertMessage = "미션 완료에 실패했습니다"
                }
                showAlert = true
            } catch {
                alertMessage = "미션 완료에 실패했습니다"
                showAlert = true
            }
            isLoading = false
        }
    }

    // MARK: - 위치 인증 미션 완료

    func completeLocationMission(missionId: Int, targetLat: Double, targetLng: Double) {
        guard !isLoading else { return }

        isLoading = true

        locationManager.requestWhenInUseAuthorization()

        Task {
            let location = await getCurrentLocation()

            guard let location = location else {
                alertMessage = "위치를 가져올 수 없습니다. 위치 권한을 확인해주세요."
                showAlert = true
                isLoading = false
                return
            }

            let targetLocation = CLLocation(latitude: targetLat, longitude: targetLng)
            let distance = location.distance(from: targetLocation)

            if distance <= 100 {
                do {
                    _ = try await repository.completeMission(missionId: missionId)
                    isCompleted = true
                    message = "위치 인증 완료!"
                } catch let error as APIError {
                    switch error {
                    case .serverError(let msg):
                        if msg.contains("이미 완료") {
                            isCompleted = true
                            alertMessage = "이미 완료한 미션입니다"
                        } else {
                            alertMessage = msg
                        }
                    default:
                        alertMessage = "미션 완료에 실패했습니다"
                    }
                    showAlert = true
                } catch {
                    alertMessage = "미션 완료에 실패했습니다"
                    showAlert = true
                }
            } else {
                alertMessage = "해당 장소 근처에서 인증해주세요.\n(현재 거리: \(Int(distance))m)"
                showAlert = true
            }
            isLoading = false
        }
    }

    private func getCurrentLocation() async -> CLLocation? {
        return await withCheckedContinuation { continuation in
            let delegate = LocationDelegate { location in
                continuation.resume(returning: location)
            }
            locationManager.delegate = delegate
            // delegate를 강하게 유지하기 위해 objc_setAssociatedObject 사용
            objc_setAssociatedObject(locationManager, "delegate", delegate, .OBJC_ASSOCIATION_RETAIN)
            locationManager.requestLocation()
        }
    }

    // MARK: - 체류시간 미션

    func startStayTimeMission(missionId: Int, minutes: Int) {
        guard !isTimerRunning else { return }
        remainingSeconds = minutes * 60
        isTimerRunning = true

        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self else { return }
                self.remainingSeconds -= 1
                if self.remainingSeconds <= 0 {
                    self.timer?.invalidate()
                    self.timer = nil
                    self.isTimerRunning = false
                    await self.completeStayTimeMission(missionId: missionId)
                }
            }
        }
    }

    func stopTimer() {
        timer?.invalidate()
        timer = nil
        isTimerRunning = false
    }

    private func completeStayTimeMission(missionId: Int) async {
        isLoading = true
        do {
            _ = try await repository.completeMission(missionId: missionId)
            isCompleted = true
            message = "체류시간 미션 완료!"
        } catch let error as APIError {
            switch error {
            case .serverError(let msg):
                if msg.contains("이미 완료") {
                    isCompleted = true
                    alertMessage = "이미 완료한 미션입니다"
                } else {
                    alertMessage = msg
                }
            default:
                alertMessage = "미션 완료에 실패했습니다"
            }
            showAlert = true
        } catch {
            alertMessage = "미션 완료에 실패했습니다"
            showAlert = true
        }
        isLoading = false
    }

    var formattedTime: String {
        let mins = remainingSeconds / 60
        let secs = remainingSeconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }

    deinit {
        timer?.invalidate()
    }
}

// MARK: - Location Delegate

private class LocationDelegate: NSObject, CLLocationManagerDelegate {
    private let completion: (CLLocation?) -> Void
    private var didRespond = false

    init(completion: @escaping (CLLocation?) -> Void) {
        self.completion = completion
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard !didRespond else { return }
        didRespond = true
        completion(locations.first)
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        guard !didRespond else { return }
        didRespond = true
        completion(nil)
    }
}
