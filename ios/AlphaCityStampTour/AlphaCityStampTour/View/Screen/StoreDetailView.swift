//
//  StoreDetailView.swift
//  AlphaCityStampTour
//

import SwiftUI

private let categoryMap: [String: String] = [
    "cafe": "카페",
    "restaurant": "음식점",
    "shopping": "쇼핑",
    "hotel": "호텔",
    "convenience": "편의시설",
]

private let missionTypeMap: [String: String] = [
    "quiz": "퀴즈",
    "location_auth": "위치인증",
    "stay_time": "체류시간",
]

struct StoreDetailView: View {
    let store: StoreData
    var onBackTapped: () -> Void = {}
    var onNavigateToMap: ((Double, Double) -> Void)? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 8) {
                Button(action: onBackTapped) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(Color(hex: "121212"))
                        .frame(width: 44, height: 44)
                }

                Text("상점 상세")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))

                Spacer()
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 4)
            .background(Color.white)

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))

            ScrollView {
                VStack(spacing: 16) {
                    // Store image
                    if let imageUrl = store.imageUrl, let url = URL(string: imageUrl) {
                        AsyncImage(url: url) { image in
                            image
                                .resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity)
                                .frame(height: 200)
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                        } placeholder: {
                            RoundedRectangle(cornerRadius: 16)
                                .fill(Color(hex: "F0F0F0"))
                                .frame(height: 200)
                        }
                    }

                    // Store name + category
                    VStack(alignment: .leading, spacing: 8) {
                        HStack(spacing: 8) {
                            Text(store.name)
                                .font(AppFont.bold(22))
                                .foregroundColor(Color(hex: "121212"))

                            Text(categoryMap[store.category] ?? store.category)
                                .font(AppFont.semibold(12))
                                .foregroundColor(Color(hex: "16A34A"))
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(
                                    Color(hex: "16A34A").opacity(0.1)
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }

                        if let program = store.program {
                            Text(program.name)
                                .font(AppFont.medium(12))
                                .foregroundColor(AppColor.primary)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 4)
                                .background(AppColor.primary.opacity(0.1))
                                .clipShape(RoundedRectangle(cornerRadius: 6))
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)

                    // Info card
                    VStack(alignment: .leading, spacing: 12) {
                        if let address = store.address {
                            StoreInfoRow(
                                icon: "mappin.and.ellipse",
                                label: "주소",
                                value: "\(address)\(store.addressDetail.map { " \($0)" } ?? "")"
                            )
                        }

                        if store.operatingDays != nil || store.openTime != nil {
                            let operatingText = [
                                store.operatingDays,
                                (store.openTime != nil && store.closeTime != nil) ? "\(store.openTime!) ~ \(store.closeTime!)" : nil
                            ].compactMap { $0 }.joined(separator: " ")
                            StoreInfoRow(
                                icon: "clock",
                                label: "운영 시간",
                                value: operatingText
                            )
                        }

                        StoreInfoRow(
                            icon: "phone",
                            label: "연락처",
                            value: store.phone
                        )
                    }
                    .padding(20)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .shadow(color: .black.opacity(0.05), radius: 4, y: 2)

                    // Description
                    if let description = store.description {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("상점 소개")
                                .font(AppFont.semibold(16))
                                .foregroundColor(Color(hex: "121212"))

                            Text(description)
                                .font(AppFont.regular(14))
                                .foregroundColor(Color(hex: "666666"))
                                .lineSpacing(6)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(20)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                    }

                    // Connected mission
                    if let mission = store.mission {
                        VStack(alignment: .leading, spacing: 12) {
                            HStack(spacing: 8) {
                                Text("연결 미션")
                                    .font(AppFont.semibold(16))
                                    .foregroundColor(Color(hex: "121212"))

                                Text(missionTypeMap[mission.type] ?? mission.type)
                                    .font(AppFont.semibold(11))
                                    .foregroundColor(AppColor.primary)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 3)
                                    .background(AppColor.primary.opacity(0.15))
                                    .clipShape(RoundedRectangle(cornerRadius: 6))
                            }

                            Text(mission.name)
                                .font(AppFont.medium(14))
                                .foregroundColor(Color(hex: "3D608D"))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(20)
                        .background(Color(hex: "EDF7FF"))
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                    }

                    // Connected coupons
                    if let storeCoupons = store.storeCoupons, !storeCoupons.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("사용 가능 쿠폰")
                                .font(AppFont.semibold(16))
                                .foregroundColor(Color(hex: "121212"))

                            ForEach(storeCoupons, id: \.coupon.id) { sc in
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(sc.coupon.name)
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(Color(hex: "EA580C"))

                                    if let desc = sc.coupon.description {
                                        Text(desc)
                                            .font(AppFont.regular(12))
                                            .foregroundColor(Color(hex: "8F8F8F"))
                                    }
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(16)
                                .background(Color(hex: "FFF7ED"))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(20)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
                    }

                    // Map button
                    if let lat = store.latitude, let lng = store.longitude {
                        Button {
                            onNavigateToMap?(lat, lng)
                        } label: {
                            HStack(spacing: 8) {
                                Image(systemName: "mappin.and.ellipse")
                                    .font(.system(size: 16))
                                Text("지도에서 보기")
                                    .font(AppFont.semibold(16))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(Color(hex: "16A34A"))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }

                    Spacer()
                        .frame(height: 20)
                }
                .padding(20)
            }
            .background(Color(hex: "F9F9F9"))
        }
    }
}

private struct StoreInfoRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundColor(Color(hex: "9CA3AF"))
                .frame(width: 20)

            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(AppFont.medium(12))
                    .foregroundColor(Color(hex: "9CA3AF"))

                Text(value)
                    .font(AppFont.medium(14))
                    .foregroundColor(Color(hex: "121212"))
            }
        }
    }
}
