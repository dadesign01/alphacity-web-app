//
//  APIError.swift
//  AlphaCityStampTour
//

import Foundation

enum APIError: LocalizedError {
    case invalidInput(String)
    case unauthorized(String)
    case serverError(String)
    case networkError(Error)
    case decodingError

    var errorDescription: String? {
        switch self {
        case .invalidInput(let msg): return msg
        case .unauthorized(let msg): return msg
        case .serverError(let msg): return msg
        case .networkError(let error): return error.localizedDescription
        case .decodingError: return "데이터 처리 중 오류가 발생했습니다"
        }
    }
}
