//
//  HomeViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class HomeViewModel: ObservableObject {
    @Published var banners: [BannerData] = []
    @Published var programs: [ProgramData] = []
    @Published var events: [EventData] = []
    @Published var totalStampCount: Int = 10
    @Published var userStampCount: Int = 0
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    func fetchHomeData() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            async let bannersTask = repository.fetchBanners()
            async let programsTask = repository.fetchPrograms()
            async let eventsTask = repository.fetchEvents()
            async let stampsTask = repository.fetchStamps()

            do {
                banners = try await bannersTask
            } catch {
                print("[HomeVM] 배너 로드 실패: \(error)")
            }

            do {
                let fetchedPrograms = try await programsTask
                programs = filterTodayPrograms(fetchedPrograms)
            } catch {
                print("[HomeVM] 프로그램 로드 실패: \(error)")
            }

            do {
                let fetchedEvents = try await eventsTask
                events = fetchedEvents
            } catch {
                print("[HomeVM] 이벤트 로드 실패: \(error)")
            }

            do {
                let fetchedStamps = try await stampsTask
                totalStampCount = max(fetchedStamps.count, 1)
            } catch {
                print("[HomeVM] 스탬프 로드 실패: \(error)")
            }

            // 로그인 상태면 유저 프로필도 가져오기
            if TokenManager.shared.isLoggedIn {
                do {
                    let profile = try await repository.fetchUserProfile()
                    userStampCount = profile.stampCount ?? 0
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
                return true // 파싱 실패 시 포함
            }
            return today >= start && today <= end
        }
        return filtered.isEmpty ? programs : filtered // 필터 결과 없으면 전체 표시
    }
}
