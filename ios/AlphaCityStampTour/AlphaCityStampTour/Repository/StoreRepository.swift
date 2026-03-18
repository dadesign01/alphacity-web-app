//
//  StoreRepository.swift
//  AlphaCityStampTour
//

import Foundation

final class StoreRepository {
    static let shared = StoreRepository()
    private let client = APIClient.shared

    private init() {}

    func fetchApprovedStores() async throws -> [StoreData] {
        return try await client.request(path: "stores")
    }

    func registerStore(_ request: StoreRegisterRequest) async throws -> StoreData {
        return try await client.request(
            path: "stores/register",
            method: "POST",
            body: request,
            authenticated: true
        )
    }
}
