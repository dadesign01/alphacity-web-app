//
//  SplashBadge.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI

struct SplashBadge: View {
    let text: String

    var body: some View {
        VStack(spacing: 0) {
            // 라운드 사각형 배지
            Text(text)
                .font(AppFont.bold(18))
                .tracking(-0.54)
                .foregroundStyle(.white)
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(AppColor.primary)
                .clipShape(RoundedRectangle(cornerRadius: 14))

            // 삼각형 꼬리
            Triangle()
                .fill(AppColor.primary)
                .frame(width: 16, height: 12)
                .offset(y: -1)
        }
    }
}

private struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.closeSubpath()
        return path
    }
}

#Preview {
    SplashBadge(text: "디지털 혁신거점")
}
