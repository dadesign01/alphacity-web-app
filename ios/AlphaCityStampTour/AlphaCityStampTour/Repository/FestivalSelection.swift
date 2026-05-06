//
//  FestivalSelection.swift
//  AlphaCityStampTour
//
//  앱 전역 "선택된 축제" 상태.
//  - selectedFestivalId == nil → 전체 축제
//  - selectedFestivalId == id  → 해당 축제 필터링
//

import Foundation
import Combine

final class FestivalSelection: ObservableObject {
    static let shared = FestivalSelection()

    @Published var selectedFestivalId: Int? = nil

    private init() {}

    func select(_ id: Int?) {
        selectedFestivalId = id
    }
}
