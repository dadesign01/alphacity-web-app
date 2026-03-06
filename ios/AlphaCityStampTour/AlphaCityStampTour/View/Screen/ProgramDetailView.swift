//
//  ProgramDetailView.swift
//  AlphaCityStampTour
//

import SwiftUI
import MapKit

struct ProgramDetailView: View {
    let program: ProgramData
    var onBackTapped: (() -> Void)?
    @StateObject private var viewModel = ProgramDetailViewModel()
    @State private var showShareSheet = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            DetailHeaderView(
                title: headerTitle,
                onBackTapped: { onBackTapped?() }
            )

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))

            ScrollView(.vertical, showsIndicators: false) {
                VStack(alignment: .leading, spacing: 0) {
                    // Thumbnail (padded, rounded)
                    DetailThumbnailView(program: program)
                        .padding(.horizontal, 20)
                        .padding(.top, 20)

                    // Title
                    Text(program.name)
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                        .padding(.horizontal, 20)
                        .padding(.top, 13)

                    // Category badges
                    HStack(spacing: 4) {
                        Text(categoryLabel)
                            .font(AppFont.semibold(11))
                            .foregroundColor(.white)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 3)
                            .background(categoryBadgeColor)
                            .clipShape(RoundedRectangle(cornerRadius: 100))

                        if program.status == "in_progress" {
                            Text(isSeminar ? "참여 가능" : "운영중")
                                .font(AppFont.semibold(11))
                                .foregroundColor(.white)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 3)
                                .background(Color(hex: "845EDA"))
                                .clipShape(RoundedRectangle(cornerRadius: 100))
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 10)

                    // Info rows (icon + text, no label)
                    VStack(spacing: 0) {
                        if let location = program.location, !location.isEmpty {
                            DetailInfoRow(
                                systemIcon: "mappin.and.ellipse",
                                assetIcon: "icon_location_pin",
                                text: location
                            )
                        }

                        if let hours = program.operatingHours, !hours.isEmpty {
                            DetailInfoRow(
                                systemIcon: "clock",
                                assetIcon: nil,
                                text: hours
                            )
                        }

                        if let speaker = program.speaker, !speaker.isEmpty {
                            DetailInfoRow(
                                systemIcon: "person.fill",
                                assetIcon: nil,
                                text: speaker
                            )
                        }

                        DetailInfoRow(
                            systemIcon: "calendar",
                            assetIcon: nil,
                            text: formatDateRange(start: program.startDate, end: program.endDate)
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 14)

                    // Divider
                    Divider()
                        .background(Color(hex: "B5B5B5"))
                        .padding(.horizontal, 20)
                        .padding(.top, 14)

                    // "소개" section
                    if let description = program.description, !description.isEmpty {
                        Text("소개")
                            .font(AppFont.semibold(14))
                            .foregroundColor(Color(hex: "121212"))
                            .padding(.horizontal, 20)
                            .padding(.top, 14)

                        Text(description)
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "595959"))
                            .lineSpacing(4)
                            .padding(.horizontal, 20)
                            .padding(.top, 8)
                    }

                    // Divider
                    Divider()
                        .background(Color(hex: "B5B5B5"))
                        .padding(.horizontal, 20)
                        .padding(.top, 14)

                    // "제휴 혜택" section
                    Text("제휴 혜택")
                        .font(AppFont.semibold(14))
                        .foregroundColor(Color(hex: "121212"))
                        .padding(.horizontal, 20)
                        .padding(.top, 14)

                    Text("스탬프 투어 참여자 10% 할인")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "595959"))
                        .padding(.horizontal, 20)
                        .padding(.top, 8)

                    Text("쿠폰 사용 가능")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "595959"))
                        .padding(.horizontal, 20)
                        .padding(.top, 3)

                    Spacer().frame(height: 30)

                    // Message banner
                    if let message = viewModel.message {
                        Text(message)
                            .font(AppFont.medium(13))
                            .foregroundColor(viewModel.isParticipated ? Color(hex: "16A34A") : Color(hex: "DC2626"))
                            .padding(.horizontal, 20)
                            .padding(.bottom, 8)
                    }

                    // Action buttons
                    if isFood {
                        // Food: "지도에서 보기" + Share button
                        HStack(spacing: 11) {
                            Button(action: { openInMaps() }) {
                                HStack(spacing: 8) {
                                    Image(systemName: "map")
                                        .resizable()
                                        .scaledToFit()
                                        .frame(width: 20, height: 20)
                                    Text("지도에서 보기")
                                        .font(AppFont.medium(16))
                                        .tracking(-0.32)
                                }
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 56)
                                .background(
                                    LinearGradient(
                                        colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                            }

                            Button(action: { showShareSheet = true }) {
                                Image(systemName: "square.and.arrow.up")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 24, height: 24)
                                    .foregroundColor(AppColor.primary)
                            }
                            .frame(width: 56, height: 56)
                            .background(Color(hex: "EDF7FF"))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 40)
                    } else {
                        // Program/Seminar: "참여하기" button (right-aligned, compact)
                        HStack {
                            Spacer()
                            Button(action: { viewModel.participate(program: program) }) {
                                Group {
                                    if viewModel.isLoading {
                                        ProgressView()
                                            .tint(.white)
                                    } else {
                                        Text(viewModel.isParticipated ? "참여 완료" : "참여하기")
                                            .font(AppFont.medium(16))
                                            .tracking(-0.32)
                                    }
                                }
                                .foregroundColor(.white)
                                .frame(width: 101, height: 56)
                                .background(
                                    viewModel.isParticipated
                                    ? LinearGradient(colors: [Color(hex: "9CA3AF"), Color(hex: "6B7280")], startPoint: .topLeading, endPoint: .bottomTrailing)
                                    : LinearGradient(colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")], startPoint: .topLeading, endPoint: .bottomTrailing)
                                )
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                            }
                            .disabled(viewModel.isParticipated || viewModel.isLoading)
                        }
                        .padding(.horizontal, 20)
                        .padding(.bottom, 40)
                    }
                }
            }
        }
        .background(Color.white)
        .onAppear {
            viewModel.checkParticipation(program: program)
        }
        .sheet(isPresented: $showShareSheet) {
            ShareSheet(items: [shareText])
        }
    }

    // MARK: - Actions

    private func openInMaps() {
        guard let location = program.location, !location.isEmpty else { return }
        let encoded = location.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? location
        if let url = URL(string: "maps://?q=\(encoded)") {
            UIApplication.shared.open(url)
        }
    }

    private var shareText: String {
        "\(program.name) - 알파시티 스탬프 투어\nalphacity://program/\(program.id)"
    }

    private var isSeminar: Bool { program.category == "seminar" }
    private var isFood: Bool { program.category == "food" }

    private var headerTitle: String {
        switch program.category {
        case "food": return "삼점 상세"
        case "seminar": return "세미나 상세"
        default: return "프로그램 상세"
        }
    }

    private var categoryLabel: String {
        switch program.category {
        case "food": return "맛집"
        case "seminar": return "세미나"
        case "exhibition": return "전시"
        default: return "기타"
        }
    }

    private var categoryBadgeColor: Color {
        switch program.category {
        case "food": return Color(hex: "1BB3CE")
        case "seminar": return Color(hex: "2563EB")
        case "exhibition": return Color(hex: "1BB3CE")
        default: return Color(hex: "8F8F8F")
        }
    }

    private func formatDateRange(start: String, end: String) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]

        let displayFormatter = DateFormatter()
        displayFormatter.locale = Locale(identifier: "ko_KR")
        displayFormatter.dateFormat = "yyyy.MM.dd(E)"

        let startStr = formatter.date(from: String(start.prefix(10))).map { displayFormatter.string(from: $0) } ?? start.prefix(10).description
        let endStr = formatter.date(from: String(end.prefix(10))).map { displayFormatter.string(from: $0) } ?? end.prefix(10).description
        return "\(startStr) ~ \(endStr)"
    }
}

