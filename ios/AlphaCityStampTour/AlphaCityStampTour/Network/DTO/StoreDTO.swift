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

// StoreData → ProgramData 변환 (ProgramDetailView 재사용)
extension StoreData {
    func toProgramData() -> ProgramData {
        let hours = [openTime, closeTime].compactMap { $0 }.joined(separator: " - ")
        let fullAddress = [address, addressDetail].compactMap { $0 }.joined(separator: " ")
        return ProgramData(
            id: -id,
            festivalId: nil,
            name: name,
            description: description,
            category: "food",
            subcategory: nil,
            hasCoupon: !(storeCoupons?.isEmpty ?? true),
            imageUrl: imageUrl,
            operatingHours: hours.isEmpty ? nil : hours,
            location: fullAddress.isEmpty ? nil : fullAddress,
            latitude: latitude,
            longitude: longitude,
            phone: phone,
            speaker: nil,
            ownerName: ownerName,
            storeCode: storeCode,
            operatingDays: operatingDays,
            startDate: "",
            endDate: "",
            status: "in_progress",
            events: nil,
            storeCoupons: storeCoupons?.map {
                StoreCouponInfo(storeId: id, storeName: name, couponId: $0.coupon.id, couponName: $0.coupon.name, couponDescription: $0.coupon.description)
            },
            stores: nil
        )
    }
}
