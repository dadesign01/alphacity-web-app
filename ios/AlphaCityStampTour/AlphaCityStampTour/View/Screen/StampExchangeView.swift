//
//  StampExchangeView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct StampExchangeView: View {
    var onBackTapped: () -> Void
    var onNavigateToCoupons: () -> Void = {}
    @ObservedObject var viewModel: StampViewModel

    @State private var showConfirmCoupon: CouponData? = nil

    private var exchangeableCouponCount: Int {
        viewModel.coupons.filter { viewModel.userStampCount >= $0.requiredStamps }.count
    }

    var body: some View {
        ZStack {
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
                        // === 보유 스탬프 정보 카드 ===
                        ZStack {
                            RoundedRectangle(cornerRadius: 22)
                                .fill(Color(hex: "EDF7FF"))

                            Canvas { context, size in
                                let scaleX = size.width / 362.0
                                let scaleY = size.height / 140.0

                                let c1Rect = CGRect(
                                    x: -204 * scaleX, y: 60 * scaleY,
                                    width: 619 * scaleX, height: 740.91 * scaleY
                                )
                                context.opacity = 0.15
                                context.fill(Path(ellipseIn: c1Rect), with: .linearGradient(
                                    Gradient(stops: [
                                        .init(color: Color(hex: "2563EB").opacity(0), location: 0.15),
                                        .init(color: Color(hex: "2563EB"), location: 0.88),
                                    ]),
                                    startPoint: CGPoint(x: 0, y: size.height),
                                    endPoint: CGPoint(x: size.width * 0.5, y: 0)
                                ))

                                let c2Rect = CGRect(
                                    x: 42 * scaleX, y: -80 * scaleY,
                                    width: 619 * scaleX, height: 740.91 * scaleY
                                )
                                context.fill(Path(ellipseIn: c2Rect), with: .linearGradient(
                                    Gradient(stops: [
                                        .init(color: Color(hex: "2563EB").opacity(0), location: 0.59),
                                        .init(color: Color(hex: "2563EB"), location: 0.75),
                                    ]),
                                    startPoint: CGPoint(x: 0, y: size.height * 0.5),
                                    endPoint: CGPoint(x: size.width, y: 0)
                                ))
                                context.opacity = 1.0
                            }
                            .clipShape(RoundedRectangle(cornerRadius: 22))

                            HStack(spacing: 16) {
                                ZStack {
                                    Circle()
                                        .fill(Color.white.opacity(0.7))
                                        .frame(width: 52, height: 52)
                                    Image("StampTrophy")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 30, height: 30)
                                }

                                VStack(alignment: .leading, spacing: 2) {
                                    Text("보유 스탬프")
                                        .font(AppFont.medium(13))
                                        .foregroundColor(Color(hex: "3D608D"))
                                    Text("\(viewModel.userStampCount)개")
                                        .font(AppFont.bold(28))
                                        .foregroundColor(AppColor.primary)
                                        .tracking(-0.56)
                                }

                                Spacer()

                                VStack(spacing: 2) {
                                    Text("교환 가능")
                                        .font(AppFont.medium(11))
                                        .foregroundColor(Color(hex: "3D608D"))
                                    Text("\(exchangeableCouponCount)개")
                                        .font(AppFont.bold(22))
                                        .foregroundColor(Color(hex: "16A34A"))
                                }
                            }
                            .padding(20)
                        }
                        .padding(.horizontal, 20)
                        .padding(.top, 24)

                        // === 안내 메시지 ===
                        HStack(alignment: .top, spacing: 8) {
                            Text("💡")
                                .font(.system(size: 14))
                            Text("스탬프를 사용하여 다양한 혜택 쿠폰으로 교환하세요.\n교환 후 스탬프는 차감됩니다.")
                                .font(AppFont.medium(13))
                                .foregroundColor(Color(hex: "3D608D"))
                                .lineSpacing(3)
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(hex: "F0F7FF"))
                        )
                        .padding(.horizontal, 20)
                        .padding(.top, 12)

                        // === Section Title ===
                        Text("교환 가능한 쿠폰")
                            .font(AppFont.semibold(18))
                            .foregroundColor(Color(hex: "121212"))
                            .tracking(-0.36)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 20)
                            .padding(.top, 24)

                        // === Coupon Exchange List ===
                        if viewModel.coupons.isEmpty {
                            Text("현재 교환 가능한 쿠폰이 없습니다.")
                                .font(AppFont.medium(14))
                                .foregroundColor(Color(hex: "9CA3AF"))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 40)
                        } else {
                            VStack(spacing: 0) {
                                ForEach(viewModel.coupons) { coupon in
                                    ExchangeCouponCardView(
                                        coupon: coupon,
                                        userStamps: viewModel.userStampCount,
                                        onExchangeTapped: { showConfirmCoupon = coupon }
                                    )

                                    if coupon.id != viewModel.coupons.last?.id {
                                        DashedDivider()
                                            .padding(.horizontal, 23)
                                    }
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.top, 12)
                        }

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

            // === 교환 확인 팝업 ===
            if let coupon = showConfirmCoupon {
                CouponConfirmPopup(
                    coupon: coupon,
                    isRedeeming: viewModel.isRedeeming,
                    onDismiss: { showConfirmCoupon = nil },
                    onConfirm: {
                        viewModel.redeemCoupon(couponId: coupon.id)
                        showConfirmCoupon = nil
                    }
                )
            }

            // === 교환 완료 팝업 ===
            if let success = viewModel.redeemSuccess {
                CouponSuccessPopup(
                    success: success,
                    onDismiss: { viewModel.clearRedeemSuccess() },
                    onNavigateToCoupons: {
                        viewModel.clearRedeemSuccess()
                        onNavigateToCoupons()
                    }
                )
            }

            // === 교환 실패 팝업 ===
            if let error = viewModel.redeemError {
                CouponErrorPopup(
                    message: error,
                    onDismiss: { viewModel.clearRedeemError() }
                )
            }
        }
        .onAppear {
            viewModel.fetchStampData()
        }
    }
}

