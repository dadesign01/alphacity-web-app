import SwiftUI

enum Tab: Int, CaseIterable {
    case home, map, stamp, mypage

    var title: String {
        switch self {
        case .home: return "홈"
        case .map: return "지도"
        case .stamp: return "스탬프"
        case .mypage: return "마이페이지"
        }
    }

    var icon: String {
        switch self {
        case .home: return "TabHome"
        case .map: return "TabMap"
        case .stamp: return "TabStamp"
        case .mypage: return "TabMypage"
        }
    }
}

// '지도에서 보기'로 지도 탭에 진입했을 때, 뒤로가기 시 복원할 직전 상세 화면
private enum MapBackTarget {
    case program(ProgramData)
    case festival(FestivalData)
    case store(StoreData)
    case stamp
}

struct MainTabView: View {
    @Binding var deepLinkProgramId: Int?
    var onLogout: () -> Void = {}
    var onNavigateToRegister: () -> Void = {}
    @State private var selectedTab: Tab = .home
    @State private var showProgramList = false
    @State private var programListCategory: String? = nil
    @State private var showEventHighlight = false
    @State private var showMyCoupons = false
    @State private var showStampExchange = false
    @StateObject private var stampViewModel = StampViewModel()
    @State private var selectedProgram: ProgramData? = nil
    @State private var selectedFestival: FestivalData? = nil
    @State private var selectedStore: StoreData? = nil
    @State private var showGuestDialog = false
    @State private var mapFocusLat: Double? = nil
    @State private var mapFocusLng: Double? = nil
    // 지도/스탬프 탭 진입 맥락 (뒤로가기 복원용)
    @State private var mapBackTarget: MapBackTarget? = nil
    @State private var stampFromHome = false

    private var isGuest: Bool { !TokenManager.shared.isLoggedIn }

    // 지도 → 직전 상세 복원
    private func restoreFromMap() {
        switch mapBackTarget {
        case .program(let p): selectedProgram = p
        case .festival(let f): selectedFestival = f
        case .store(let s): selectedStore = s
        case .stamp: selectedTab = .stamp
        case .none: break
        }
        mapBackTarget = nil
    }

    // 배너 클릭 → 연결 대상(linkType/linkId) 상세로 이동
    private func handleBannerTap(_ banner: BannerData) {
        switch banner.linkType {
        case "event":
            showEventHighlight = true
        case "program":
            guard let id = banner.linkId else { return }
            Task { @MainActor in
                if let program = try? await HomeRepository.shared.fetchProgramById(id) {
                    selectedProgram = program
                }
            }
        case "festival":
            guard let id = banner.linkId else { return }
            Task { @MainActor in
                if let list = try? await HomeRepository.shared.fetchFestivals(),
                   let festival = list.first(where: { $0.id == id }) {
                    selectedFestival = festival
                }
            }
        default:
            break
        }
    }

