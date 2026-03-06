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

struct StoreData: Decodable {
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
    let status: String
}