// MARK: - Confirm Popup

private struct CouponConfirmPopup: View {
    let coupon: CouponData
    let isRedeeming: Bool
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    private var validUntilText: String {
        formatValidUntil(coupon.validUntil)
    }

    var body: some View {
        Color.black.opacity(0.4)
            .ignoresSafeArea()
            .onTapGesture { onDismiss() }

        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color(hex: "F3EAFF"))
                    .frame(width: 56, height: 56)
                Text("🎁")
                    .font(.system(size: 26))
            }

            Text("쿠폰 교환 확인")
                .font(AppFont.semibold(20))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 16)

            Text("스탬프를 사용하여 쿠폰을 발급하시겠습니까?")
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "9CA3AF"))
                .multilineTextAlignment(.center)
                .padding(.top, 8)

            // 쿠폰 정보 카드
            VStack(alignment: .leading, spacing: 8) {
                Text(coupon.name)
                    .font(AppFont.bold(16))
                    .foregroundColor(Color(hex: "121212"))

                if let desc = coupon.description, !desc.isEmpty {
                    PopupInfoRow(label: "할인 혜택", value: desc, valueColor: AppColor.primary)
                }
                PopupInfoRow(label: "유효기간", value: validUntilText)
                PopupInfoRow(label: "차감 스탬프", value: "\(coupon.requiredStamps)개", valueColor: AppColor.primary, valueBold: true)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(hex: "EDF7FF"))
            )
            .padding(.top, 20)

            // 버튼
            HStack(spacing: 12) {
                Button(action: onDismiss) {
                    Text("취소")
                        .font(AppFont.semibold(15))
                        .foregroundColor(Color(hex: "666666"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(hex: "F3F4F6"))
                        )
                }

                Button(action: onConfirm) {
                    Text("교환하기")
                        .font(AppFont.semibold(15))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(
                                    LinearGradient(
                                        colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                        )
                }
                .disabled(isRedeeming)
            }
            .padding(.top, 24)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: .black.opacity(0.15), radius: 20, x: 0, y: 8)
        )
        .padding(.horizontal, 32)
    }
}

// MARK: - Success Popup

private struct CouponSuccessPopup: View {
    let success: RedeemSuccess
    let onDismiss: () -> Void
    let onNavigateToCoupons: () -> Void

    private var validUntilText: String {
        formatValidUntil(success.validUntil)
    }

    var body: some View {
        Color.black.opacity(0.4)
            .ignoresSafeArea()
            .onTapGesture { onDismiss() }

        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color(hex: "ECFDF5"))
                    .frame(width: 56, height: 56)
                Text("✅")
                    .font(.system(size: 26))
            }

            Text("쿠폰 발급 완료!")
                .font(AppFont.semibold(20))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 16)

            Text("쿠폰이 보관함에 저장되었습니다")
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "9CA3AF"))
                .padding(.top, 8)

            VStack(alignment: .leading, spacing: 10) {
                Text(success.couponName)
                    .font(AppFont.bold(16))
                    .foregroundColor(Color(hex: "121212"))

                if let desc = success.couponDescription, !desc.isEmpty {
                    PopupInfoRow(label: "할인 혜택", value: desc, valueColor: AppColor.primary)
                }
                PopupInfoRow(label: "유효기간", value: validUntilText)
            }
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(hex: "EDF7FF"))
            )
            .padding(.top, 20)

            Button(action: onNavigateToCoupons) {
                Text("쿠폰 보관함으로 이동")
                    .font(AppFont.semibold(15))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(
                                LinearGradient(
                                    colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    )
            }
            .padding(.top, 24)

            Button(action: onDismiss) {
                Text("닫기")
                    .font(AppFont.semibold(15))
                    .foregroundColor(Color(hex: "666666"))
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "F3F4F6"))
                    )
            }
            .padding(.top, 10)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: .black.opacity(0.15), radius: 20, x: 0, y: 8)
        )
        .padding(.horizontal, 32)
    }
}