    var body: some View {
        if let store = selectedStore {
            ProgramDetailView(
                program: store.toProgramData(),
                onBackTapped: {
                    selectedStore = nil
                },
                onNavigateToMap: { lat, lng in
                    mapBackTarget = .store(store)
                    selectedStore = nil
                    mapFocusLat = lat
                    mapFocusLng = lng
                    selectedTab = .map
                }
            )
        } else if let program = selectedProgram {
            ProgramDetailView(
                program: program,
                onBackTapped: {
                    selectedProgram = nil
                    deepLinkProgramId = nil
                },
                onNavigateToMap: { lat, lng in
                    mapBackTarget = .program(program)
                    selectedProgram = nil
                    mapFocusLat = lat
                    mapFocusLng = lng
                    selectedTab = .map
                }
            )
        } else if let festival = selectedFestival {
            FestivalDetailView(
                festival: festival,
                onBackTapped: { selectedFestival = nil },
                onSeeAllPrograms: { selectedFestival = nil; showProgramList = true },
                onProgramTapped: { p in selectedProgram = p },
                onNavigateToMap: { lat, lng in
                    mapBackTarget = .festival(festival)
                    selectedFestival = nil
                    mapFocusLat = lat
                    mapFocusLng = lng
                    selectedTab = .map
                }
            )
        } else if showProgramList {
            ProgramListView(
                initialCategory: programListCategory,
                onBackTapped: { showProgramList = false; programListCategory = nil },
                onProgramTapped: { program in selectedProgram = program }
            )
        } else if showEventHighlight {
            EventHighlightView(
                onBackTapped: { showEventHighlight = false }
            )
        } else if showMyCoupons {
            MyCouponsView(
                onBackTapped: { showMyCoupons = false }
            )
        } else if showStampExchange {
            StampExchangeView(
                onBackTapped: { showStampExchange = false },
                onNavigateToCoupons: {
                    showStampExchange = false
                    showMyCoupons = true
                },
                viewModel: stampViewModel
            )
        } else {
        VStack(spacing: 0) {
            // Content
            Group {
                switch selectedTab {
                case .home:
                    HomeView(
                        onNavigateToMyPage: {
                            if isGuest { showGuestDialog = true }
                            else { selectedTab = .mypage }
                        },
                        onNavigateToProgramList: { showProgramList = true },
                        onNavigateToProgramListWithCategory: { category in
                            programListCategory = category
                            showProgramList = true
                        },
                        onNavigateToEventHighlight: { showEventHighlight = true },
                        onNavigateToMap: { selectedTab = .map },
                        onNavigateToStamp: { stampFromHome = true; selectedTab = .stamp },
                        onNavigateToCoupons: { showMyCoupons = true },
                        onProgramClick: { program in selectedProgram = program },
                        onFestivalClick: { festival in selectedFestival = festival },
                        onBannerClick: { banner in handleBannerTap(banner) },
                        onGuestRestricted: { showGuestDialog = true },
                        isGuest: isGuest
                    )
                case .map:
                    MapContentView(
                        onProgramTapped: { program in selectedProgram = program },
                        onStoreTapped: { store in selectedStore = store },
                        onProfileTap: { selectedTab = .mypage },
                        focusLat: $mapFocusLat,
                        focusLng: $mapFocusLng,
                        showBack: mapBackTarget != nil,
                        onBack: { restoreFromMap() }
                    )
                case .stamp:
                    StampView(
                        onNavigateToMap: { lat, lng in
                            mapBackTarget = .stamp
                            mapFocusLat = lat
                            mapFocusLng = lng
                            selectedTab = .map
                        },
                        onNavigateToExchange: { showStampExchange = true },
                        onProfileTap: {
                            if isGuest { showGuestDialog = true }
                            else { selectedTab = .mypage }
                        },
                        showBack: stampFromHome,
                        onBack: { stampFromHome = false; selectedTab = .home }
                    )
                case .mypage:
                    MyPageView(onLogout: onLogout)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Tab Bar
            Divider()
                .foregroundColor(Color(hex: "E5E7EB"))

            HStack(spacing: 0) {
                ForEach(Array(Tab.allCases.enumerated()), id: \.element) { index, tab in
                    Button {
                        if isGuest && tab != .home {
                            showGuestDialog = true
                        } else {
                            // 하단 탭 직접 이동은 진입 맥락 초기화 (탭 루트 동작)
                            mapBackTarget = nil
                            stampFromHome = false
                            selectedTab = tab
                        }
                    } label: {
                        VStack(spacing: 4) {
                            Image(tab.icon)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 24, height: 24)
                                .saturation(selectedTab == tab ? 1.0 : 0.0)
                                .opacity(selectedTab == tab ? 1.0 : 0.6)

                            Text(tab.title)
                                .font(AppFont.medium(10))
                                .foregroundColor(selectedTab == tab ? AppColor.primary : Color(hex: "999999"))
                        }
                        .frame(minWidth: 56)
                    }

                    if index < Tab.allCases.count - 1 {
                        Spacer()
                    }
                }
            }
            .padding(.horizontal, 28)
            .padding(.top, 8)
            .padding(.bottom, 20)
            .background(Color.white)
        }
        .edgesIgnoringSafeArea(.bottom)
        .alert("회원 전용 기능", isPresented: $showGuestDialog) {
            Button("로그인") {
                showGuestDialog = false
                onNavigateToRegister()
            }
            Button("취소", role: .cancel) {
                showGuestDialog = false
            }
        } message: {
            Text("로그인 후 이용하실 수 있습니다.\n로그인 페이지로 이동하시겠습니까?")
        }
        .onChange(of: deepLinkProgramId) { _, newId in
            guard let programId = newId else { return }
            Task {
                do {
                    let program = try await HomeRepository.shared.fetchProgramById(programId)
                    selectedProgram = program
                } catch {
                    print("Deep link program fetch failed: \(error)")
                    deepLinkProgramId = nil
                }
            }
        }
        } // end if showProgramList else
    }
}

#Preview {
    MainTabView(deepLinkProgramId: .constant(nil))
}
