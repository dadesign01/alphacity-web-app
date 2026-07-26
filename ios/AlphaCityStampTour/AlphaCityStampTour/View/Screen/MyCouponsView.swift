//
//  MyCouponsView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct MyCouponsView: View {
    var onBackTapped: () -> Void
    @StateObject private var viewModel = MyCouponsViewModel()
    @State private var selectedCoupon: MyCouponData? = nil

    // 파란 보유쿠폰 카드 + CTA + "내 쿠폰" 타이틀 (빈 상태/목록 상태 공용)
    @ViewBuilder
    private var couponsHeader: some View {
        // === Blue Info Card ===
        ZStack {
            RoundedRectangle(cornerRadius: 22)
                .fill(Color(hex: "EDF7FF"))
                .frame(height: 185)

            VStack(alignment: .leading, spacing: 4) {
                Text("나의 보유 쿠폰")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Text("사용 시에는 해당 장소에서 고유 코드를 제시해주세요.")
                    .font(AppFont.semibold(13))
                    .foregroundColor(Color(hex: "3D608D"))
                Spacer()
            }
            .padding(20)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: 185)

            Text("\(viewModel.availableCoupons.count)개")
                .font(AppFont.bold(35))
                .foregroundColor(AppColor.primary)
                .padding(.trailing, 20)
                .padding(.bottom, 20)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomTrailing)
        }
        .frame(height: 185)
        .padding(.horizontal, 20)
        .padding(.top, 30)

        // === CTA Button ===
        Button(action: onBackTapped) {
            HStack {
                Spacer()
                Text("추가 쿠폰 받으러 가기")
                    .font(AppFont.medium(16))
                    .foregroundColor(.white)
                Text(">")
                    .font(AppFont.medium(16))
                    .foregroundColor(.white)
                Spacer()
            }
            .frame(height: 56)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .fill(
                        LinearGradient(
                            colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
            )
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)

        // === Section Title ===
        Text("내 쿠폰")
            .font(AppFont.semibold(18))
            .foregroundColor(Color(hex: "121212"))
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 20)
            .padding(.top, 24)
    }

    var body: some View {
        ZStack {
            Color.white.ignoresSafeArea()

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
                    Text("쿠폰 보관함")
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                    Spacer()
                }
                .padding(.horizontal, 20)
                .frame(height: 50)

                Divider()
                    .background(Color(hex: "E2E2E2"))

                if viewModel.availableCoupons.isEmpty {
                    // 빈 상태: 스크롤 없이 안내 문구를 콘텐츠 영역 세로 중앙에 배치
                    VStack(spacing: 0) {
                        couponsHeader

                        Spacer()

                        Text("사용 가능한 쿠폰이 없습니다.")
                            .font(AppFont.medium(14))
                            .foregroundColor(Color(hex: "9CA3AF"))
                            .frame(maxWidth: .infinity)

                        Spacer()
                    }
                } else {
                    ScrollView {
                        VStack(spacing: 0) {
                            couponsHeader

                            // === Coupon List ===
                            VStack(spacing: 12) {
                                ForEach(viewModel.availableCoupons) { coupon in
                                    MyCouponCard(coupon: coupon) {
                                        selectedCoupon = coupon
                                    }
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.top, 16)
                        }
                    }
                }

                // === Footer (하단 고정) ===
                Text("2026 OLLYMOA. All rights reserved.")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "999999"))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 24)
                    .background(Color(hex: "F9F9F9"))
            }

            // === Bottom Sheet Overlay ===
            if let coupon = selectedCoupon {
                MyCouponDetailSheet(
                    coupon: coupon,
                    onDismiss: { selectedCoupon = nil },
                    onUseCoupon: {
                        viewModel.useCoupon(userCouponId: coupon.id)
                        selectedCoupon = nil
                    }
                )
            }
        }
        .onAppear {
            viewModel.fetchMyCoupons()
        }
        .alert("알림", isPresented: $viewModel.useSuccess) {
            Button("확인", role: .cancel) {}
        } message: {
            Text("쿠폰이 사용되었습니다.")
        }
        .alert("오류", isPresented: Binding(get: { viewModel.useError != nil }, set: { if !$0 { viewModel.useError = nil } })) {
            Button("확인", role: .cancel) { viewModel.useError = nil }
        } message: {
            Text(viewModel.useError ?? "")
        }
    }
}

// MARK: - Coupon Card

private struct MyCouponCard: View {
    let coupon: MyCouponData
    let onTap: () -> Void

