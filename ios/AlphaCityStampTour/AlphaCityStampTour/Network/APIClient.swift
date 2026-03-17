//
//  APIClient.swift
//  AlphaCityStampTour
//

import Foundation

final class APIClient {
    static let shared = APIClient()

    private let baseURL = "http://223.130.141.53:1111/api/v1"
    let baseURLString = "http://223.130.141.53:1111/api/v1"
    static let serverURL = "http://223.130.141.53:1111"

    private let decoder: JSONDecoder = {
        let decoder = JSONDecoder()
        return decoder
    }()

    private let encoder: JSONEncoder = {
        let encoder = JSONEncoder()
        return encoder
    }()

    private init() {}

    func request<T: Decodable>(
        path: String,
        method: String = "GET",
        body: (any Encodable)? = nil,
        authenticated: Bool = false
    ) async throws -> T {
        guard let url = URL(string: "\(baseURL)/\(path)") else {
            throw APIError.invalidInput("잘못된 URL입니다")
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if authenticated, let token = TokenManager.shared.accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        if let body = body {
            request.httpBody = try encoder.encode(body)
        }

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.serverError("서버 응답을 받을 수 없습니다")
        }

        let apiResponse = try decoder.decode(APIResponse<T>.self, from: data)

        if apiResponse.success, let resultData = apiResponse.data {
            return resultData
        } else {
            let message = apiResponse.error?.message ?? "요청에 실패했습니다"
            switch httpResponse.statusCode {
            case 401:
                throw APIError.unauthorized(message)
            case 400:
                throw APIError.invalidInput(message)
            default:
                throw APIError.serverError(message)
            }
        }
    }
}
