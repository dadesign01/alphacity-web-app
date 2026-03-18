//
//  SplashView.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI

struct SplashView: View {
    var onStartTapped: () -> Void = {}

    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // 배경색
                AppColor.background
                    .ignoresSafeArea()

                // 상단 그라데이션 (하늘색 → 투명)
                VStack {
                    LinearGradient(
                        colors: [
                            AppColor.splashGradientStart,
                            AppColor.splashGradientEnd,
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(height: geometry.size.height * 0.45)

                    Spacer()
                }
                .ignoresSafeArea()

                // 하단 배경 이미지 (도시 풍경)
                VStack {
                    Spacer()
                    Image("SplashBgBottom")
                        .resizable()
                        .scaledToFill()
                        .frame(width: geometry.size.width)
                        .clipped()
                }
                .ignoresSafeArea()

                // 왼쪽 구름
                Image("SplashCloudRight")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 180, height: 234)
                    .position(x: -20, y: geometry.size.height * 0.16)

                // 오른쪽 구름
                Image("SplashCloudRight")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 180, height: 234)
                    .position(x: geometry.size.width + 20, y: geometry.size.height * 0.45)

                // 메인 콘텐츠
                VStack(spacing: 0) {
                    Spacer()
                        .frame(height: geometry.size.height * 0.14)

                    // "디지털 혁신거점" 배지
                    SplashBadge(text: "디지털 혁신거점")

                    Spacer()
                        .frame(height: 20)

                    // 메인 타이틀
                    Text("수성알파시티\n스탬프 투어")
                        .font(AppFont.black(45))
                        .tracking(-0.45)
                        .lineSpacing(6)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(AppColor.textDark)

                    Spacer()
                        .frame(height: 16)

                    // 서브 타이틀
                    Text("뚜비와 함께 알파시티 스탬프 나들이를 떠나요!")
                        .font(AppFont.medium(14))
                        .tracking(-0.42)
                        .foregroundStyle(AppColor.textGray)

                    Spacer()
                }

                // 캐릭터 (뚜비) + 하단 "투어 시작하기" 버튼
                VStack(spacing: 24) {
                    Spacer()

                    GIFImageView(name: "alphacity_splash")
                        .frame(width: 320, height: 320)

                    Button(action: onStartTapped) {
                        Text("투어 시작하기")
                            .font(AppFont.semibold(20))
                            .tracking(-0.6)
                            .foregroundStyle(.white)
                            .frame(width: 309, height: 65)
                            .background(AppColor.primary)
                            .clipShape(RoundedRectangle(cornerRadius: 19))
                    }
                    .padding(.bottom, 60)
                }
            }
        }
    }
}

#Preview {
    SplashView()
}