// MARK: - Error Popup

private struct CouponErrorPopup: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        Color.black.opacity(0.4)
            .ignoresSafeArea()
            .onTapGesture { onDismiss() }

        VStack(spacing: 0) {
            ZStack {
                Circle()
                    .fill(Color(hex: "FEF2F2"))
                    .frame(width: 56, height: 56)
                Text("❌")
                    .font(.system(size: 26))
            }

            Text("교환 실패")
                .font(AppFont.semibold(20))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 16)

            Text(message)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "9CA3AF"))
                .multilineTextAlignment(.center)
                .padding(.top, 8)

            Button(action: onDismiss) {
                Text("확인")
                    .font(AppFont.semibold(15))
                    .foregroundColor(Color(hex: "666666"))
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "F3F4F6"))
                    )
            }
            .padding(.top, 24)
        }
        .padding(24)
        .background(
            RoundedRectangle(cornerRadius: 22)
                .fill(Color.white)
                .shadow(color: .black.opacity(0.15), radius: 20, x: 0, y: 8)
        )
        .padding(.horizontal, 32)
    }
}

// MARK: - Info Row

private struct PopupInfoRow: View {
    let label: String
    let value: String
    var valueColor: Color = Color(hex: "3D608D")
    var valueBold: Bool = false

    var body: some View {
        HStack {
            Text(label)
                .font(AppFont.medium(13))
                .foregroundColor(Color(hex: "8F8F8F"))
            Spacer()
            Text(value)
                .font(valueBold ? AppFont.semibold(13) : AppFont.medium(13))
                .foregroundColor(valueColor)
        }
    }
}

// MARK: - Date Format Util

private func formatValidUntil(_ raw: String) -> String {
    if raw.count >= 10 {
        let dateStr = String(raw.prefix(10))
        let parts = dateStr.split(separator: "-")
        if parts.count == 3 {
            return "\(parts[0]).\(parts[1]).\(parts[2])"
        }
    }
    return raw
}

// MARK: - Exchange Coupon Card

private struct ExchangeCouponCardView: View {
    let coupon: CouponData
    let userStamps: Int
    var onExchangeTapped: () -> Void

    private var canExchange: Bool {
        userStamps >= coupon.requiredStamps
    }

    private var validUntilText: String {
        let raw = coupon.validUntil
        if raw.count >= 10 {
            let dateStr = String(raw.prefix(10))
            let parts = dateStr.split(separator: "-")
            if parts.count == 3 {
                return "\(parts[0]).\(parts[1]).\(parts[2])까지"
            }
        }
        return raw
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 20) {
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

                VStack(alignment: .leading, spacing: 2) {
                    Text(coupon.name)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))

                    if let desc = coupon.description, !desc.isEmpty {
                        Text(desc)
                            .font(AppFont.bold(22))
                            .foregroundColor(AppColor.primary)
                    }

                    HStack(spacing: 4) {
                        Text("📅")
                            .font(.system(size: 11))
                        Text(validUntilText)
                            .font(AppFont.medium(12))
                            .foregroundColor(Color(hex: "8F8F8F"))
                    }
                    .padding(.top, 2)

                    Text("필요 스탬프 : \(coupon.requiredStamps)개")
                        .font(AppFont.medium(12))
                        .foregroundColor(canExchange ? AppColor.primary : Color(hex: "EA580C"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            RoundedRectangle(cornerRadius: 7)
                                .fill(canExchange ? Color(hex: "EDF7FF") : Color(hex: "FFF7ED"))
                        )
                        .padding(.top, 4)
                }

                Spacer()
            }
            .padding(.vertical, 16)

            Button(action: { if canExchange { onExchangeTapped() } }) {
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
                                    colors: [Color(hex: "D0D5DD"), Color(hex: "D0D5DD")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                    )
            }
            .disabled(!canExchange)
            .padding(.bottom, 16)
        }
    }
}

// MARK: - Dashed Divider

private struct DashedDivider: View {
    var body: some View {
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
        .frame(height: 1)
    }
}

#Preview {
    StampExchangeView(
        onBackTapped: {},
        viewModel: StampViewModel()
    )
}
