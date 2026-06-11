//
//  APIResponse.swift
//  AlphaCityStampTour
//

import Foundation

struct APIResponse<T: Decodable>: Decodable {
    let success: Bool
    let data: T?
    let message: String?
    let error: APIErrorDetail?
}

struct APIErrorDetail: Decodable {
    let code: String
    let message: String
}

struct UploadData: Decodable {
    let imageUrl: String
}
