//
//  StampViewModel.swift
//  AlphaCityStampTour
//

import Foundation
import Combine

struct RedeemSuccess {
    let couponName: String
    let couponDescription: String?
    let validUntil: String
    let requiredStamps: Int
}

@MainActor
final class StampViewModel: ObservableObject {
    @Published var stamps: [StampData] = []
    @Published var totalStampCount: Int = 0
    @Published var userStampCount: Int = 0
    @Published var collectedStampIds: Set<Int> = []
    @Published var userStamps: [UserStampData] = []
    @Published var missions: [MissionData] = []
    @Published var coupons: [CouponData] = []
    @Published var redeemedCouponIds: Set<Int> = []
    @Published var festivals: [FestivalData] = []
    @Published var isRedeeming = false
    @Published var redeemSuccess: RedeemSuccess? = nil
    @Published var redeemError: String? = nil
    @Published var isLoading = false
    @Published var availableStamps: Int = 0

    private let repository = StampRepository.shared
    private let homeRepository = HomeRepository.shared
    private let selection = FestivalSelection.shared
    private var cancellables = Set<AnyCancellable>()
    // 진행 중인 로드 작업 (축제 변경 시 취소 후 즉시 재요청)
    private var fetchTask: Task<Void, Never>? = nil

    var selectedFestivalId: Int? { selection.selectedFestivalId }

    func selectFestival(_ id: Int?) {
        selection.select(id)
        fetchStampData()
    }

    init() {
        // 외부에서 축제가 변경되면 스탬프 화면도 다시 로드
        selection.$selectedFestivalId
            .dropFirst()
            .sink { [weak self] _ in self?.fetchStampData() }
            .store(in: &cancellables)
    }

    var progress: Float {
        guard totalStampCount > 0 else { return 0 }
        return min(Float(userStampCount) / Float(totalStampCount), 1.0)
    }

    var percentText: String {
        "\(Int(progress * 100))%"
    }

    var completedMissionCount: Int {
        missions.filter { $0.isCompleted == true }.count
    }

    var remainingMissionCount: Int {
        missions.count - completedMissionCount
    }

    func fetchStampData() {
        // 이전 로드를 취소해 오래된 축제 데이터가 새 선택을 덮어쓰지 않도록 함
        fetchTask?.cancel()

        let festivalId = selection.selectedFestivalId

        fetchTask = Task {
            isLoading = true
            defer { isLoading = false }

            // 축제 목록 (드롭다운용)
            if festivals.isEmpty {
                do {
                    festivals = try await homeRepository.fetchFestivals()
                } catch {
                    print("[StampVM] 축제 로드 실패: \(error)")
                }
            }

            do {
                var fetchedStamps = try await repository.fetchStamps(festivalId: festivalId)
                // 선택한 축제에 스탬프가 없으면 전체 축제 스탬프를 잠금 상태로 노출 (#8-1)
                if fetchedStamps.isEmpty, festivalId != nil {
                    fetchedStamps = (try? await repository.fetchStamps(festivalId: nil)) ?? []
                }
                stamps = fetchedStamps
                totalStampCount = max(fetchedStamps.count, 1)
            } catch {
                print("[StampVM] 스탬프 로드 실패: \(error)")
            }

            do {
                missions = try await repository.fetchMissions(festivalId: festivalId)
            } catch {
                print("[StampVM] 미션 로드 실패: \(error)")
            }

            if TokenManager.shared.isLoggedIn {
                do {
                    let fetchedUserStamps = try await repository.fetchUserStamps(festivalId: festivalId)
                    userStamps = fetchedUserStamps
                    collectedStampIds = Set(fetchedUserStamps.map { $0.stampId })
                    userStampCount = fetchedUserStamps.count
                } catch {
                    print("[StampVM] 유저 스탬프 로드 실패: \(error)")
                }
            }

            // 쿠폰 목록 로드 (축제별)
            do {
                let couponResponse = try await repository.fetchCoupons(festivalId: festivalId)
                coupons = couponResponse.coupons
                redeemedCouponIds = Set(couponResponse.redeemedCouponIds)
                availableStamps = couponResponse.availableStamps ?? 0
            } catch {
                print("[StampVM] 쿠폰 로드 실패: \(error)")
            }
        }
    }

    func redeemCoupon(couponId: Int) {
        guard !isRedeeming else { return }
        isRedeeming = true
        redeemSuccess = nil
        redeemError = nil

        let coupon = coupons.first { $0.id == couponId }

        Task {
            do {
                _ = try await repository.redeemCoupon(couponId: couponId)
                redeemSuccess = RedeemSuccess(
                    couponName: coupon?.name ?? "쿠폰",
                    couponDescription: coupon?.description,
                    validUntil: coupon?.validUntil ?? "",
                    requiredStamps: coupon?.requiredStamps ?? 0
                )
                fetchStampData()
            } catch {
                redeemError = error.localizedDescription
            }
            isRedeeming = false
        }
    }

    func clearRedeemSuccess() {
        redeemSuccess = nil
    }

    func clearRedeemError() {
        redeemError = nil
    }
}