// MARK: - Header

private struct DetailHeaderView: View {
    let title: String
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

            Text(title)
                .font(AppFont.semibold(18))
                .foregroundColor(Color(hex: "121212"))

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.white)
    }
}

// MARK: - Thumbnail

private struct DetailThumbnailView: View {
    let program: ProgramData

    var body: some View {
        if let imageUrl = program.imageUrl, !imageUrl.isEmpty {
            let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
            AsyncImage(url: URL(string: fullURL)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(maxWidth: .infinity)
                        .frame(height: 229)
                        .clipped()
                default:
                    placeholderImage
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: 15))
        } else {
            placeholderImage
                .clipShape(RoundedRectangle(cornerRadius: 15))
        }
    }

    private var placeholderImage: some View {
        ZStack {
            Color(hex: "F8F8F8")
            Text(String(program.name.prefix(1)))
                .font(AppFont.bold(40))
                .foregroundColor(Color(hex: "B5B5B5"))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 229)
    }
}

// MARK: - Info Row (icon + text, no label)

private struct DetailInfoRow: View {
    let systemIcon: String
    let assetIcon: String?
    let text: String

    var body: some View {
        HStack(spacing: 8) {
            if let asset = assetIcon {
                Image(asset)
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 14, height: 14)
                    .foregroundColor(Color(hex: "595959"))
            } else {
                Image(systemName: systemIcon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 14, height: 14)
                    .foregroundColor(Color(hex: "595959"))
            }

            Text(text)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "595959"))
                .lineLimit(1)

            Spacer()
        }
        .padding(.vertical, 5)
    }
}

// MARK: - Share Sheet

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

#Preview {
    ProgramDetailView(
        program: ProgramData(
            id: 1,
            name: "현대 AI 모빌리티 혁신 전시 2026",
            description: "2026, 새로워진 현대의 AI 기술로\n자율주행부터 스마트 이동 기술까지, 미래 모빌리티를 경험하세요.",
            category: "exhibition",
            imageUrl: nil,
            operatingHours: "10:00 - 18:00",
            location: "알파시티 2로 33 태왕알파시티 3층 AI 체험존",
            latitude: 35.842,
            longitude: 128.690,
            speaker: nil,
            startDate: "2026-01-15",
            endDate: "2026-01-31",
            status: "in_progress",
            events: nil
        ),
        onBackTapped: {}
    )
}
