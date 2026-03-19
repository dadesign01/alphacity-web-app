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

    func updateProfile(nickname: String, currentPassword: String?, newPassword: String?, phone: String? = nil, name: String? = nil, address: String? = nil, addressDetail: String? = nil, birthDate: String? = nil, gender: String? = nil) async throws -> UserProfileData {
        var body: [String: String] = ["nickname": nickname]
        if let currentPassword = currentPassword, !currentPassword.isEmpty {
            body["currentPassword"] = currentPassword
        }
        if let newPassword = newPassword, !newPassword.isEmpty {
            body["newPassword"] = newPassword
        }
        if let phone = phone {
            body["phone"] = phone
        }
        if let name = name, !name.isEmpty {
            body["name"] = name
        }
        if let address = address {
            body["address"] = address
        }
        if let addressDetail = addressDetail {
            body["addressDetail"] = addressDetail
        }
        if let birthDate = birthDate, !birthDate.isEmpty {
            body["birthDate"] = birthDate
        }
        if let gender = gender, !gender.isEmpty {
            body["gender"] = gender
        }
        return try await client.request(
            path: "users/me",
            method: "PUT",
            body: body,
            authenticated: true
        )
    }

    func deleteAccount() async throws {
        let _: EmptyData? = try await client.request(
            path: "users/me",
            method: "DELETE",
            authenticated: true
        )
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
        return try await client.request(path: "programs/\(programId)/missions", authenticated: true)
    }

    func completeMission(missionId: Int, answer: String? = nil) async throws -> MissionCompletionData {
        var body: [String: String]? = nil
        if let answer = answer {
            body = ["answer": answer]
        }
        return try await client.request(
            path: "missions/\(missionId)/complete",
            method: "POST",
            body: body,
            authenticated: true
        )
    }

    func fetchActivityHistory() async throws -> [ActivityItemDTO] {
        return try await client.request(path: "users/activity", authenticated: true)
    }
}
