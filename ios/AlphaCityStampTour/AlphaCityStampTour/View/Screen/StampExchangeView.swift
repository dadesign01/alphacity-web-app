//
//  StampExchangeView.swift
//  AlphaCityStampTour
//

import SwiftUI

private struct ExchangeCouponData: Identifiable {
    let id: Int
    let name: String
    let benefit: String
    let requiredStamps: Int
    let validUntil: String
}

private let mockExchangeCoupons: [ExchangeCouponData] = [
    ExchangeCouponData(id: 1, name: "메타 카페 음료 할인", benefit: "30% 할인", requiredStamps: 3, validUntil: "2026.03.31까지 사용 가능"),
    ExchangeCouponData(id: 2, name: "푸드 코트 식사 할인권", benefit: "5,000원 할인", requiredStamps: 6, validUntil: "2026.03.31까지 사용 가능"),
    ExchangeCouponData(id: 3, name: "VR 체험 무료 이용권", benefit: "1회 무료 이용", requiredStamps: 10, validUntil: "2026.03.31까지 사용 가능"),
    ExchangeCouponData(id: 4, name: "기념품샵 쇼핑 할인 쿠폰", benefit: "10,000원 할인", requiredStamps: 10, validUntil: "2026.03.31까지 사용 가능"),
]

private let userStampCount = 5

struct StampExchangeView: View {
    var onBackTapped: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            // === Header ===
            HStack(spacing: 24) {
                Button(action: onBackTapped) {
                    Image("IconBackArrow")
                        .renderingMode(.original)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 26)
                }
                Text("쿠폰 교환")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .frame(height: 50)

            Divider()
                .background(Color(hex: "E2E2E2"))

            ScrollView {
                VStack(spacing: 0) {
                    // === Blue Info Card ===
                    ZStack {
                        RoundedRectangle(cornerRadius: 22)
                            .fill(Color(hex: "EDF7FF"))
                            .frame(height: 185)

                        VStack(alignment: .leading, spacing: 4) {
                            Text("나의 보유 스탬프")
                                .font(AppFont.semibold(18))
                                .foregroundColor(Color(hex: "121212"))
                            Text("스탬프를 사용하여 다양한 혜택 쿠폰으로 교환하세요.\n교환 완료 후, 사용된 스탬프는 보유 수량에서 자동 차감됩니다.")
                                .font(AppFont.semibold(13))
                                .foregroundColor(Color(hex: "3D608D"))
                            Spacer()
                        }
                        .padding(20)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .frame(height: 185)

                        Text("\(userStampCount)개")
                            .font(AppFont.bold(35))
                            .foregroundColor(AppColor.primary)
                            .padding(.trailing, 20)
                            .padding(.bottom, 20)
                            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
                    }
                    .frame(height: 185)
                    .padding(.horizontal, 20)
                    .padding(.top, 30)

                    // === Section Title ===
                    Text("교환 가능한 쿠폰")
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 20)
                        .padding(.top, 24)

                    // === Coupon Exchange List ===
                    VStack(spacing: 0) {
                        ForEach(mockExchangeCoupons) { coupon in
                            ExchangeCouponCard(coupon: coupon, userStamps: userStampCount)

                            if coupon.id != mockExchangeCoupons.last?.id {
                                // Dashed divider (approximation)
                                Rectangle()
                                    .fill(Color.clear)
                                    .frame(height: 1)
                                    .overlay(
                                        GeometryReader { geo in
                                            Path { path in
                                                let y = geo.size.height / 2
                                                var x: CGFloat = 0
                                                while x < geo.size.width {
                                                    path.move(to: CGPoint(x: x, y: y))
                                                    path.addLine(to: CGPoint(x: x + 2, y: y))
                                                    x += 6
                                                }
                                            }
                                            .stroke(Color(hex: "EDEDED"), lineWidth: 1)
                                        }
                                    )
                                    .padding(.horizontal, 43)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 25)
                            .fill(Color.white)
                    )
                    .padding(.horizontal, 20)

                    // Footer
                    Text("© 2026 Alpha Stamp. All rights reserved.")
                        .font(AppFont.regular(10))
                        .foregroundColor(Color(hex: "AFBFCC"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                }
            }
        }
        .background(Color.white)
    }
}

// MARK: - Exchange Coupon Card

private struct ExchangeCouponCard: View {
    let coupon: ExchangeCouponData
    let userStamps: Int

    private var canExchange: Bool {
        userStamps >= coupon.requiredStamps
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 20) {
                // Thumbnail
                ZStack {
                    Circle()
                        .fill(Color(hex: "F8F8F8"))
                        .frame(width: 85, height: 85)
                    Image("IconCoupon")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 52, height: 52)
                }

                // Info
                VStack(alignment: .leading, spacing: 2) {
                    Text(coupon.name)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                    Text(coupon.benefit)
                        .font(AppFont.bold(28))
                        .foregroundColor(AppColor.primary)

                    // Validity + required stamps
                    Text(coupon.validUntil)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))

                    HStack(spacing: 0) {
                        Text("필요 스탬프 : \(coupon.requiredStamps)개")
                            .font(AppFont.medium(12))
                            .foregroundColor(AppColor.primary)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(
                        RoundedRectangle(cornerRadius: 7)
                            .fill(Color(hex: "EDF7FF"))
                    )
                }

                Spacer()
            }
            .padding(.vertical, 16)

            // Exchange button
            Button(action: {}) {
                Text(canExchange ? "교환하기" : "스탬프가 부족해요")
                    .font(AppFont.semibold(16))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(
                        RoundedRectangle(cornerRadius: 13)
                            .fill(
                                canExchange
                                ? LinearGradient(
                                    colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                                : LinearGradient(
                                    colors: [Color(hex: "8F8F8F"), Color(hex: "8F8F8F")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    )
            }
            .padding(.bottom, 16)
        }
    }
}

#Preview {
    StampExchangeView(onBackTapped: {})
}
