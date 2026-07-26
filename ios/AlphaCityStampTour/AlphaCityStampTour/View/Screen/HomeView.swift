import SwiftUI

struct HomeView: View {
    @State private var currentBannerIndex = 0
    @StateObject private var viewModel = HomeViewModel()
    private let bannerTimer = Timer.publish(every: 4, on: .main, in: .common).autoconnect()
    var onNavigateToMyPage: (() -> Void)?
    var onNavigateToProgramList: (() -> Void)?
    var onNavigateToProgramListWithCategory: ((String) -> Void)?
    var onNavigateToEventHighlight: (() -> Void)?
    var onNavigateToMap: (() -> Void)?
    var onNavigateToStamp: (() -> Void)?
    var onNavigateToCoupons: (() -> Void)?
    var onProgramClick: ((ProgramData) -> Void)?
    var onFestivalClick: ((FestivalData) -> Void)?
    var onBannerClick: ((BannerData) -> Void)?
    var onGuestRestricted: (() -> Void)?
    var isGuest: Bool = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HomeHeaderView(onProfileTap: { onNavigateToMyPage?() })

            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // Banner Carousel
                    BannerCarouselView(
                        banners: viewModel.banners,
                        currentIndex: $currentBannerIndex,
                        onBannerTapped: { banner in onBannerClick?(banner) }
                    )
                    .onReceive(bannerTimer) { _ in
                        let count = max(viewModel.banners.count, 1)
                        withAnimation {
                            currentBannerIndex = (currentBannerIndex + 1) % count
                        }
                    }

                    // 진행중인 축제
                    FestivalSectionView(
                        festivals: viewModel.festivals,
                        onSeeAllTapped: { onNavigateToProgramList?() },
                        onFestivalTapped: { festival in onFestivalClick?(festival) }
                    )

                    // 알파시티 이벤트
                    EventSectionView(
                        events: viewModel.events,
                        onSeeAllTapped: { onNavigateToEventHighlight?() },
                        onEventTapped: { onNavigateToEventHighlight?() }
                    )

                    // 나의 스탬프 진행률
                    StampProgressSectionView(
                        stampCount: viewModel.userStampCount,
                        totalStamps: viewModel.totalStampCount,
                        onStampTapped: {
                            if isGuest { onGuestRestricted?() } else { onNavigateToStamp?() }
                        }
                    )

                    // 지도보기 + 쿠폰함 + Footer (gray background area)
                    VStack(spacing: 0) {
                        QuickMenuSectionView(
                            onMapTapped: {
                                if isGuest { onGuestRestricted?() } else { onNavigateToMap?() }
                            },
                            onCouponTapped: {
                                if isGuest { onGuestRestricted?() } else { onNavigateToCoupons?() }
                            }
                        )

                        Text("2026 OLLYMOA. All rights reserved.")
                            .font(AppFont.regular(11))
                            .foregroundColor(Color(hex: "999999"))
                            .padding(.vertical, 24)
                    }
                    .background(AppColor.background)
                }
            }
        }
        .background(Color.white)
        .onAppear {
            viewModel.fetchHomeData()
        }
    }
}

// MARK: - Header

private struct HomeHeaderView: View {
    var onProfileTap: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Image("HeaderLogo")
                .resizable()
                .scaledToFill()
                .frame(width: 40, height: 40)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            Text("올리모아")
                .font(AppFont.semibold(18))
                .foregroundColor(Color(hex: "121212"))
                .tracking(-0.36)

            Spacer()

            Button(action: onProfileTap) {
                Image("IconProfile")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 40, height: 40)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(Color(hex: "EBEBEB"), lineWidth: 1)
                    )
            }
        }
        .padding(.leading, 15)
        .padding(.trailing, 20)
        .padding(.vertical, 12)
        .background(Color.white)
    }
}

// MARK: - Banner Carousel

private struct BannerCarouselView: View {
    let banners: [BannerData]
    @Binding var currentIndex: Int
    var onBannerTapped: ((BannerData) -> Void)?

