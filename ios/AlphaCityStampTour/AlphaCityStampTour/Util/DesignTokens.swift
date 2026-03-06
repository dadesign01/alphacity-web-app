//
//  DesignTokens.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI
import CoreText

// MARK: - Font Registration

enum FontRegistration {
    static func registerFonts() {
        let fontNames = [
            "Pretendard-Black",
            "Pretendard-ExtraBold",
            "Pretendard-Bold",
            "Pretendard-SemiBold",
            "Pretendard-Medium",
            "Pretendard-Regular",
        ]
        for name in fontNames {
            if let url = Bundle.main.url(forResource: name, withExtension: "otf", subdirectory: "Resources/Fonts") {
                CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
            } else if let url = Bundle.main.url(forResource: name, withExtension: "otf", subdirectory: "Fonts") {
                CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
            } else if let url = Bundle.main.url(forResource: name, withExtension: "otf") {
                CTFontManagerRegisterFontsForURL(url as CFURL, .process, nil)
            }
        }
    }
}

// MARK: - Colors

enum AppColor {
    static let primary = Color(hex: "2563EB")
    static let primaryLight = Color(hex: "60BDFF")
    static let background = Color(hex: "F9F9F9")
    static let textDark = Color(hex: "3A3E47")
    static let textGray = Color(hex: "5F687C")
    static let splashGradientStart = Color(hex: "60BDFF")
    static let splashGradientEnd = Color(hex: "94E8FF").opacity(0)
}

// MARK: - Pretendard Font

enum AppFont {
    static func black(_ size: CGFloat) -> Font {
        .custom("Pretendard-Black", size: size)
    }

    static func extraBold(_ size: CGFloat) -> Font {
        .custom("Pretendard-ExtraBold", size: size)
    }

    static func bold(_ size: CGFloat) -> Font {
        .custom("Pretendard-Bold", size: size)
    }

    static func semibold(_ size: CGFloat) -> Font {
        .custom("Pretendard-SemiBold", size: size)
    }

    static func medium(_ size: CGFloat) -> Font {
        .custom("Pretendard-Medium", size: size)
    }

    static func regular(_ size: CGFloat) -> Font {
        .custom("Pretendard-Regular", size: size)
    }
}

// MARK: - Color Extension

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8:
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}
