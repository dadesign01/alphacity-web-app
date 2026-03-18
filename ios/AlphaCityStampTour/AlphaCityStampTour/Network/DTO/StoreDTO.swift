//
//  StoreDTO.swift
//  AlphaCityStampTour
//

import Foundation

struct StoreRegisterRequest: Encodable {
    let name: String
    let category: String
    let ownerName: String
    let phone: String
    var address: String?
    var addressDetail: String?
    var description: String?
    var imageUrl: String?
    var storeCode: String?
    var operatingDays: String?
    var openTime: String?
    var closeTime: String?
}

struct StoreData: Decodable, Identifiable {
    let id: Int
    let name: String
    let category: String
    let ownerName: String
    let phone: String
    let address: String?
    let addressDetail: String?
    let description: String?
    let imageUrl: String?
    let storeCode: String?
    let operatingDays: String?
    let openTime: String?
    let closeTime: String?
    let latitude: Double?
    let longitude: Double?
    let status: String
    let mission: StoreMissionInfo?
    let storeCoupons: [StoreCouponDetail]?
    let program: StoreProgramInfo?
}

struct StoreMissionInfo: Decodable {
    let id: Int
    let name: String
    let type: String
}

struct StoreCouponDetail: Decodable {
    let coupon: StoreCouponData
}

struct StoreCouponData: Decodable {
    let id: Int
    let name: String
    let description: String?
    let imageUrl: String?
}

struct StoreProgramInfo: Decodable {
    let id: Int
    let name: String
}