    var body: some View {
        if banners.isEmpty {
            // 배너 없을 때 placeholder
            RoundedRectangle(cornerRadius: 15)
                .fill(Color(hex: "EDF7FF"))
                .frame(height: 150)
                .padding(.horizontal, 20)
                .padding(.top, 12)
        } else {
            ZStack(alignment: .bottom) {
                TabView(selection: $currentIndex) {
                    ForEach(Array(banners.enumerated()), id: \.element.id) { index, banner in
                        let fullURL = banner.imageUrl.hasPrefix("http")
                            ? banner.imageUrl
                            : "\(APIClient.serverURL)\(banner.imageUrl)"

                        AsyncImage(url: URL(string: fullURL)) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .scaledToFill()
                                    .frame(height: 150)
                                    .clipped()
                            case .failure:
                                Color(hex: "EDF7FF")
                                    .frame(height: 150)
                            default:
                                Color(hex: "EDF7FF")
                                    .frame(height: 150)
                            }
                        }
                        .contentShape(Rectangle())
                        .onTapGesture { onBannerTapped?(banner) }
                        .tag(index)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .frame(height: 150)
                .animation(.easeInOut, value: currentIndex)

                // Paging dots overlay
                HStack(spacing: 6) {
                    ForEach(0..<banners.count, id: \.self) { index in
                        Circle()
                            .fill(Color.white.opacity(index == currentIndex ? 1.0 : 0.5))
                            .frame(width: 8, height: 8)
                    }
                }
                .padding(.bottom, 12)
            }
            .clipShape(RoundedRectangle(cornerRadius: 15))
            .padding(.horizontal, 20)
            .padding(.top, 12)
        }
    }
}

// MARK: - 진행중인 축제

private struct FestivalSectionView: View {
    let festivals: [FestivalData]
    var onSeeAllTapped: (() -> Void)?
    var onFestivalTapped: ((FestivalData) -> Void)?

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                Text("진행중인\n전국의 축제·공연·전시")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
                Button(action: { onSeeAllTapped?() }) {
                    Text("보러가기  >")
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "121212"))
                }
            }
            .padding(.horizontal, 20)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    if festivals.isEmpty {
                        FestivalCardView(image: "ProgramImg1", imageUrl: nil, name: "수성알파시티 알파위크", tag: "#진행중", address: nil) {}
                        FestivalCardView(image: "ProgramImg2", imageUrl: nil, name: "2026 대구 치맥페스티벌", tag: "#예정", address: nil) {}
                    } else {
                        ForEach(festivals, id: \.id) { festival in
                            let tag: String = {
                                switch festival.status {
                                case "in_progress": return "#진행중"
                                case "scheduled": return "#예정"
                                default: return "#종료"
                                }
                            }()
                            FestivalCardView(
                                image: "ProgramImg1",
                                imageUrl: festival.bannerUrl ?? festival.imageUrl,
                                name: festival.name,
                                tag: tag,
                                address: festival.address,
                                onTap: { onFestivalTapped?(festival) }
                            )
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 2)
            }
        }
        .padding(.top, 20)
    }
}

private struct FestivalCardView: View {
    let image: String
    var imageUrl: String? = nil
    let name: String
    let tag: String
    let address: String?
    var onTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if let imageUrl = imageUrl, !imageUrl.isEmpty {
                let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                AsyncImage(url: URL(string: fullURL)) { phase in
                    switch phase {
                    case .success(let img): img.resizable().scaledToFill()
                    default: Image(image).resizable().scaledToFill()
                    }
                }
                .frame(width: 260, height: 150)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                Image(image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 260, height: 150)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            Text(name)
                .font(AppFont.semibold(15))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 8)

            HStack(spacing: 4) {
                TagChip(text: tag, textColor: "E85151", borderColor: "E85151")
                if let addr = address, !addr.isEmpty {
                    Text(addr)
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .lineLimit(1)
                }
            }
            .padding(.top, 4)
        }
        .frame(width: 260)
        .contentShape(Rectangle())
        .onTapGesture { onTap() }
    }
}

