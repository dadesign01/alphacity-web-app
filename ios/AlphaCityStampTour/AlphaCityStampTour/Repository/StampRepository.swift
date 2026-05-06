//
//  StampRepository.swift
//  AlphaCityStampTour
//

import Foundation

final class StampRepository {
    static let shared = StampRepository()
    private let client = APIClient.shared

    private init() {}

    func fetchStamps(festivalId: Int? = nil) async throws -> [StampData] {
        let path = festivalId.map { "stamps?festivalId=\($0)" } ?? "stamps"
        return try await client.request(path: path)
    }

    func fetchUserStamps(festivalId: Int? = nil) async throws -> [UserStampData] {
        let path = festivalId.map { "stamps/my?festivalId=\($0)" } ?? "stamps/my"
        return try await client.request(path: path, authenticated: true)
    }

    func fetchUserProfile() async throws -> UserProfileData {
        return try await client.request(path: "users/me", authenticated: true)
    }

    func fetchMissions(festivalId: Int? = nil) async throws -> [MissionData] {
        let path = festivalId.map { "missions?festivalId=\($0)" } ?? "missions"
        return try await client.request(path: path)
    }

    func fetchCoupons(festivalId: Int? = nil) async throws -> CouponListResponse {
        let path = festivalId.map { "coupons?festivalId=\($0)" } ?? "coupons"
        return try await client.request(path: path)
    }

    func redeemCoupon(couponId: Int) async throws -> UserCouponData {
        return try await client.request(path: "coupons/\(couponId)/redeem", method: "POST", authenticated: true)
    }

    func useCoupon(userCouponId: Int) async throws -> UserCouponData {
        return try await client.request(path: "coupons/use", method: "POST", body: ["userCouponId": userCouponId], authenticated: true)
    }

    func fetchMyCoupons(festivalId: Int? = nil) async throws -> [MyCouponData] {
        var path = "coupons?my=true"
        if let festivalId = festivalId { path += "&festivalId=\(festivalId)" }
        return try await client.request(path: path, authenticated: true)
    }
}
