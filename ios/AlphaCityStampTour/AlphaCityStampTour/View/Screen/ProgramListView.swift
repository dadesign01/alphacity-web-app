//
//  ProgramListView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct ProgramListView: View {
    @StateObject private var viewModel = ProgramListViewModel()
    var initialCategory: String? = nil
    var onBackTapped: (() -> Void)?
    var onProgramTapped: ((ProgramData) -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            // Header
            ProgramListHeaderView(onBackTapped: { onBackTapped?() })

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))

            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // 축제 선택 드롭다운
                    FestivalSelectBarView(
                        festivals: viewModel.festivals,
                        selectedId: viewModel.selectedFestivalId,
                        onSelect: { viewModel.selectFestival($0) }
                    )

                    // Category Tabs
                    CategoryTabsView(
                        selectedCategory: viewModel.selectedCategory,
                        onCategorySelected: { viewModel.selectCategory($0) }
                    )

                    // Program List
                    if viewModel.programs.isEmpty && !viewModel.isLoading {
                        Text("등록된 프로그램이 없습니다")
                            .font(AppFont.regular(14))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .padding(.top, 80)
                            .frame(minHeight: 300)
                    } else {
                        ForEach(Array(viewModel.programs.enumerated()), id: \.element.id) { index, program in
                            ProgramListCardView(
                                program: program,
                                isSeminar: viewModel.selectedCategory == "seminar"
                            )
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onProgramTapped?(program)
                            }

                            if index < viewModel.programs.count - 1 {
                                Divider()
                                    .foregroundColor(Color(hex: "B5B5B5"))
                                    .padding(.horizontal, 20)
                            }
                        }
                    }

                    Spacer().frame(minHeight: 40)

                    // Footer - always at bottom
                    VStack {
                        Text("2026 OLLYMOA. All rights reserved.")
                            .font(AppFont.regular(10))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .padding(.vertical, 20)
                    }
                    .frame(maxWidth: .infinity)
                    .background(AppColor.background)
                }
                .frame(minHeight: UIScreen.main.bounds.height - 120)
            }
        }
        .background(Color.white)
        .onAppear {
            if let category = initialCategory {
                viewModel.selectCategory(category)
            } else {
                viewModel.fetchPrograms()
            }
        }
    }
}

// MARK: - Header

private struct ProgramListHeaderView: View {
    var onBackTapped: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onBackTapped) {
                Image("IconBackArrow")
                    .renderingMode(.original)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 13, height: 26)
            }

            Text("오늘의 프로그램")
                .font(AppFont.semibold(18))
                .foregroundColor(Color(hex: "121212"))

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.white)
    }
}

// MARK: - 축제 선택 드롭다운

private struct FestivalSelectBarView: View {
    let festivals: [FestivalData]
    let selectedId: Int?
    let onSelect: (Int?) -> Void

    private var selectedName: String {
        festivals.first(where: { $0.id == selectedId })?.name ?? "전체 축제"
    }

    var body: some View {
        Menu {
            Button("전체 축제") { onSelect(nil) }
            ForEach(festivals, id: \.id) { festival in
                Button(festival.name) { onSelect(festival.id) }
            }
        } label: {
            HStack(spacing: 10) {
                Text("축제 선택")
                    .font(AppFont.medium(13))
                    .foregroundColor(Color(hex: "8F8F8F"))
                Text(selectedName)
                    .font(AppFont.semibold(14))
                    .foregroundColor(Color(hex: "121212"))
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.down")
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "8F8F8F"))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 10)
                    .stroke(Color(hex: "D0D5DD"), lineWidth: 1)
            )
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 10)
    }
}

// MARK: - Category Tabs

private struct CategoryTabsView: View {
    let selectedCategory: String
    let onCategorySelected: (String) -> Void

    private let categories: [(key: String, label: String, icon: String)] = [
        ("food", "맛집", "icon_food"),
        ("exhibition", "전시", "icon_exhibition"),
        ("seminar", "세미나", "icon_seminar"),
        ("experience", "체험", "icon_activity"),
    ]