private struct ProgramCardView: View {
    let image: String
    var imageUrl: String? = nil
    let name: String
    let tag: String

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if let imageUrl = imageUrl, !imageUrl.isEmpty {
                let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                AsyncImage(url: URL(string: fullURL)) { phase in
                    switch phase {
                    case .success(let img):
                        img.resizable().scaledToFill()
                    default:
                        Image(image).resizable().scaledToFill()
                    }
                }
                .frame(width: 168, height: 168)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                Image(image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 168, height: 168)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            Text(name)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 8)

            HStack(spacing: 4) {
                TagChip(text: tag, textColor: "E85151", borderColor: "E85151")
                TagChip(text: "#쿠폰가능", textColor: "4C27D0", borderColor: "4C27D0")
            }
            .padding(.top, 4)
        }
        .frame(width: 168)
    }
}

// MARK: - 알파시티 이벤트

private struct EventSectionView: View {
    let events: [EventData]
    var onSeeAllTapped: (() -> Void)?
    var onEventTapped: (() -> Void)?

    private static let fallbackEvents: [(image: String, name: String, date: String, tags: [(text: String, color: String, border: String)])] = [
        ("EventImg2", "갤럭시탭 추첨 이벤트", "26.04.02 ~ 26.06.05", [
            ("#추첨이벤트", "E85151", "E85151"),
            ("#스탬프 10개", "5182FF", "5182FF"),
        ]),
        ("EventImg3", "VR 헤드셋 이벤트", "26.04.02 ~ 26.06.05", [
            ("#추첨이벤트", "E85151", "E85151"),
            ("#스탬프 15개", "5182FF", "5182FF"),
        ]),
        ("EventImg4", "에코백 증정 이벤트", "26.04.02 ~ 26.06.05", [
            ("#선착순", "E85151", "E85151"),
            ("#스탬프 2개", "5182FF", "5182FF"),
        ]),
    ]

    private func formatDate(_ dateStr: String) -> String {
        // Server returns ISO8601, format to "YY.MM.DD"
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        guard let date = formatter.date(from: String(dateStr.prefix(10))) else { return dateStr }
        let display = DateFormatter()
        display.dateFormat = "yy.MM.dd"
        return display.string(from: date)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("알파시티 이벤트")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
                Button(action: { onSeeAllTapped?() }) {
                    Text("보러가기  >")
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "121212"))
                }
            }
            .padding(.horizontal, 20)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    if events.isEmpty {
                        ForEach(Self.fallbackEvents.indices, id: \.self) { index in
                            let event = Self.fallbackEvents[index]
                            EventCardView(
                                image: event.image,
                                name: event.name,
                                date: event.date,
                                tags: event.tags
                            )
                        }
                    } else {
                        ForEach(events, id: \.id) { event in
                            let typeTag = event.type == "raffle" ? "#추첨이벤트" : "#선착순"
                            let dateStr = "\(formatDate(event.startDate)) ~ \(formatDate(event.endDate))"
                            EventCardView(
                                image: "EventImg2",
                                imageUrl: event.imageUrl,
                                name: event.name,
                                date: dateStr,
                                tags: [
                                    (typeTag, "E85151", "E85151"),
                                    ("#\(event.reward ?? "보상")", "5182FF", "5182FF"),
                                ]
                            )
                            .onTapGesture { onEventTapped?() }
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 2)
            }
        }
        .padding(.top, 28)
    }
}

private struct EventCardView: View {
    let image: String
    var imageUrl: String? = nil
    let name: String
    let date: String
    let tags: [(text: String, color: String, border: String)]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            ZStack {
                RoundedRectangle(cornerRadius: 22)
                    .fill(Color(hex: "D9D9D9"))
                    .frame(width: 131, height: 131)

                if let imageUrl = imageUrl, !imageUrl.isEmpty {
                    let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                    AsyncImage(url: URL(string: fullURL)) { phase in
                        switch phase {
                        case .success(let img):
                            img.resizable().scaledToFill()
                        default:
                            Image(image).resizable().scaledToFill()
                        }
                    }
                    .frame(width: 168, height: 168)
                    .clipped()
                } else {
                    Image(image)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 168, height: 168)
                        .clipped()
                }
            }
            .frame(width: 168, height: 168)

            Text(name)
                .font(AppFont.semibold(14))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 8)

            Text(date)
                .font(AppFont.regular(12))
                .foregroundColor(Color(hex: "121212"))
                .padding(.top, 2)

            HStack(spacing: 4) {
                ForEach(tags.indices, id: \.self) { i in
                    TagChip(text: tags[i].text, textColor: tags[i].color, borderColor: tags[i].border)
                }
            }
            .padding(.top, 4)
        }
        .frame(width: 168)
    }
}

