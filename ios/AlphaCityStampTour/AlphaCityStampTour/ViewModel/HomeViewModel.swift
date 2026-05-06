//
//  HomeViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import Combine

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var banners: [BannerData] = []
    @Published var festivals: [FestivalData] = []
    @Published var programs: [ProgramData] = []
    @Published var events: [EventData] = []
    @Published var totalStampCount: Int = 10
    @Published var userStampCount: Int = 0
    @Published var userCouponCount: Int = 0
    @Published var isLoading = false

    private let repository = HomeRepository.shared
    private let selection = FestivalSelection.shared
    private var cancellables = Set<AnyCancellable>()

    init() {
        // 선택된 축제가 바뀌면 자동으로 다시 로드
        selection.$selectedFestivalId
            .dropFirst()
            .sink { [weak self] _ in self?.fetchHomeData() }
            .store(in: &cancellables)
    }

    func selectFestival(_ id: Int?) {
        selection.select(id)
    }

    var selectedFestivalId: Int? { selection.selectedFestivalId }

    func fetchHomeData() {
        guard !isLoading else { return }
        isLoading = true

        let festivalId = selection.selectedFestivalId

        Task {
            async let bannersTask = repository.fetchBanners()
            async let festivalsTask = repository.fetchFestivals()
            async let programsTask = repository.fetchPrograms(festivalId: festivalId)
            async let eventsTask = repository.fetchEvents(festivalId: festivalId)
            async let stampsTask = repository.fetchStamps(festivalId: festivalId)

            do { banners = try await bannersTask } catch { print("[HomeVM] 배너 로드 실패: \(error)") }
            do { festivals = try await festivalsTask } catch { print("[HomeVM] 축제 로드 실패: \(error)") }

            do {
                let fetchedPrograms = try await programsTask
                programs = filterTodayPrograms(fetchedPrograms)
            } catch {
                print("[HomeVM] 프로그램 로드 실패: \(error)")
            }

            do { events = try await eventsTask } catch { print("[HomeVM] 이벤트 로드 실패: \(error)") }
            do { totalStampCount = max((try await stampsTask).count, 1) } catch { print("[HomeVM] 스탬프 로드 실패: \(error)") }

            if TokenManager.shared.isLoggedIn {
                do {
                    let profile = try await repository.fetchUserProfile()
                    userStampCount = profile.stampCount ?? 0
                    userCouponCount = profile.couponCount ?? 0
                } catch {
                    print("[HomeVM] 프로필 로드 실패: \(error)")
                }
            }

            isLoading = false
        }
    }

    private func filterTodayPrograms(_ programs: [ProgramData]) -> [ProgramData] {
        let today = Date()
        let parser = DateFormatter()
        parser.dateFormat = "yyyy-MM-dd"

        let filtered = programs.filter { program in
            if program.status == "in_progress" { return true }
            guard let start = parser.date(from: String(program.startDate.prefix(10))),
                  let end = parser.date(from: String(program.endDate.prefix(10))) else {
                return true
            }
            return today >= start && today <= end
        }
        return filtered.isEmpty ? programs : filtered
    }
}
