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

struct ActivityDisplayItem: Identifiable {
    let id: String
    let type: ActivityType
    let title: String
    let datetime: String
    let validUntil: String?
}

private func formatISODate(_ isoString: String) -> String {
    let isoFormatter = ISO8601DateFormatter()
    isoFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
    if let date = isoFormatter.date(from: isoString) {
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "yyyy.MM.dd HH:mm"
        return displayFormatter.string(from: date)
    }
    // Fallback without fractional seconds
    let isoFormatter2 = ISO8601DateFormatter()
    isoFormatter2.formatOptions = [.withInternetDateTime]
    if let date = isoFormatter2.date(from: isoString) {
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "yyyy.MM.dd HH:mm"
        return displayFormatter.string(from: date)
    }
    return isoString
}

private func convertDTO(_ dto: ActivityItemDTO, index: Int) -> ActivityDisplayItem {
    let activityType: ActivityType
    switch dto.type {
    case "stamp": activityType = .stamp
    case "mission": activityType = .mission
    case "event": activityType = .event
    case "coupon": activityType = .coupon
    default: activityType = .stamp
    }
    return ActivityDisplayItem(
        id: "\(dto.type)-\(index)-\(dto.date)",
        type: activityType,
        title: dto.title,
        datetime: formatISODate(dto.date),
        validUntil: dto.validUntil
    )
}

struct ActivityHistoryView: View {
    var onBackTapped: () -> Void
    @StateObject private var viewModel = ActivityHistoryViewModel()
    @State private var selectedFilter: ActivityType? = nil

    private var allActivities: [ActivityDisplayItem] {
        viewModel.activities.enumerated().map { (index, dto) in
            convertDTO(dto, index: index)
        }
    }

    private var filteredActivities: [ActivityDisplayItem] {
        if let filter = selectedFilter {
            return allActivities.filter { $0.type == filter }
        }
        return allActivities
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
            if viewModel.isLoading {
                Spacer()
                ProgressView()
                    .tint(AppColor.primary)
                Spacer()
            } else {
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
        }
        .background(Color.white)
        .onAppear {
            viewModel.fetchActivities()
        }
    }
}

private struct ActivityItemRow: View {
    let activity: ActivityDisplayItem

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
