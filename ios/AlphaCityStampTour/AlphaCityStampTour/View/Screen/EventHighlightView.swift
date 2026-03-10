import SwiftUI

// MARK: - Tab Enum

private enum EventTab: Int, CaseIterable {
    case raffle, firstCome, experience

    var title: String {
        switch self {
        case .raffle: return "추첨 이벤트"
        case .firstCome: return "선착순 사은품"
        case .experience: return "체험"
        }
    }
}

// MARK: - Mock Data for Experience tab (no DB type)

private struct ExperienceEvent: Identifiable {
    let id = UUID()
    let image: String
    let title: String
    let description: String
    let timeInfo: String
    let location: String
    let price: String
}

private let mockExperienceEvents: [ExperienceEvent] = [
    ExperienceEvent(
        image: "EventImg1",
        title: "도자기 만들기",
        description: "전통 도예 기법으로 나만의 도자기를\n만들어 보세요.",
        timeInfo: "체험 시간 : 90분 | 1회 10명 정원",
        location: "알파시티 2로 33 공예 체험관",
        price: "15,000원"
    ),
    ExperienceEvent(
        image: "EventImg2",
        title: "천연비누 원데이 클래스",
        description: "천연 재료로 만드는 나만의 향기가득\n비누 만들기 원데이 클래스",
        timeInfo: "체험 시간 : 60분 | 1회 8명 정원",
        location: "알파시티 2로 33 DIY 공방",
        price: "12,000원"
    ),
    ExperienceEvent(
        image: "EventImg3",
        title: "3D 프린팅 액티비티",
        description: "3D 프린터로 나의 상상을 현실화하는\n나만의 작품을 뽐내보세요.",
        timeInfo: "체험 시간 : 120분 | 1회 6명 정원",
        location: "알파시티 2로 33 3D 프린팅 스튜디오",
        price: "무료"
    ),
    ExperienceEvent(
        image: "EventImg4",
        title: "수제 브레드 원데이 클래스",
        description: "유명 베이커리 카페 제빵사가 알려주는\n맛있는 빵 레시피! 제빵 체험해보세요.",
        timeInfo: "체험 시간 : 100분 | 1회 12명 정원",
        location: "알파시티 2로 33 ABC 베이커리",
        price: "18,000원"
    ),
]

// MARK: - Main View

struct EventHighlightView: View {
    var onBackTapped: (() -> Void)?
    @StateObject private var viewModel = EventHighlightViewModel()
    @State private var selectedTab: EventTab = .raffle
    @State private var selectedEventId: Int? = nil
    @State private var selectedEventType: String? = nil

    var body: some View {
        ZStack {
            if let eventId = selectedEventId, let eventType = selectedEventType {
                if eventType == "raffle" {
                    RaffleEventDetailView(eventId: eventId, onBackTapped: {
                        selectedEventId = nil
                        selectedEventType = nil
                        viewModel.fetchEvents()
                    })
                } else {
                    FirstComeEventDetailView(eventId: eventId, onBackTapped: {
                        selectedEventId = nil
                        selectedEventType = nil
                        viewModel.fetchEvents()
                    })
                }
            } else {
                VStack(spacing: 0) {
                    // Header
                    EventHighlightHeader(onBackTapped: onBackTapped)

                    Divider()
                        .foregroundColor(Color(hex: "E2E2E2"))

                    // Tab Bar (outside scroll to ensure clickability)
                    EventTabBarView(
                        selectedTab: selectedTab,
                        onTabSelected: { selectedTab = $0 }
                    )

                    // Content (scrollable)
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(spacing: 0) {
                            switch selectedTab {
                            case .raffle:
                                RaffleTabContent(events: viewModel.raffleEvents, onEventTapped: { event in
                                    selectedEventId = event.id
                                    selectedEventType = event.type
                                })
                            case .firstCome:
                                FirstComeTabContent(events: viewModel.firstComeEvents, onEventTapped: { event in
                                    selectedEventId = event.id
                                    selectedEventType = event.type
                                })
                            case .experience:
                                ExperienceTabContent()
                            }

                            Spacer().frame(minHeight: 40)

                            // Footer
                            VStack {
                                Text("© 2026 Alpha Stamp. All rights reserved.")
                                    .font(AppFont.regular(10))
                                    .foregroundColor(Color(hex: "8F8F8F"))
                                    .padding(.vertical, 20)
                            }
                            .frame(maxWidth: .infinity)
                            .background(AppColor.background)
                        }
                        .frame(minHeight: UIScreen.main.bounds.height - 180)
                    }
                }
                .background(Color.white)
                .navigationBarHidden(true)
                .onAppear {
                    viewModel.fetchEvents()
                }
            }
        }
    }
}

// MARK: - Header

private struct EventHighlightHeader: View {
    var onBackTapped: (() -> Void)?

