//
//  ProgramDetailView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct ProgramDetailView: View {
    let program: ProgramData
    var onBackTapped: (() -> Void)?
    var onNavigateToMap: ((Double, Double) -> Void)?
    @StateObject private var viewModel = ProgramDetailViewModel()
    @State private var showShareSheet = false
    @State private var showMissionSheet = false
    @State private var selectedMission: MissionData?
    @State private var showMissionRestrictionAlert = false
    @State private var missionRestrictionMessage = ""

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

                        if let phone = program.phone, !phone.isEmpty {
                            DetailInfoRow(
                                systemIcon: "phone.fill",
                                assetIcon: nil,
                                text: phone
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
                        HStack(spacing: 8) {
                            Text("소개")
                                .font(AppFont.semibold(14))
                                .foregroundColor(Color(hex: "121212"))

                            if !isFood {
                                Button(action: {
                                    if viewModel.isSpeaking {
                                        viewModel.stopSpeaking()
                                    } else if let desc = program.description {
                                        viewModel.speakDescription(desc)
                                    }
                                }) {
                                    TTSIndicator(isSpeaking: viewModel.isSpeaking)
                                }
                            }

                            Spacer()
                        }
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

                    if let storeCoupons = program.storeCoupons, !storeCoupons.isEmpty {
                        ForEach(storeCoupons, id: \.couponId) { sc in
                            HStack(spacing: 6) {
                                Text("•")
                                    .font(AppFont.regular(12))
                                    .foregroundColor(Color(hex: "595959"))
                                Text("\(sc.couponName) (\(sc.storeName))")
                                    .font(AppFont.regular(12))
                                    .foregroundColor(Color(hex: "595959"))
                                Spacer()
                            }
                            .padding(.horizontal, 20)
                            .padding(.top, 2)
                        }
                    } else {
                        Text("등록된 제휴 혜택이 없습니다")
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "9CA3AF"))
                            .padding(.horizontal, 20)
                            .padding(.top, 8)
                    }

                    // 참여 가능 미션 section
                    Group {
                        Divider()
                            .background(Color(hex: "B5B5B5"))
                            .padding(.horizontal, 20)
                            .padding(.top, 14)

                        Text("참여 가능 미션")
                            .font(AppFont.semibold(14))
                            .foregroundColor(Color(hex: "121212"))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 20)
                            .padding(.top, 14)

                        if viewModel.isMissionsLoading {
                            HStack {
                                Spacer()
                                ProgressView()
                                Spacer()
                            }
                            .padding(20)
                        } else {
                            let available = viewModel.availableMissions(for: program.category ?? "")
                            let disabled = viewModel.disabledMissions(for: program.category ?? "")

                            ForEach(available) { mission in
                                MissionCardView(mission: mission, enabled: true)
                            }

                            if !disabled.isEmpty {
                                Text(viewModel.disabledMessage(for: program.category ?? ""))
                                    .font(AppFont.regular(11))
                                    .foregroundColor(Color(hex: "DC2626"))
                                    .padding(.horizontal, 20)
                                    .padding(.top, 8)

                                ForEach(disabled) { mission in
                                    MissionCardView(mission: mission, enabled: false)
                                }
                            }
                        }
                    }

                    Spacer().frame(height: 30)

                    // Message banner
                    if let message = viewModel.message {
                        Text(message)
                            .font(AppFont.medium(13))
                            .foregroundColor(viewModel.isParticipated ? Color(hex: "16A34A") : Color(hex: "DC2626"))
                            .padding(.horizontal, 20)
                            .padding(.bottom, 8)
                    }

                    // Action buttons: "지도에서 보기" + "참여하기"
                    HStack(spacing: 11) {
                        // "지도에서 보기" button
                        Button(action: {
                            guard let lat = program.latitude, let lng = program.longitude else { return }
                            onNavigateToMap?(lat, lng)
                        }) {
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

                        // "참여하기" button - 항상 활성화 (미션 내부에서 위치 체크)
                        Button(action: { showMissionSheet = true }) {
                            Text("참여하기")
                                .font(AppFont.medium(16))
                                .tracking(-0.32)
                            .foregroundColor(.white)
                            .frame(width: 101, height: 56)
                            .background(
                                LinearGradient(colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")], startPoint: .topLeading, endPoint: .bottomTrailing)
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 40)
                }
            }
        }
        .background(Color.white)
        .onAppear {
            viewModel.checkParticipation(program: program)
            viewModel.fetchMissions(programId: program.id)
            viewModel.checkLocationForParticipation(lat: program.latitude, lng: program.longitude)
        }
        .sheet(isPresented: $showShareSheet) {
            ShareSheet(items: [shareText])
        }
        .sheet(isPresented: $showMissionSheet) {
            MissionSelectionSheet(
                missions: viewModel.missions,
                category: program.category ?? "",
                onSelectMission: { mission in
                    showMissionSheet = false
                    let cat = program.category ?? ""
                    // 세미나: 체류시간만 가능
                    if cat == "seminar" && mission.type != "stay_time" {
                        missionRestrictionMessage = "해당 프로그램은 체류시간 미션만 참여 가능합니다."
                        showMissionRestrictionAlert = true
                        return
                    }
                    // 비세미나: 체류시간 불가
                    if cat != "seminar" && mission.type == "stay_time" {
                        missionRestrictionMessage = "해당 장소는 체류시간 미션 대상이 아닙니다. 다른 미션을 선택해주세요."
                        showMissionRestrictionAlert = true
                        return
                    }
                    selectedMission = mission
                },
                onDismiss: { showMissionSheet = false }
            )
            .presentationDetents([.medium, .large])
        }
        .fullScreenCover(item: $selectedMission) { mission in
            missionView(for: mission)
        }
        .alert("알림", isPresented: $showMissionRestrictionAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(missionRestrictionMessage)
        }
    }

    @ViewBuilder
    private func missionView(for mission: MissionData) -> some View {
        switch mission.type {
        case "quiz":
            QuizMissionView(
                mission: mission,
                onDismiss: { selectedMission = nil },
                onCompleted: { viewModel.fetchMissions(programId: program.id) }
            )
        case "location_auth":
            LocationMissionView(
                mission: mission,
                onDismiss: { selectedMission = nil },
                onCompleted: { viewModel.fetchMissions(programId: program.id) }
            )
        case "stay_time":
            StayTimeMissionView(
                mission: mission,
                onDismiss: { selectedMission = nil },
                onCompleted: { viewModel.fetchMissions(programId: program.id) }
            )
        default:
            EmptyView()
        }
    }

    // MARK: - Helpers

    private var shareText: String {
        "\(program.name) - 알파시티 스탬프 투어\nalphacity://program/\(program.id)"
    }

    private var isSeminar: Bool { program.category == "seminar" }
    private var isFood: Bool { program.category == "food" }

    private var headerTitle: String {
        switch program.category {
        case "food": return "맛집 상세"
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

// MARK: - Mission Card

private struct MissionCardView: View {
    let mission: MissionData
    let enabled: Bool

    private var typeLabel: String {
        switch mission.type {
        case "quiz": return "퀴즈"
        case "location_auth": return "위치 인증"
        case "stay_time": return "체류시간"
        default: return mission.type
        }
    }

    private var typeColor: Color {
        switch mission.type {
        case "quiz": return Color(hex: "2563EB")
        case "location_auth": return Color(hex: "16A34A")
        case "stay_time": return Color(hex: "D97706")
        default: return Color(hex: "6B7280")
        }
    }

    private var detail: String? {
        switch mission.type {
        case "quiz": return mission.question
        case "location_auth": return mission.place?.name
        case "stay_time":
            let place = mission.place?.name ?? ""
            let mins = mission.stayMinutes ?? 0
            return "\(place) \(mins)분"
        default: return nil
        }
    }

    private var isCompleted: Bool {
        mission.isCompleted == true
    }

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(mission.name)
                        .font(AppFont.medium(13))
                        .foregroundColor(Color(hex: "121212"))

                    Text(typeLabel)
                        .font(AppFont.semibold(10))
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(enabled ? typeColor : Color(hex: "AAAAAA"))
                        .clipShape(RoundedRectangle(cornerRadius: 100))

                    if isCompleted {
                        Text("완료")
                            .font(AppFont.semibold(10))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color(hex: "16A34A"))
                            .clipShape(RoundedRectangle(cornerRadius: 100))
                    }
                }

                if let detail = detail, !detail.isEmpty {
                    Text(detail)
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "888888"))
                        .lineLimit(1)
                }
            }
            Spacer()

            if isCompleted {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundColor(Color(hex: "16A34A"))
                    .font(.system(size: 18))
            }
        }
        .padding(12)
        .background(isCompleted ? Color(hex: "F0FFF4") : (enabled ? Color(hex: "F8F9FA") : Color(hex: "F0F0F0")))
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .padding(.horizontal, 20)
        .padding(.vertical, 2)
        .opacity(enabled ? 1.0 : 0.5)
    }
}

