//
//  ProgramListViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class ProgramListViewModel: ObservableObject {
    @Published var programs: [ProgramData] = []
    @Published var festivals: [FestivalData] = []
    @Published var selectedCategory: String = "exhibition"
    @Published var isLoading = false

    private let repository = HomeRepository.shared
    private let selection = FestivalSelection.shared

    var selectedFestivalId: Int? { selection.selectedFestivalId }

    init() {
        Task { await loadFestivals() }
    }

    private func loadFestivals() async {
        do {
            festivals = try await repository.fetchFestivals()
        } catch {
            print("축제 로드 실패: \(error)")
        }
    }

    func selectCategory(_ category: String) {
        selectedCategory = category
        fetchPrograms()
    }

    func selectFestival(_ id: Int?) {
        selection.select(id)
        fetchPrograms()
    }

    func fetchPrograms() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                programs = try await repository.fetchPrograms(festivalId: selection.selectedFestivalId, category: selectedCategory)
            } catch {
                print("프로그램 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}
