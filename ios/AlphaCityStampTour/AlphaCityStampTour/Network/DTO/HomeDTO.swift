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
    let subcategory: String?
    let hasCoupon: Bool?
    let imageUrl: String?
    let operatingHours: String?
    let location: String?
    let latitude: Double?
    let longitude: Double?
    let phone: String?
    let speaker: String?
    let ownerName: String?
    let storeCode: String?
    let operatingDays: String?
    let startDate: String
    let endDate: String
    let status: String
    let events: [EventData]?
    let storeCoupons: [StoreCouponInfo]?
    let stores: [ProgramStoreData]?
}

struct StoreCouponInfo: Decodable {
    let storeId: Int
    let storeName: String
    let couponId: Int
    let couponName: String
    let couponDescription: String?
}

struct ProgramStoreData: Decodable, Identifiable {
    let id: Int
    let name: String
}

// MARK: - 미션

struct MissionData: Decodable, Identifiable {
    let id: Int
    let name: String
    let type: String
    let placeId: Int?
    let programId: Int?
    let stampId: Int?
    let question: String?
    let answer: String?
    let options: [String]?
    let stayMinutes: Int?
    let place: MissionPlace?
    let isCompleted: Bool?

    struct MissionPlace: Decodable {
        let name: String
        let latitude: Double?
        let longitude: Double?
    }
}

// MARK: - 미션 완료 결과

struct MissionCompletionData: Decodable {
    let id: Int
    let missionId: Int
    let userId: Int
    let completedAt: String?
    let stamp: StampData?
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
    let winnerCount: Int?
    let participantLimit: Int
    let participantCount: Int?
    let status: String
    let program: EventProgram?
    let isParticipated: Bool?
    let myRaffleNumber: Int?
    let price: Int?
    let duration: Int?
    let capacity: Int?
    let location: String?

    struct EventProgram: Decodable {
        let name: String
    }
}

// MARK: - 이벤트 참여 결과

struct EventParticipationData: Decodable {
    let id: Int
    let eventId: Int
    let userId: Int
    let raffleNumber: Int?
    let joinedAt: String
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

// MARK: - 쿠폰

struct CouponListResponse: Decodable {
    let coupons: [CouponData]
    let redeemedCouponIds: [Int]
    let availableStamps: Int?
}

struct CouponData: Decodable, Identifiable {
    let id: Int
    let name: String
    let description: String?
    let requiredStamps: Int
    let validUntil: String
    let imageUrl: String?
    let programId: Int?
}

struct UserCouponData: Decodable, Identifiable {
    let id: Int
    let userId: Int
    let couponId: Int
    let code: String
    let status: String
    let storeId: Int?
    let requestedAt: String?
    let usedAt: String?
    let createdAt: String?
}

struct MyCouponData: Decodable, Identifiable {
    let id: Int
    let couponId: Int
    let name: String
    let description: String?
    let imageUrl: String?
    let code: String
    let status: String
    let validUntil: String
    let storeName: String?
    let requestedAt: String?
    let usedAt: String?
    let createdAt: String?
}

// MARK: - 활동 이력

struct ActivityItemDTO: Decodable {
    let type: String
    let subType: String?
    let title: String
    let date: String
    let validUntil: String?
}

// MARK: - 유저 프로필

struct UserProfileData: Decodable {
    let id: Int
    let email: String
    let nickname: String
    let name: String?
    let phone: String?
    let address: String?
    let addressDetail: String?
    let profileImage: String?
    let provider: String?
    let birthDate: String?
    let gender: String?
    let stampCount: Int?
    let couponCount: Int?
    let missionCount: Int?
}
