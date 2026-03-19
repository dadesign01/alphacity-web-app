//
//  MyCouponsViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class MyCouponsViewModel: ObservableObject {
    @Published var myCoupons: [MyCouponData] = []
    @Published var isLoading = false

    private let repository = StampRepository.shared

    var availableCoupons: [MyCouponData] {
        myCoupons.filter { $0.status == "issued" }
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
