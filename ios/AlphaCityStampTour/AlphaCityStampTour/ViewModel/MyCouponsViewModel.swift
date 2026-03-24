//
//  MyCouponsViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MyCouponsViewModel: ObservableObject {
    @Published var myCoupons: [MyCouponData] = []
    @Published var isLoading = false
    @Published var useSuccess = false
    @Published var useError: String? = nil

    private let repository = StampRepository.shared

    var availableCoupons: [MyCouponData] {
        let today = ISO8601DateFormatter().string(from: Date()).prefix(10)
        return myCoupons.filter { coupon in
            coupon.status == "issued" && String(coupon.validUntil.prefix(10)) >= today
        }
    }

    func useCoupon(userCouponId: Int) {
        Task {
            do {
                _ = try await repository.useCoupon(userCouponId: userCouponId)
                useSuccess = true
                fetchMyCoupons()
            } catch {
                useError = error.localizedDescription
            }
        }
    }

    func fetchMyCoupons() {
        guard !isLoading else { return }
        isLoading = true

        Task {
            do {
                myCoupons = try await repository.fetchMyCoupons()
            } catch {
                print("[MyCouponsVM] 내 쿠폰 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}
