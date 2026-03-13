//
//  ProgramDetailViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import AVFoundation
import CoreLocation

@MainActor
final class ProgramDetailViewModel: NSObject, ObservableObject, AVSpeechSynthesizerDelegate, CLLocationManagerDelegate {
    @Published var isParticipated = false
    @Published var isLoading = false
    @Published var message: String? = nil
    @Published var missions: [MissionData] = []
    @Published var isMissionsLoading = false
    @Published var isSpeaking = false
    @Published var isNearLocation = false
    @Published var isCheckingLocation = false

    private let repository = HomeRepository.shared
    private let synthesizer = AVSpeechSynthesizer()
    private let locationManager = CLLocationManager()
    private var targetLatitude: Double = 0
    private var targetLongitude: Double = 0

    override init() {
        super.init()
        synthesizer.delegate = self
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }

    // MARK: - Location

    func checkLocationForParticipation(lat: Double?, lng: Double?) {
        guard let lat = lat, let lng = lng else {
            isNearLocation = true
            return
        }
        targetLatitude = lat
        targetLongitude = lng
        isCheckingLocation = true

        switch locationManager.authorizationStatus {
        case .authorizedWhenInUse, .authorizedAlways:
            locationManager.requestLocation()
        case .notDetermined:
            locationManager.requestWhenInUseAuthorization()
        default:
            isNearLocation = true
            isCheckingLocation = false
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        Task { @MainActor in
            guard let location = locations.first else {
                isNearLocation = true
                isCheckingLocation = false
                return
            }
            let target = CLLocation(latitude: targetLatitude, longitude: targetLongitude)
            isNearLocation = location.distance(from: target) <= 300
            isCheckingLocation = false
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in
            isNearLocation = true
            isCheckingLocation = false
        }
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                if isCheckingLocation {
                    manager.requestLocation()
                }
            case .denied, .restricted:
                if isCheckingLocation {
                    isNearLocation = true
                    isCheckingLocation = false
                }
            default:
                break
            }
        }
    }

    func checkParticipation(program: ProgramData) {
        // 먼저 로컬 데이터로 빠르게 표시
        if let events = program.events {
            isParticipated = events.contains { $0.isParticipated == true }
        }

        // 서버에서 최신 데이터 조회
        guard TokenManager.shared.isLoggedIn else { return }
        Task {
            do {
                let fresh = try await repository.fetchProgramById(program.id)
                if let events = fresh.events {
                    isParticipated = events.contains { $0.isParticipated == true }
                }
            } catch {
                // 실패 시 로컬 데이터 유지
            }
        }
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

    // MARK: - Missions

    func fetchMissions(programId: Int) {
        isMissionsLoading = true
        Task {
            do {
                missions = try await repository.fetchProgramMissions(programId: programId)
            } catch {
                print("Failed to fetch missions: \(error)")
                missions = []
            }
            isMissionsLoading = false
        }
    }

    func availableMissions(for category: String) -> [MissionData] {
        if category == "seminar" {
            return missions.filter { $0.type == "stay_time" }
        } else {
            return missions.filter { $0.type == "quiz" || $0.type == "location_auth" }
        }
    }

    func disabledMissions(for category: String) -> [MissionData] {
        if category == "seminar" {
            return missions.filter { $0.type != "stay_time" }
        } else {
            return missions.filter { $0.type != "quiz" && $0.type != "location_auth" }
        }
    }

    func disabledMessage(for category: String) -> String {
        if category == "seminar" {
            return "해당 프로그램에서는 체류시간 미션만 참여 가능합니다"
        } else {
            return "해당 프로그램에서는 다른 미션을 선택해주세요"
        }
    }

    // MARK: - TTS

    func speakDescription(_ text: String) {
        if isSpeaking {
            stopSpeaking()
        } else {
            let utterance = AVSpeechUtterance(string: text)
            utterance.voice = AVSpeechSynthesisVoice(language: "ko-KR")
            synthesizer.speak(utterance)
        }
    }

    func stopSpeaking() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
    }

    // MARK: - AVSpeechSynthesizerDelegate

    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = true
        }
    }

    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }

    nonisolated func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
}
