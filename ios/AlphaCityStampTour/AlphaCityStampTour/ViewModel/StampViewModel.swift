//
//  StampViewModel.swift
//  AlphaCityStampTour
//

import Foundation

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
    @Published var isRedeeming = false
    @Published var redeemSuccess: RedeemSuccess? = nil
    @Published var redeemError: String? = nil
    @Published var isLoading = false

    private let repository = StampRepository.shared

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
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                let fetchedStamps = try await repository.fetchStamps()
                stamps = fetchedStamps
                totalStampCount = max(fetchedStamps.count, 1)
            } catch {
                print("[StampVM] 스탬프 로드 실패: \(error)")
            }

            do {
                missions = try await repository.fetchMissions()
            } catch {
                print("[StampVM] 미션 로드 실패: \(error)")
            }

            if TokenManager.shared.isLoggedIn {
                do {
                    let fetchedUserStamps = try await repository.fetchUserStamps()
                    userStamps = fetchedUserStamps
                    collectedStampIds = Set(fetchedUserStamps.map { $0.stampId })
                    userStampCount = fetchedUserStamps.count
                } catch {
                    print("[StampVM] 유저 스탬프 로드 실패: \(error)")
                    do {
                        let profile = try await repository.fetchUserProfile()
                        userStampCount = profile.stampCount ?? 0
                    } catch {
                        print("[StampVM] 프로필 로드 실패: \(error)")
                    }
                }
            }

            // 쿠폰 목록 로드
            do {
                coupons = try await repository.fetchCoupons()
            } catch {
                print("[StampVM] 쿠폰 로드 실패: \(error)")
            }

            isLoading = false
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