    private var validUntilText: String {
        let raw = coupon.validUntil
        if raw.count >= 10 {
            let dateStr = String(raw.prefix(10))
            let parts = dateStr.split(separator: "-")
            if parts.count == 3 {
                return "\(parts[0]).\(parts[1]).\(parts[2])까지 사용 가능"
            }
        }
        return raw
    }

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 0) {
                ZStack {
                    Circle()
                        .fill(Color(hex: "F8F8F8"))
                        .frame(width: 85, height: 85)

                    if let imageUrl = coupon.imageUrl, !imageUrl.isEmpty {
                        AsyncImage(url: URL(string: imageUrl.hasPrefix("http") ? imageUrl : APIClient.serverURL + imageUrl)) { image in
                            image.resizable().scaledToFit()
                        } placeholder: {
                            Image("IconCoupon")
                                .resizable()
                                .scaledToFit()
                        }
                        .frame(width: 52, height: 52)
                    } else {
                        Image("IconCoupon")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 52, height: 52)
                    }
                }
                .padding(.leading, 16)

                VStack(alignment: .leading, spacing: 2) {
                    Text(coupon.name)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                        .lineLimit(1)

                    if let desc = coupon.description, !desc.isEmpty {
                        Text(desc)
                            .font(AppFont.bold(28))
                            .foregroundColor(AppColor.primary)
                    }

                    Text(validUntilText)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            RoundedRectangle(cornerRadius: 7)
                                .fill(Color(hex: "F8F8F8"))
                        )
                }
                .padding(.leading, 18)

                Spacer(minLength: 0)

                ZStack {
                    MyCouponDecoShape()
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "4B8BF5"), AppColor.primary],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )

                    Image(systemName: "qrcode")
                        .font(.system(size: 28, weight: .medium))
                        .foregroundColor(.white)
                }
                .frame(width: 82, height: 134)
            }
            .frame(height: 134)
            .background(
                RoundedRectangle(cornerRadius: 25)
                    .fill(Color.white)
            )
            .clipShape(RoundedRectangle(cornerRadius: 25))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Coupon Deco Shape

private struct MyCouponDecoShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let w = rect.width
        let h = rect.height

        path.move(to: CGPoint(x: w * 0.35, y: 0))
        path.addLine(to: CGPoint(x: w, y: 0))
        path.addLine(to: CGPoint(x: w, y: h))
        path.addLine(to: CGPoint(x: w * 0.35, y: h))

        path.addCurve(
            to: CGPoint(x: w * 0.15, y: h * 0.5),
            control1: CGPoint(x: w * 0.0, y: h * 0.85),
            control2: CGPoint(x: w * 0.0, y: h * 0.65)
        )
        path.addCurve(
            to: CGPoint(x: w * 0.35, y: 0),
            control1: CGPoint(x: w * 0.3, y: h * 0.35),
            control2: CGPoint(x: w * 0.15, y: h * 0.15)
        )

        path.closeSubpath()
        return path
    }
}

// MARK: - Coupon Detail Bottom Sheet

private struct MyCouponDetailSheet: View {
    let coupon: MyCouponData
    let onDismiss: () -> Void
    let onUseCoupon: () -> Void

    var body: some View {
        Color.black.opacity(0.61)
            .ignoresSafeArea()
            .onTapGesture { onDismiss() }

        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 0) {
                // Handle
                RoundedRectangle(cornerRadius: 100)
                    .fill(Color(hex: "D9D9D9"))
                    .frame(width: 65, height: 7)
                    .padding(.top, 19)

                // Store name
                Text(coupon.storeName ?? coupon.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .padding(.top, 24)

                // Benefit
                Text(coupon.description ?? coupon.name)
                    .font(AppFont.bold(28))
                    .foregroundColor(Color(hex: "121212"))
                    .padding(.top, 4)

                // Code box
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color(hex: "EDF7FF"))
                    .frame(height: 86)
                    .overlay(
                        Text(coupon.code)
                            .font(AppFont.bold(42))
                            .foregroundColor(AppColor.primary)
                    )
                    .padding(.horizontal, 20)
                    .padding(.top, 16)

                // Notice
                Text("이 화면을 쿠폰 사용처 관계자에게 보여주세요.")
                    .font(AppFont.medium(12))
                    .foregroundColor(Color(hex: "8F8F8F"))
                    .padding(.top, 16)

                // Use button
                Button(action: onUseCoupon) {
                    Text("쿠폰 사용하기")
                        .font(AppFont.semibold(16))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(
                            RoundedRectangle(cornerRadius: 13)
                                .fill(
                                    LinearGradient(
                                        colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                        )
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)

                // Caution section
                ScrollView {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 4) {
                            Image(systemName: "exclamationmark.circle")
                                .font(.system(size: 14))
                                .foregroundColor(Color(hex: "8F8F8F"))
                            Text("쿠폰 사용 시 주의해주세요.")
                                .font(AppFont.medium(10))
                                .foregroundColor(Color(hex: "8F8F8F"))
                        }

                        Text("""
                        - 본 쿠폰은 유효기간 내에만 사용 가능합니다.
                        - 쿠폰은 1회 사용 원칙이며, 재사용 불가합니다.
                        - 현금 교환 및 환불은 불가합니다.
                        - 타 쿠폰과 중복 사용이 불가할 수 있습니다.
                        - 알파시티 축제 고객센터 : 053-123-4567
                        - 쿠폰 사용 조건은 사용처 사정에 따라 변경될 수 있습니다.
                        """)
                            .font(AppFont.medium(10))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .lineSpacing(4)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                    .padding(.bottom, 30)
                }
                .frame(maxHeight: 200)
                .background(Color(hex: "F8F8F8"))
            }
            .background(Color.white)
            .clipShape(
                UnevenRoundedRectangle(
                    topLeadingRadius: 40,
                    bottomLeadingRadius: 0,
                    bottomTrailingRadius: 0,
                    topTrailingRadius: 40
                )
            )
        }
        .ignoresSafeArea(.container, edges: .bottom)
    }
}

#Preview {
    MyCouponsView(onBackTapped: {})
}
