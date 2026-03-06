//
//  HomeDTO.swift
//  AlphaCityStampTour
//

import Foundation

// MARK: - 배너

struct BannerData: Decodable, Identifiable {
    let id: Int
    let title: String
    let imageUrl: String
}

// MARK: - 프로그램

struct ProgramData: Decodable, Identifiable {
    let id: Int
    let name: String
    let description: String?
    let category: String?
    let imageUrl: String?
    let operatingHours: String?
    let location: String?
    let latitude: Double?
    let longitude: Double?
    let speaker: String?
    let startDate: String
    let endDate: String
    let status: String
    let events: [EventData]?
}

// MARK: - 이벤트

struct EventData: Decodable, Identifiable {
    let id: Int
    let name: String
    let description: String?
    let imageUrl: String?
    let type: String
    let startDate: String
    let endDate: String
    let reward: String?
    let participantLimit: Int
    let participantCount: Int?
    let status: String
    let program: EventProgram?
    let isParticipated: Bool?

    struct EventProgram: Decodable {
        let name: String
    }
}

// MARK: - 스탬프

struct StampData: Decodable, Identifiable {
    let id: Int
    let name: String
    let conditionType: String
    let conditionDetail: String?
    let imageUrl: String?
}

// MARK: - 유저 스탬프 수집 기록

struct UserStampData: Decodable, Identifiable {
    let id: Int
    let stampId: Int
    let collectedAt: String?
}

// MARK: - 유저 프로필

struct UserProfileData: Decodable {
    let id: Int
    let email: String
    let nickname: String
    let profileImage: String?
    let stampCount: Int?
    let couponCount: Int?
    let missionCount: Int?
}
