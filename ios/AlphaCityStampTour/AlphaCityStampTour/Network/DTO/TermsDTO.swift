//
//  TermsDTO.swift
//  AlphaCityStampTour
//

import Foundation

struct TermData: Decodable, Identifiable {
    let id: Int
    let type: String
    let title: String
    let content: String
    let version: String
    let isActive: Bool
}
