//
//  StampView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct StampView: View {
    @StateObject private var viewModel = StampViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            StampHeaderView()

            ScrollView {
                VStack(spacing: 0) {
                    // Title Section - 둘 다 left 정렬
                    VStack(alignment: .leading, spacing: 8) {
                        Text("스탬프 컬렉션")
                            .font(AppFont.bold(24))
                            .foregroundColor(Color(hex: "121212"))
                            .tracking(-0.48)

                        Text("축제 현장에서 미션에 참여해 스탬프를 획득해보세요.")
                            .font(AppFont.regular(14))
                            .foregroundColor(Color(hex: "121212"))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 20)

                    // Progress Card
                    StampProgressCardView(
                        progress: viewModel.progress,
                        percentText: viewModel.percentText
                    )
                    .padding(.top, 20)
                    .padding(.horizontal, 20)

                    // Reward Button
                    RewardButtonView()
                        .padding(.top, 12)
                        .padding(.horizontal, 20)

                    // Stamp Grid Section (light blue background)
                    VStack(spacing: 0) {
                        Text("스탬프 현황")
                            .font(AppFont.semibold(18))
                            .foregroundColor(Color(hex: "121212"))
                            .tracking(-0.36)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 20)
                            .padding(.top, 24)

                        // Stamp Grid
                        StampGridView(
                            stamps: viewModel.stamps,
                            collectedStampIds: viewModel.collectedStampIds
                        )
                        .padding(.top, 16)
                        .padding(.horizontal, 20)
                        .padding(.bottom, 32)

                        // Footer
                        Text("© 2026 Alpha Stamp. All rights reserved.")
                            .font(AppFont.regular(10))
                            .foregroundColor(Color(hex: "AFBFCC"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                    }
                    .background(Color(hex: "EDF7FF"))
                    .padding(.top, 20)
                }
            }
        }
        .background(Color.white)
        .onAppear {
            viewModel.fetchStampData()
        }
    }
}

// MARK: - Header

private struct StampHeaderView: View {
    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8) {
                Image("HeaderLogo")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 40, height: 40)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                Text("알파스탬프")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                    .tracking(-0.36)

                Spacer()

                Image("IconProfile")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 40, height: 40)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(Color(hex: "EBEBEB"), lineWidth: 1)
                    )
            }
            .padding(.horizontal, 15)
            .padding(.vertical, 12)

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))
        }
        .background(Color.white)
    }
}

// MARK: - Progress Card (반원 데코레이션 포함)

private struct StampProgressCardView: View {
    let progress: Float
    let percentText: String

    var body: some View {
        ZStack {
            // 배경 + 반원 데코레이션
            RoundedRectangle(cornerRadius: 22)
                .fill(Color(hex: "EDF7FF"))

            // 반원 데코레이션 (Figma: 두 개의 큰 원, opacity 0.27, gradient)
            Canvas { context, size in
                // Figma 기준: 카드 362x185, 원 619x740.91
                let scaleX = size.width / 362.0
                let scaleY = size.height / 185.0

                // 왼쪽 하단 원 (Figma: x=-204, y=87.49, 619x740.91)
                let c1Rect = CGRect(
                    x: -204 * scaleX,
                    y: 87.49 * scaleY,
                    width: 619 * scaleX,
                    height: 740.91 * scaleY
                )
                let leftCircle = Path(ellipseIn: c1Rect)
                context.opacity = 0.27
                context.fill(leftCircle, with: .linearGradient(
                    Gradient(stops: [
                        .init(color: Color(hex: "2563EB").opacity(0), location: 0.15),
                        .init(color: Color(hex: "2563EB"), location: 0.88),
                    ]),
                    startPoint: CGPoint(x: c1Rect.minX + c1Rect.width * 0.3, y: c1Rect.maxY),
                    endPoint: CGPoint(x: c1Rect.minX + c1Rect.width * 0.7, y: c1Rect.minY)
                ))

                // 오른쪽 상단 원 (Figma: x=42, y=-98.42, 619x740.91)
                let c2Rect = CGRect(
                    x: 42 * scaleX,
                    y: -98.42 * scaleY,
                    width: 619 * scaleX,
                    height: 740.91 * scaleY
                )
                let rightCircle = Path(ellipseIn: c2Rect)
                context.fill(rightCircle, with: .linearGradient(
                    Gradient(stops: [
                        .init(color: Color(hex: "2563EB").opacity(0), location: 0.59),
                        .init(color: Color(hex: "2563EB"), location: 0.75),
                    ]),
                    startPoint: CGPoint(x: c2Rect.minX, y: c2Rect.midY),
                    endPoint: CGPoint(x: c2Rect.maxX, y: c2Rect.minY + c2Rect.height * 0.2)
                ))
                context.opacity = 1.0
            }
            .clipShape(RoundedRectangle(cornerRadius: 22))

            // 콘텐츠
            VStack(spacing: 0) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text("나의 스탬프 진행률")
                            .font(AppFont.semibold(18))
                            .foregroundColor(Color(hex: "121212"))
                            .tracking(-0.36)

                        Text("차곡차곡 모아 다양한 리워드를 만나보세요!")
                            .font(AppFont.semibold(13))
                            .foregroundColor(Color(hex: "3D608D"))
                            .tracking(-0.26)
                    }

                    Spacer()

                    Text(percentText)
                        .font(AppFont.bold(35))
                        .foregroundColor(AppColor.primary)
                        .tracking(-0.7)
                }