    var body: some View {
        HStack(spacing: 12) {
            Button(action: { onBackTapped?() }) {
                Image("IconBackArrow")
                    .renderingMode(.original)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 13, height: 26)
            }

            Text("이벤트 하이라이트")
                .font(AppFont.semibold(18))
                .foregroundColor(Color(hex: "121212"))

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.white)
    }
}

// MARK: - Tab Bar

private struct EventTabBarView: View {
    let selectedTab: EventTab
    let onTabSelected: (EventTab) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ForEach(EventTab.allCases, id: \.self) { tab in
                let isSelected = selectedTab == tab
                Button(action: { onTabSelected(tab) }) {
                    Text(tab.title)
                        .font(AppFont.regular(14))
                        .foregroundColor(isSelected ? .white : Color(hex: "121212"))
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(
                            RoundedRectangle(cornerRadius: 11)
                                .fill(isSelected ? Color(hex: "121212") : Color(hex: "F8F8F8"))
                        )
                }
            }
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(Color.white)
    }
}

// MARK: - 추첨 이벤트 탭

private struct RaffleTabContent: View {
    let events: [EventData]
    var onEventTapped: ((EventData) -> Void)?

    var body: some View {
        if events.isEmpty {
            EmptyContentView()
        } else {
            LazyVStack(spacing: 0) {
                ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                    RaffleEventCard(event: event)
                        .onTapGesture { onEventTapped?(event) }
                    if index < events.count - 1 {
                        Divider()
                            .background(Color(hex: "B5B5B5"))
                            .padding(.horizontal, 20)
                    }
                }
            }
        }
    }
}

private struct RaffleEventCard: View {
    let event: EventData

    private var isOpen: Bool { event.status != "ended" }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 이미지
            ZStack {
                if let imageUrl = event.imageUrl, !imageUrl.isEmpty {
                    let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                    AsyncImage(url: URL(string: fullURL)) { phase in
                        switch phase {
                        case .success(let image):
                            image
                                .resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity)
                                .frame(height: 130)
                                .clipped()
                        default:
                            placeholderImage
                        }
                    }
                } else {
                    placeholderImage
                }

                if !isOpen {
                    Color.black.opacity(0.68)
                    Text("이벤트 종료")
                        .font(AppFont.bold(20))
                        .foregroundColor(.white)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 130)
            .clipShape(RoundedRectangle(cornerRadius: 22))

            // 상태 배지 + 제목
            HStack(spacing: 10) {
                Text(isOpen ? "참여 가능" : "마  감")
                    .font(AppFont.semibold(11))
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 3)
                    .background(
                        Capsule()
                            .fill(isOpen ? AppColor.primary : Color(hex: "8F8F8F"))
                    )

                Text(event.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .lineLimit(1)
            }
            .padding(.top, 12)

            // 설명
            if let description = event.description, !description.isEmpty {
                Text(description)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .lineSpacing(4)
                    .padding(.top, 9)
            }

            // 기간
            Text("이벤트 기간 : \(formatEventDate(event.startDate)) ~ \(formatEventDate(event.endDate))")
                .font(AppFont.regular(12))
                .foregroundColor(Color(hex: "828282"))
                .padding(.top, 5)

            // 보상 + 참여자수
            HStack(spacing: 4) {
                if let reward = event.reward, !reward.isEmpty {
                    HStack(spacing: 3) {
                        Image("IconCoupon")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 20, height: 20)
                        Text(reward)
                            .font(AppFont.medium(12))
                            .foregroundColor(AppColor.primary)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 5)
                    .background(
                        RoundedRectangle(cornerRadius: 7)
                            .fill(Color(hex: "EDF7FF"))
                    )
                }

                Spacer()

                HStack(spacing: 3) {
                    Image("IconProfile")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                    Text("\(formattedCount(event.participantCount ?? 0))명 참여")
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 5)
                .background(
                    RoundedRectangle(cornerRadius: 7)
                        .fill(Color(hex: "F8F8F8"))
                )
            }
            .padding(.top, 5)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    private var placeholderImage: some View {
        ZStack {
            Color(hex: "E8E8E8")
            Text(String(event.name.prefix(1)))
                .font(AppFont.bold(32))
                .foregroundColor(Color(hex: "B5B5B5"))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 130)
    }
}

// MARK: - 선착순 사은품 탭

private struct FirstComeTabContent: View {
    let events: [EventData]
    var onEventTapped: ((EventData) -> Void)?

    var body: some View {
        if events.isEmpty {
            EmptyContentView()
        } else {
            LazyVStack(spacing: 0) {
                ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                    FirstComeEventItem(event: event)
                        .onTapGesture { onEventTapped?(event) }
                    if index < events.count - 1 {
                        Divider()
                            .background(Color(hex: "B5B5B5"))
                            .padding(.horizontal, 20)
                    }
                }
            }
        }
    }
}

private struct FirstComeEventItem: View {
    let event: EventData