    var body: some View {
        HStack(spacing: 7) {
            ForEach(categories, id: \.key) { category in
                let isSelected = selectedCategory == category.key
                Button {
                    onCategorySelected(category.key)
                } label: {
                    HStack(spacing: 4) {
                        Image(category.icon)
                            .renderingMode(.template)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 14, height: 14)
                            .foregroundColor(isSelected ? .white : Color(hex: "121212"))
                        Text(category.label)
                            .font(AppFont.regular(14))
                            .foregroundColor(isSelected ? .white : Color(hex: "121212"))
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(isSelected ? Color(hex: "121212") : Color(hex: "F8F8F8"))
                    .clipShape(RoundedRectangle(cornerRadius: 11))
                }
            }
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }
}

// MARK: - Program Card

private struct ProgramListCardView: View {
    let program: ProgramData
    let isSeminar: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Thumbnail image with coupon badge
            ZStack(alignment: .topTrailing) {
                if let imageUrl = program.imageUrl, !imageUrl.isEmpty {
                    let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                    AsyncImage(url: URL(string: fullURL)) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity)
                                .frame(height: 240)
                                .clipped()
                        default:
                            placeholderImage
                        }
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 22))
                } else {
                    placeholderImage
                        .clipShape(RoundedRectangle(cornerRadius: 22))
                }

                // Coupon badge overlay
                if program.hasCoupon == true {
                    Text("쿠폰")
                        .font(AppFont.semibold(10))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color(hex: "FF6B35"))
                        .clipShape(RoundedRectangle(cornerRadius: 100))
                        .padding(.top, 10)
                        .padding(.trailing, 10)
                }
            }

            Spacer().frame(height: 12)

            // Subcategory tag + Status badge + Title
            HStack(spacing: 8) {
                // Subcategory tag
                if let subcategory = program.subcategory, !subcategory.isEmpty {
                    Text(subcategory)
                        .font(AppFont.medium(11))
                        .foregroundColor(Color(hex: "555555"))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Color(hex: "F0F0F0"))
                        .clipShape(RoundedRectangle(cornerRadius: 100))
                }

                let badge = statusBadge
                Text(badge.text)
                    .font(AppFont.semibold(11))
                    .foregroundColor(badge.textColor)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(badge.bgColor)
                    .clipShape(RoundedRectangle(cornerRadius: 100))

                Text(program.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .lineLimit(1)
            }

            // Description
            if let description = program.description, !description.isEmpty {
                Text(description)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .lineLimit(2)
                    .lineSpacing(4)
                    .padding(.top, 8)
            }

            // Time / Date info
            Text(timeInfo)
                .font(AppFont.regular(12))
                .foregroundColor(Color(hex: "828282"))
                .lineSpacing(4)
                .padding(.top, 5)

            // Location bar
            if let location = program.location, !location.isEmpty {
                HStack(spacing: 6) {
                    Image("icon_location_pin")
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 12, height: 12)
                        .foregroundColor(Color(hex: "8F8F8F"))
                    Text(location)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .lineLimit(1)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(hex: "F8F8F8"))
                .clipShape(RoundedRectangle(cornerRadius: 7))
                .padding(.top, 7)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    private var placeholderImage: some View {
        ZStack {
            Color(hex: "E8E8E8")
            Text(String(program.name.prefix(1)))
                .font(AppFont.bold(32))
                .foregroundColor(Color(hex: "B5B5B5"))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 240)
    }

    private var statusBadge: (text: String, bgColor: Color, textColor: Color) {
        switch program.status {
        case "in_progress":
            if isSeminar {
                return ("참여 가능", Color(hex: "EDF7FF"), AppColor.primary)
            }
            return ("운영중", Color(hex: "EDF7FF"), AppColor.primary)
        case "scheduled":
            return ("운영예정", Color(hex: "F8F8F8"), Color(hex: "8F8F8F"))
        default:
            return ("종료", Color(hex: "F8F8F8"), Color(hex: "8F8F8F"))
        }
    }

    private var timeInfo: String {
        let hours = program.operatingHours ?? ""
        if isSeminar, let speaker = program.speaker, !speaker.isEmpty {
            return hours.isEmpty ? speaker : "\(hours)    |   \(speaker)"
        } else {
            let dateRange = formatDateRange(start: program.startDate, end: program.endDate)
            return hours.isEmpty ? dateRange : "\(hours)    |   \(dateRange)"
        }
    }

    private func formatDateRange(start: String, end: String) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]

        let displayFormatter = DateFormatter()
        displayFormatter.locale = Locale(identifier: "ko_KR")
        displayFormatter.dateFormat = "M/dd(E)"

        let startStr = formatter.date(from: String(start.prefix(10))).map { displayFormatter.string(from: $0) } ?? start.prefix(10).description
        let endStr = formatter.date(from: String(end.prefix(10))).map { displayFormatter.string(from: $0) } ?? end.prefix(10).description
        return "\(startStr) ~ \(endStr)"
    }
}

#Preview {
    ProgramListView(onBackTapped: {}, onProgramTapped: { _ in })
}
