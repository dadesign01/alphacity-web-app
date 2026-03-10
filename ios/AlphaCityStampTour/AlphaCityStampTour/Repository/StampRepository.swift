//
//  StampRepository.swift
//  AlphaCityStampTour
//

import Foundation

final class StampRepository {
    static let shared = StampRepository()
    private let client = APIClient.shared

    private init() {}

    func fetchStamps() async throws -> [StampData] {
        return try await client.request(path: "stamps")
    }

    func fetchUserStamps() async throws -> [UserStampData] {
        return try await client.request(path: "stamps/my", authenticated: true)
    }

    func fetchUserProfile() async throws -> UserProfileData {
        return try await client.request(path: "users/me", authenticated: true)
    }

    func fetchMissions() async throws -> [MissionData] {
        return try await client.request(path: "missions")
    }

    func fetchCoupons() async throws -> [CouponData] {
        return try await client.request(path: "coupons")
    }

    func redeemCoupon(couponId: Int) async throws -> UserCouponData {
        return try await client.request(path: "coupons/\(couponId)/redeem", method: "POST", authenticated: true)
    }
}
