//
//  MapViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MapViewModel: ObservableObject {
    @Published var programs: [ProgramData] = []
    @Published var selectedCategory: String = "all"
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    var filteredPrograms: [ProgramData] {
        let withCoords = programs.filter { $0.latitude != nil && $0.longitude != nil }
        if selectedCategory == "all" {
            return withCoords
        }
        return withCoords.filter { $0.category == selectedCategory }
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

    func selectCategory(_ category: String) {
        selectedCategory = category
    }
}