                Spacer().frame(height: 28)

                // Progress bar with character
                GeometryReader { geo in
                    let barWidth = geo.size.width
                    let filledWidth = barWidth * CGFloat(progress)

                    ZStack(alignment: .bottomLeading) {
                        // Bar track
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 100)
                                .fill(Color.white)
                                .frame(height: 16)

                            RoundedRectangle(cornerRadius: 100)
                                .fill(Color(hex: "4C27D0"))
                                .frame(width: max(filledWidth, 0), height: 16)
                        }
                        .frame(maxWidth: .infinity, alignment: .bottom)

                        // Character on bar
                        Image("StampCharacterSmall")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 36, height: 36)
                            .offset(x: max(filledWidth - 18, 0), y: -20)
                    }
                }
                .frame(height: 47)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 24)
        }
    }
}

// MARK: - Reward Button (피그마 아이콘)

private struct RewardButtonView: View {
    var body: some View {
        HStack(spacing: 12) {
            Image("IconRewardExchange")
                .resizable()
                .scaledToFit()
                .frame(width: 18, height: 18)

            Text("스탬프 리워드 교환하러 가기")
                .font(AppFont.medium(16))
                .foregroundColor(.white)
                .tracking(-0.32)

            Spacer()

            Text(">")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.white)
        }
        .padding(.horizontal, 16)
        .frame(height: 56)
        .background(
            LinearGradient(
                colors: [
                    Color(hex: "6092FF"),
                    Color(hex: "2563EB"),
                    Color(hex: "1551D3"),
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// MARK: - Stamp Grid (트로피/메달 구분, 자물쇠)

private struct StampGridView: View {
    let stamps: [StampData]
    let collectedStampIds: Set<Int>

    private let columns = Array(repeating: GridItem(.flexible(), spacing: 19), count: 3)

    var body: some View {
        LazyVGrid(columns: columns, spacing: 12) {
            ForEach(stamps) { stamp in
                StampSlotView(
                    stamp: stamp,
                    isCollected: collectedStampIds.contains(stamp.id)
                )
            }
        }
    }
}

private struct StampSlotView: View {
    let stamp: StampData
    let isCollected: Bool

    /// conditionType에 따른 fallback 이미지
    private var fallbackImage: String {
        switch stamp.conditionType {
        case "mission_complete", "place_visit":
            return "StampTrophy"
        case "event_participate", "quiz_correct":
            return "StampMedal"
        default:
            return "StampTrophy"
        }
    }

    var body: some View {
        VStack(spacing: 8) {
            ZStack {
                RoundedRectangle(cornerRadius: 35)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.05), radius: 7, x: 0, y: 1)
                    .aspectRatio(1, contentMode: .fit)

                if isCollected {
                    // 수집된 스탬프: admin imageUrl 우선, 없으면 conditionType별 fallback
                    if let imageUrl = stamp.imageUrl, !imageUrl.isEmpty {
                        AsyncImage(url: URL(string: imageUrl.hasPrefix("http") ? imageUrl : APIClient.serverURL + imageUrl)) { image in
                            image.resizable().scaledToFit()
                        } placeholder: {
                            Image(fallbackImage)
                                .resizable()
                                .scaledToFit()
                        }
                        .padding(18)
                    } else {
                        Image(fallbackImage)
                            .resizable()
                            .scaledToFit()
                            .padding(18)
                    }
                } else {
                    // 미수집 스탬프: 자물쇠 + MISSION CLEAR (Figma: opacity 0.28)
                    VStack(spacing: 4) {
                        Image("StampLocked")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 29, height: 40)

                        Text("MISSION CLEAR")
                            .font(AppFont.medium(8))
                            .foregroundColor(Color(hex: "121212").opacity(0.6))
                    }
                    .opacity(0.28)
                }
            }

            Text(stamp.name)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "121212"))
                .lineLimit(1)
        }
    }
}

#Preview {
    StampView()
}