// MARK: - Mission Selection Sheet

struct MissionSelectionSheet: View {
    let missions: [MissionData]
    let category: String
    var onSelectMission: ((MissionData) -> Void)?
    var onDismiss: (() -> Void)?

    var body: some View {
        VStack(spacing: 0) {
            // Handle bar
            RoundedRectangle(cornerRadius: 3)
                .fill(Color(hex: "D1D5DB"))
                .frame(width: 40, height: 5)
                .padding(.top, 12)

            // Header
            HStack {
                Text("미션 선택")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
                Button(action: { onDismiss?() }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Color(hex: "828282"))
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 20)

            Text("참여할 미션을 선택하세요")
                .font(AppFont.regular(13))
                .foregroundColor(Color(hex: "828282"))
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 4)

            Divider()
                .padding(.top, 16)

            // Mission list
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 8) {
                    ForEach(missions) { mission in
                        let isAvailable = isMissionAvailable(mission)
                        let isCompleted = mission.isCompleted == true

                        Button(action: {
                            if !isCompleted {
                                onSelectMission?(mission)
                            }
                        }) {
                            HStack(spacing: 12) {
                                // 아이콘
                                Image(systemName: missionIcon(mission.type))
                                    .font(.system(size: 20))
                                    .foregroundColor(isCompleted ? Color(hex: "16A34A") : missionColor(mission.type))
                                    .frame(width: 40, height: 40)
                                    .background(
                                        Circle().fill(isCompleted ? Color(hex: "F0FFF4") : missionBgColor(mission.type))
                                    )

                                VStack(alignment: .leading, spacing: 2) {
                                    HStack(spacing: 6) {
                                        Text(mission.name)
                                            .font(AppFont.medium(14))
                                            .foregroundColor(Color(hex: "121212"))

                                        Text(missionTypeLabel(mission.type))
                                            .font(AppFont.semibold(10))
                                            .foregroundColor(.white)
                                            .padding(.horizontal, 6)
                                            .padding(.vertical, 1)
                                            .background(missionColor(mission.type))
                                            .clipShape(RoundedRectangle(cornerRadius: 100))
                                    }

                                    if let detail = missionDetail(mission) {
                                        Text(detail)
                                            .font(AppFont.regular(12))
                                            .foregroundColor(Color(hex: "888888"))
                                            .lineLimit(1)
                                    }
                                }

                                Spacer()

                                if isCompleted {
                                    Image(systemName: "checkmark.circle.fill")
                                        .foregroundColor(Color(hex: "16A34A"))
                                        .font(.system(size: 20))
                                } else if !isAvailable {
                                    Image(systemName: "lock.fill")
                                        .foregroundColor(Color(hex: "CCCCCC"))
                                        .font(.system(size: 16))
                                } else {
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(Color(hex: "CCCCCC"))
                                        .font(.system(size: 14))
                                }
                            }
                            .padding(14)
                            .background(
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(isCompleted ? Color(hex: "F0FFF4") : Color(hex: "F8F9FA"))
                            )
                            .opacity(isAvailable || isCompleted ? 1.0 : 0.5)
                        }
                        .disabled(isCompleted)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 16)
                .padding(.bottom, 40)
            }
        }
        .background(Color.white)
    }

    private func isMissionAvailable(_ mission: MissionData) -> Bool {
        if category == "seminar" {
            return mission.type == "stay_time"
        } else {
            return mission.type == "quiz" || mission.type == "location_auth"
        }
    }

    private func missionIcon(_ type: String) -> String {
        switch type {
        case "quiz": return "questionmark.circle"
        case "location_auth": return "mappin.circle"
        case "stay_time": return "timer"
        default: return "star.circle"
        }
    }

    private func missionColor(_ type: String) -> Color {
        switch type {
        case "quiz": return Color(hex: "2563EB")
        case "location_auth": return Color(hex: "16A34A")
        case "stay_time": return Color(hex: "D97706")
        default: return Color(hex: "6B7280")
        }
    }

    private func missionBgColor(_ type: String) -> Color {
        switch type {
        case "quiz": return Color(hex: "EFF6FF")
        case "location_auth": return Color(hex: "F0FFF4")
        case "stay_time": return Color(hex: "FFFBEB")
        default: return Color(hex: "F3F4F6")
        }
    }

    private func missionTypeLabel(_ type: String) -> String {
        switch type {
        case "quiz": return "퀴즈"
        case "location_auth": return "위치 인증"
        case "stay_time": return "체류시간"
        default: return type
        }
    }

    private func missionDetail(_ mission: MissionData) -> String? {
        switch mission.type {
        case "quiz": return mission.question
        case "location_auth": return mission.place?.name
        case "stay_time":
            let place = mission.place?.name ?? ""
            let mins = mission.stayMinutes ?? 0
            return "\(place) \(mins)분"
        default: return nil
        }
    }
}

#Preview {
    ProgramDetailView(
        program: ProgramData(
            id: 1,
            name: "현대 AI 모빌리티 혁신 전시 2026",
            description: "2026, 새로워진 현대의 AI 기술로\n자율주행부터 스마트 이동 기술까지, 미래 모빌리티를 경험하세요.",
            category: "exhibition",
            subcategory: nil,
            hasCoupon: nil,
            imageUrl: nil,
            operatingHours: "10:00 - 18:00",
            location: "알파시티 2로 33 태왕알파시티 3층 AI 체험존",
            latitude: 35.842,
            longitude: 128.690,
            phone: nil,
            speaker: nil,
            startDate: "2026-01-15",
            endDate: "2026-01-31",
            status: "in_progress",
            events: nil
        ),
        onBackTapped: {}
    )
}