// MARK: - Tag Chip

private struct TagChip: View {
    let text: String
    let textColor: String
    let borderColor: String

    var body: some View {
        Text(text)
            .font(AppFont.medium(9))
            .foregroundColor(Color(hex: textColor))
            .padding(.horizontal, 7)
            .padding(.vertical, 3)
            .overlay(
                RoundedRectangle(cornerRadius: 100)
                    .stroke(Color(hex: borderColor), lineWidth: 1)
            )
    }
}

// MARK: - 나의 스탬프 진행률

private struct StampProgressSectionView: View {
    let stampCount: Int
    let totalStamps: Int
    var onStampTapped: (() -> Void)?

    private var progress: Double {
        guard totalStamps > 0 else { return 0 }
        return min(Double(stampCount) / Double(totalStamps), 1.0)
    }

    private var percentText: String {
        "\(Int(progress * 100))%"
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 22)
                .fill(Color(hex: "EDF7FF"))

            VStack(spacing: 0) {
                HStack {
                    Text("나의 스탬프 진행률")
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                    Spacer()
                    Text(percentText)
                        .font(AppFont.extraBold(28))
                        .foregroundColor(AppColor.primary)
                }

                Spacer().frame(height: 6)

                HStack {
                    Spacer()
                    Text("\(stampCount) / \(totalStamps)")
                        .font(AppFont.medium(14))
                        .foregroundColor(Color(hex: "888888"))
                }

                Spacer().frame(height: 14)

                // Progress Bar + Character
                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        // Bar track
                        RoundedRectangle(cornerRadius: 100)
                            .fill(Color.white)
                            .frame(height: 10)
                            .offset(y: 27)

                        // Bar fill
                        RoundedRectangle(cornerRadius: 100)
                            .fill(Color(hex: "26AE3F"))
                            .frame(width: max(geometry.size.width * progress, 0), height: 10)
                            .offset(y: 27)

                        // Character on bar
                        Image("Walk")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 36, height: 37)
                            .offset(x: max(geometry.size.width * progress - 18, 0))
                    }
                }
                .frame(height: 47)

                Spacer().frame(height: 16)

                // Button inside card
                Button(action: { onStampTapped?() }) {
                    Text("나의 스탬프 보러가기  >")
                        .font(AppFont.semibold(16))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(AppColor.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 13))
                }
            }
            .padding(.horizontal, 31)
            .padding(.vertical, 26)
        }
        .padding(.horizontal, 20)
        .padding(.top, 28)
    }
}

// MARK: - Quick Menu (지도보기 + 쿠폰함)

private struct QuickMenuSectionView: View {
    var onMapTapped: (() -> Void)?
    var onCouponTapped: (() -> Void)?

    var body: some View {
        HStack(spacing: 10) {
            QuickMenuCard(title: "지도보기", image: "IconMapview", onTapped: onMapTapped)
            QuickMenuCard(title: "쿠폰함", image: "IconCoupon", onTapped: onCouponTapped)
        }
        .padding(.horizontal, 20)
        .padding(.top, 28)
    }
}

private struct QuickMenuCard: View {
    let title: String
    let image: String
    var onTapped: (() -> Void)?

    var body: some View {
        Button(action: { onTapped?() }) {
            ZStack(alignment: .topLeading) {
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.white)
                    .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)

                VStack(alignment: .leading) {
                    Text(title)
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                        .padding(.top, 20)
                        .padding(.leading, 18)

                    Spacer()

                    HStack {
                        Spacer()
                        Image(image)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 57, height: 80)
                            .padding(.trailing, 10)
                            .padding(.bottom, 10)
                    }
                }
            }
            .frame(height: 168)
        }
    }
}

#Preview {
    HomeView()
}
