//
//  FlexibleDouble.swift
//  AlphaCityStampTour
//
//  서버가 좌표(latitude/longitude)를 엔드포인트에 따라 문자열("35.8693") 또는
//  숫자(35.8693)로 반환하기 때문에, 두 형식을 모두 Double?로 안전하게 디코딩한다.
//  (Android는 Kotlinx Serialization의 lenient 파싱으로 이미 처리됨)
//

import Foundation

@propertyWrapper
struct FlexibleDouble: Decodable {
    var wrappedValue: Double?

    init(wrappedValue: Double?) {
        self.wrappedValue = wrappedValue
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let value = try? container.decode(Double.self) {
            wrappedValue = value
        } else if let string = try? container.decode(String.self) {
            wrappedValue = Double(string)
        } else {
            wrappedValue = nil
        }
    }
}

extension KeyedDecodingContainer {
    // 키가 아예 없거나 null이어도 nil로 처리 (옵셔널 좌표 필드 대응)
    func decode(_ type: FlexibleDouble.Type, forKey key: Key) throws -> FlexibleDouble {
        try decodeIfPresent(FlexibleDouble.self, forKey: key) ?? FlexibleDouble(wrappedValue: nil)
    }
}
