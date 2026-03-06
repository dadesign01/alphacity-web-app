//
//  ProgramListViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class ProgramListViewModel: ObservableObject {
    @Published var programs: [ProgramData] = []
    @Published var selectedCategory: String = "exhibition"
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    func selectCategory(_ category: String) {
        selectedCategory = category
        fetchPrograms()
    }

    func fetchPrograms() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                programs = try await repository.fetchProgramsByCategory(selectedCategory)
            } catch {
                print("프로그램 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}
