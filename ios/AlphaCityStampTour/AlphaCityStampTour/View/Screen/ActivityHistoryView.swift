//
//  ActivityHistoryView.swift
//  AlphaCityStampTour
//

import SwiftUI

enum ActivityType: String, CaseIterable {
    case stamp, mission, event, coupon

    var badgeLabel: String {
        switch self {
        case .stamp: return "스탬프"
        case .mission: return "미 션"
        case .event: return "행 사"
        case .coupon: return "교  환"
        }
    }

    var filterLabel: String {
        switch self {
        case .stamp: return "스탬프"
        case .mission: return "미션"
        case .event: return "행사"
        case .coupon: return "쿠폰 교환"
        }
    }

    var iconName: String {
        switch self {
        case .stamp: return "ActivityStamp"
        case .mission: return "ActivityMission"
        case .event: return "ActivityEvent"
        case .coupon: return "ActivityCoupon"
        }
    }

    var isFilled: Bool {
        self == .coupon
    }
}

struct ActivityItemData: Identifiable {
    let id: Int
    let type: ActivityType
    let title: String
    let datetime: String
    let validUntil: String?
}

private let mockActivities: [ActivityItemData] = [
    ActivityItemData(id: 1, type: .coupon, title: "스탬프 3개 쿠폰 교환", datetime: "2026.01.28 16:55", validUntil: nil),
    ActivityItemData(id: 2, type: .stamp, title: "디지털 갤러리 스탬프 획득", datetime: "2026.01.28 15:30", validUntil: "2026년 03월 31일까지 사용 가능"),
    ActivityItemData(id: 3, type: .mission, title: "디지털 갤러리 퀴즈 미션 완료", datetime: "2026.01.28 15:30", validUntil: nil),
    ActivityItemData(id: 4, type: .event, title: "디지털 아트 전시회 참여", datetime: "2026.01.28 12:23", validUntil: nil),
    ActivityItemData(id: 5, type: .stamp, title: "VR 스튜디오 스탬프 획득", datetime: "2026.01.28 12:00", validUntil: "2026년 03월 31일까지 사용 가능"),
    ActivityItemData(id: 6, type: .stamp, title: "AI 스터디 스탬프 획득", datetime: "2026.01.28 11:48", validUntil: "2026년 03월 31일까지 사용 가능"),
]

struct ActivityHistoryView: View {
    var onBackTapped: () -> Void
    @State private var selectedFilter: ActivityType? = nil

    private var filteredActivities: [ActivityItemData] {
        if let filter = selectedFilter {
            return mockActivities.filter { $0.type == filter }
        }
        return mockActivities
    }

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
                Text("활동 이력")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .frame(height: 50)

            Divider()
                .background(Color(hex: "E2E2E2"))

            // === Filter Tabs ===
            HStack(spacing: 8) {
                ForEach(ActivityType.allCases, id: \.self) { filter in
                    let isSelected = selectedFilter == filter
                    Button {
                        selectedFilter = isSelected ? nil : filter
                    } label: {
                        Text(filter.filterLabel)
                            .font(AppFont.regular(14))
                            .foregroundColor(isSelected ? .white : Color(hex: "121212"))
                            .padding(.horizontal, 14)
                            .padding(.vertical, 7)
                            .background(
                                RoundedRectangle(cornerRadius: 11)
                                    .fill(isSelected ? Color(hex: "121212") : Color(hex: "F8F8F8"))
                            )
                    }
                }
                Spacer()
            }
            .padding(.horizontal, 25)
            .padding(.vertical, 12)

            // === Activity List ===
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(filteredActivities) { activity in
                        ActivityItemRow(activity: activity)
                        Divider()
                            .background(Color(hex: "B5B5B5"))
                            .padding(.horizontal, 20)
                    }

                    // Footer
                    Text("© 2026 Alpha Stamp. All rights reserved.")
                        .font(AppFont.regular(10))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                        .background(Color(hex: "F9F9F9"))
                }
            }
        }
        .background(Color.white)
    }
}

private struct ActivityItemRow: View {
    let activity: ActivityItemData

    var body: some View {
        HStack(alignment: .top, spacing: 20) {
            // Thumbnail circle
            ZStack {
                Circle()
                    .fill(Color(hex: "F8F8F8"))
                    .frame(width: 85, height: 85)
                Image(activity.type.iconName)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 54, height: 54)
            }

            // Content
            VStack(alignment: .leading, spacing: 4) {
                // Badge
                if activity.type.isFilled {
                    Text(activity.type.badgeLabel)
                        .font(AppFont.semibold(11))
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 3)
                        .background(
                            Capsule().fill(AppColor.primary)
                        )
                } else {
                    Text(activity.type.badgeLabel)
                        .font(AppFont.semibold(11))
                        .foregroundColor(AppColor.primary)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 3)
                        .overlay(
                            Capsule().stroke(AppColor.primary, lineWidth: 1)
                        )
                }

                // Title
                Text(activity.title)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))

                // DateTime
                Text("활동 일시 : \(activity.datetime)")
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))

                // Validity info
                if let validUntil = activity.validUntil {
                    Text(validUntil)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(
                            RoundedRectangle(cornerRadius: 7)
                                .fill(Color(hex: "F8F8F8"))
                        )
                        .padding(.top, 2)
                }
            }

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
    }
}

#Preview {
    ActivityHistoryView(onBackTapped: {})
}
