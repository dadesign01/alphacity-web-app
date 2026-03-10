//
//  HomeRepository.swift
//  AlphaCityStampTour
//

import Foundation

final class HomeRepository {
    static let shared = HomeRepository()
    private let client = APIClient.shared

    private init() {}

    func fetchBanners() async throws -> [BannerData] {
        return try await client.request(path: "banners")
    }

    func fetchPrograms() async throws -> [ProgramData] {
        return try await client.request(path: "programs")
    }

    func fetchProgramsByCategory(_ category: String) async throws -> [ProgramData] {
        return try await client.request(path: "programs?category=\(category)")
    }

    func fetchEvents() async throws -> [EventData] {
        return try await client.request(path: "events")
    }

    func fetchStamps() async throws -> [StampData] {
        return try await client.request(path: "stamps")
    }

    func fetchUserProfile() async throws -> UserProfileData {
        return try await client.request(path: "users/me", authenticated: true)
    }

    func fetchEventDetail(eventId: Int) async throws -> EventData {
        return try await client.request(path: "events/\(eventId)", authenticated: true)
    }

    func participateInEvent(eventId: Int) async throws -> EventParticipationData {
        return try await client.request(
            path: "events/\(eventId)/participate",
            method: "POST",
            authenticated: true
        )
    }

    func fetchProgramById(_ id: Int) async throws -> ProgramData {
        return try await client.request(path: "programs/\(id)", authenticated: true)
    }

    func fetchProgramMissions(programId: Int) async throws -> [MissionData] {
        return try await client.request(path: "programs/\(programId)/missions")
    }
}
