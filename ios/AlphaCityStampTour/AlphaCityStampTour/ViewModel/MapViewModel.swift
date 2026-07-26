//
//  MapViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MapViewModel: ObservableObject {
    @Published var programs: [ProgramData] = []
    @Published var stores: [StoreData] = []
    @Published var missions: [MissionData] = []
    @Published var selectedCategory: String = "all"
    @Published var isLoading = false

    // 카메라 위치 보존 (네비게이션 복귀 시 복원용)
    var savedCameraLat: Double?
    var savedCameraLng: Double?
    var savedCameraZoom: Int?

    var hasSavedCameraPosition: Bool {
        savedCameraLat != nil && savedCameraLng != nil && savedCameraZoom != nil
    }

    func saveCameraPosition(lat: Double, lng: Double, zoom: Int) {
        savedCameraLat = lat
        savedCameraLng = lng
        savedCameraZoom = zoom
    }

    private let repository = HomeRepository.shared
    private let storeRepository = StoreRepository.shared

    var filteredPrograms: [ProgramData] {
        let withCoords = programs.filter { $0.latitude != nil && $0.longitude != nil }
        if selectedCategory == "all" {
            return withCoords
        }
        return withCoords.filter { $0.category == selectedCategory }
    }

    var filteredStores: [StoreData] {
        let withCoords = stores.filter { $0.latitude != nil && $0.longitude != nil }
        if selectedCategory == "all" || selectedCategory == "food" {
            return withCoords
        }
        return []
    }

    // 좌표가 있는 미션만 지도에 표시 (quiz 미션은 place 좌표가 없어 제외)
    var filteredMissions: [MissionData] {
        let withCoords = missions.filter { $0.place?.latitude != nil && $0.place?.longitude != nil }
        if selectedCategory == "all" || selectedCategory == "mission" {
            return withCoords
        }
        return []
    }

    func fetchPrograms() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                programs = try await repository.fetchPrograms()
            } catch {
                print("[MapVM] 프로그램 로드 실패: \(error)")
            }
            isLoading = false
        }
    }

    func fetchStores() {
        Task {
            do {
                stores = try await storeRepository.fetchApprovedStores()
            } catch {
                print("[MapVM] 상점 로드 실패: \(error)")
            }
        }
    }

    func fetchMissions() {
        Task {
            do {
                missions = try await repository.fetchMissions()
            } catch {
                print("[MapVM] 미션 로드 실패: \(error)")
            }
        }
    }

    func selectCategory(_ category: String) {
        selectedCategory = category
    }
}