    private var remaining: Int {
        max(event.participantLimit - (event.participantCount ?? 0), 0)
    }
    private var isSoldOut: Bool { remaining <= 0 }
    private var isOpen: Bool { event.status != "ended" && !isSoldOut }
    private var progress: CGFloat {
        guard event.participantLimit > 0 else { return 0 }
        return CGFloat(event.participantCount ?? 0) / CGFloat(event.participantLimit)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // 유형 태그 + 상태 배지 + 제목
            HStack(spacing: 8) {
                Text("선착순")
                    .font(AppFont.semibold(11))
                    .foregroundColor(AppColor.primary)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 3)
                    .background(
                        Capsule().fill(Color(hex: "EDF7FF"))
                    )

                Text(isOpen ? "진행중" : "마감")
                    .font(AppFont.semibold(11))
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 3)
                    .background(
                        Capsule()
                            .fill(isOpen ? AppColor.primary : Color(hex: "8F8F8F"))
                    )

                Text(event.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .lineLimit(1)
            }

            // 설명
            if let description = event.description, !description.isEmpty {
                Text(description)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .lineSpacing(4)
                    .padding(.top, 9)
            }

            // 보상 태그
            if let reward = event.reward, !reward.isEmpty {
                HStack(spacing: 3) {
                    Image("IconCoupon")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                    Text(reward)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 5)
                .background(
                    RoundedRectangle(cornerRadius: 7)
                        .fill(Color(hex: "F8F8F8"))
                )
                .padding(.top, 8)
            }

            // 잔여수량
            HStack {
                Text("잔여수량")
                    .font(AppFont.regular(10))
                    .foregroundColor(Color(hex: "595959"))
                Spacer()
                Text("\(remaining)/\(event.participantLimit)명")
                    .font(AppFont.bold(12))
                    .foregroundColor(isSoldOut ? Color(hex: "8F8F8F") : AppColor.primary)
            }
            .padding(.top, 12)

            // 프로그레스 바
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule()
                        .fill(Color(hex: "F8F8F8"))
                        .frame(height: 8)

                    Capsule()
                        .fill(isOpen ? AppColor.primary : Color(hex: "8E8E8E"))
                        .frame(width: geo.size.width * min(progress, 1.0), height: 8)
                }
            }
            .frame(height: 8)
            .padding(.top, 4)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .opacity(isSoldOut ? 0.5 : 1.0)
    }
}

// MARK: - 체험 탭

private struct ExperienceTabContent: View {
    var body: some View {
        LazyVStack(spacing: 0) {
            ForEach(Array(mockExperienceEvents.enumerated()), id: \.element.id) { index, event in
                ExperienceEventItem(event: event)
                if index < mockExperienceEvents.count - 1 {
                    Divider()
                        .background(Color(hex: "B5B5B5"))
                        .padding(.horizontal, 20)
                }
            }
        }
    }
}

private struct ExperienceEventItem: View {
    let event: ExperienceEvent

    var body: some View {
        HStack(alignment: .top, spacing: 17) {
            Image(event.image)
                .resizable()
                .scaledToFill()
                .frame(width: 130, height: 168)
                .clipShape(RoundedRectangle(cornerRadius: 22))

            VStack(alignment: .leading, spacing: 0) {
                Text(event.title)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))

                Text(event.description)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .lineSpacing(4)
                    .padding(.top, 9)

                Text(event.timeInfo)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "828282"))
                    .padding(.top, 6)

                HStack(spacing: 3) {
                    Image("icon_location_pin")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 20, height: 20)
                    Text(event.location)
                        .font(AppFont.medium(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .lineLimit(1)
                }
                .padding(.horizontal, 6)
                .padding(.vertical, 3)
                .background(
                    RoundedRectangle(cornerRadius: 7)
                        .fill(Color(hex: "F8F8F8"))
                )
                .padding(.top, 8)

                Spacer()

                HStack {
                    Spacer()
                    Text(event.price)
                        .font(AppFont.bold(18))
                        .foregroundColor(AppColor.primary)
                }
            }
            .frame(height: 168)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }
}

// MARK: - Empty Content

private struct EmptyContentView: View {
    var body: some View {
        Text("등록된 이벤트가 없습니다")
            .font(AppFont.regular(14))
            .foregroundColor(Color(hex: "8F8F8F"))
            .padding(.top, 80)
            .frame(minHeight: 300)
    }
}

// MARK: - Helpers

private func formatEventDate(_ dateStr: String) -> String {
    let formatter = ISO8601DateFormatter()
    formatter.formatOptions = [.withFullDate]

    let displayFormatter = DateFormatter()
    displayFormatter.dateFormat = "yyyy.MM.dd"

    if let date = formatter.date(from: String(dateStr.prefix(10))) {
        return displayFormatter.string(from: date)
    }
    return String(dateStr.prefix(10))
}

private func formattedCount(_ count: Int) -> String {
    let formatter = NumberFormatter()
    formatter.numberStyle = .decimal
    return formatter.string(from: NSNumber(value: count)) ?? "\(count)"
}

#Preview {
    EventHighlightView()
}
