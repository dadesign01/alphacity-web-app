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
}
