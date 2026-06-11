//
//  MyPageView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct MyPageView: View {
    @StateObject private var viewModel = MyPageViewModel()
    @State private var showActivityHistory = false
    @State private var showProfileInfo = false
    @State private var showEditProfile = false
    @State private var showSettings = false
    @State private var showStoreRegister = false
    @State private var showMyCoupons = false
    @State private var showStampExchange = false
    @State private var showPrivacyPolicy = false
    @State private var showServiceTerms = false
    @StateObject private var stampViewModel = StampViewModel()
    var onLogout: () -> Void

    private var nickname: String {
        viewModel.userProfile?.nickname ?? "게스트"
    }
    private var email: String {
        viewModel.userProfile?.email ?? ""
    }
    private var stampCount: Int {
        viewModel.userProfile?.stampCount ?? 0
    }
    private var couponCount: Int {
        viewModel.userProfile?.couponCount ?? 0
    }

    var body: some View {
        if showPrivacyPolicy {
            PolicyDetailView(
                policyType: .privacy,
                onBackTapped: { showPrivacyPolicy = false }
            )
        } else if showServiceTerms {
            PolicyDetailView(
                policyType: .terms,
                onBackTapped: { showServiceTerms = false }
            )
        } else if showActivityHistory {
            ActivityHistoryView(onBackTapped: { showActivityHistory = false })
        } else if showProfileInfo {
            ProfileInfoView(
                onBackTapped: { showProfileInfo = false },
                onEditTapped: { showEditProfile = true },
                userProfile: viewModel.userProfile
            )
        } else if showEditProfile {
            EditProfileView(
                onBackTapped: {
                    showEditProfile = false
                    // 수정 후 프로필 정보 새로고침
                    Task { await viewModel.fetchProfile() }
                },
                onLogout: onLogout,
                initialNickname: viewModel.userProfile?.nickname ?? "",
                initialEmail: viewModel.userProfile?.email ?? "",
                initialName: viewModel.userProfile?.name,
                initialPhone: viewModel.userProfile?.phone,
                initialAddress: viewModel.userProfile?.address,
                initialAddressDetail: viewModel.userProfile?.addressDetail,
                initialProvider: viewModel.userProfile?.provider,
                initialBirthDate: viewModel.userProfile?.birthDate,
                initialGender: viewModel.userProfile?.gender,
                viewModel: viewModel
            )
        } else if showSettings {
            SettingsView(
                onBackTapped: { showSettings = false },
                onLogout: onLogout,
                viewModel: viewModel
            )
        } else if showStoreRegister {
            StoreRegisterView(onBackTapped: { showStoreRegister = false })
        } else if showMyCoupons {
            MyCouponsView(onBackTapped: { showMyCoupons = false })
        } else if showStampExchange {
            StampExchangeView(onBackTapped: { showStampExchange = false }, viewModel: stampViewModel)
        } else {
        ScrollView {
            VStack(spacing: 0) {
                // === Profile Section ===
                Spacer().frame(height: 20)

                HStack(spacing: 20) {
                    // Profile Avatar
                    Image("IconProfile")
                        .resizable()
                        .scaledToFill()
                        .frame(width: 80, height: 80)
                        .clipShape(Circle())

                    VStack(alignment: .leading, spacing: 4) {
                        Text("\(nickname)님")
                            .font(AppFont.extraBold(28))
                            .foregroundColor(Color(hex: "121212"))
                        Text(email)
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "6D919E"))
                    }

                    Spacer()
                }
                .padding(.horizontal, 20)

                Spacer().frame(height: 28)

                // === Stats Cards ===
                HStack(spacing: 12) {
                    // 획득 스탬프 (Gradient Blue)
                    Button { showStampExchange = true } label: {
                        ZStack(alignment: .topLeading) {
                            RoundedRectangle(cornerRadius: 15)
                                .fill(
                                    LinearGradient(
                                        colors: [
                                            Color(hex: "6092FF"),
                                            Color(hex: "2563EB"),
                                            Color(hex: "1551D3"),
                                        ],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(height: 90)

                            VStack {
                                Text("획득 스탬프")
                                    .font(AppFont.medium(14))
                                    .foregroundColor(.white.opacity(0.9))
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                Spacer()
                                Text("\(stampCount)")
                                    .font(AppFont.extraBold(33))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                            }
                            .padding(16)
                            .frame(height: 90)
                        }
                    }

                    // 보유 쿠폰 (White)
                    Button { showMyCoupons = true } label: {
                        ZStack(alignment: .topLeading) {
                            RoundedRectangle(cornerRadius: 15)
                                .fill(Color.white)
                                .frame(height: 90)

                            VStack {
                                Text("보유 쿠폰")
                                    .font(AppFont.medium(14))
                                    .foregroundColor(Color(hex: "121212").opacity(0.8))
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                Spacer()
                                Text("\(couponCount)")
                                    .font(AppFont.extraBold(33))
                                    .foregroundColor(AppColor.primary)
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                            }
                            .padding(16)
                            .frame(height: 90)
                        }
                    }
                }
                .padding(.horizontal, 20)

                Spacer().frame(height: 24)

                // === White Card with Menu Items ===
                VStack(spacing: 0) {
                    Spacer().frame(height: 16)

                    // Menu items (white background)
                    MenuItemRow(icon: "IconActivity", title: "활동이력") { showActivityHistory = true }
                    menuDivider
                    MenuItemRow(icon: "IconSettings", title: "설정") { showSettings = true }
                    menuDivider
                    MenuItemRow(icon: "IconEditProfile", title: "개인정보") { showProfileInfo = true }
                    menuDivider
                    MenuItemRow(icon: "IconStoreRegister", title: "상점 등록") { showStoreRegister = true }
                    menuDivider
                    MenuItemRow(icon: "IconSettings", title: "개인정보처리방침") { showPrivacyPolicy = true }
                    menuDivider
                    MenuItemRow(icon: "IconSettings", title: "이용약관") { showServiceTerms = true }

                    // Gray section (logout ~ version)
                    VStack(spacing: 0) {
                        Divider()
                            .background(Color(hex: "EDEDED"))
                            .padding(.horizontal, 20)

                        // Logout
                        Button {
                            viewModel.logout()
                            onLogout()
                        } label: {
                            HStack(spacing: 0) {
                                Image("IconLogout")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 21)
                                    .frame(width: 40, alignment: .center)
                                Spacer().frame(width: 8)
                                Text("로그아웃")
                                    .font(AppFont.regular(12))
                                    .foregroundColor(Color(hex: "8F8F8F"))
                                Spacer()
                            }
                            .padding(.horizontal, 20)
                            .padding(.vertical, 14)
                        }

                        Spacer().frame(minHeight: 40)

                        // App Version
                        Text("디지털 페스티벌 앱 v1.0.0")
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 20)
                    }
                    .background(Color(hex: "F8F8F8"))

                    // === Company Info Footer ===
                    CompanyInfoFooter(
                        onTermsTapped: { showServiceTerms = true },
                        onPrivacyTapped: { showPrivacyPolicy = true }
                    )
                }
                .background(Color.white)
                .clipShape(
                    UnevenRoundedRectangle(
                        topLeadingRadius: 22,
                        bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0,
                        topTrailingRadius: 33
                    )
                )
            }
        }
        .background(Color(hex: "EDF7FF"))
        .scrollContentBackground(.hidden)
        .scrollIndicators(.hidden)
        .task {
            await viewModel.fetchProfile()
        }
        } // end if showActivityHistory else
    }

    private var menuDivider: some View {
        Divider()
            .background(Color(hex: "EDEDED"))
            .padding(.horizontal, 20)
    }
}

struct ProfileInfoView: View {
    var onBackTapped: () -> Void
    var onEditTapped: () -> Void
    var userProfile: UserProfileData?

    private var genderText: String {
        switch userProfile?.gender {
        case "male": return "남성"
        case "female": return "여성"
        case "other": return "기타"
        default: return "-"
        }
    }

    private var addressText: String {
        let addr = userProfile?.address ?? ""
        let detail = userProfile?.addressDetail ?? ""
        var result = ""
        if !addr.isEmpty { result += addr }
        if !detail.isEmpty {
            if !result.isEmpty { result += " " }
            result += detail
        }
        return result.isEmpty ? "-" : result
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
                Text("개인정보")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .frame(height: 50)

            Divider()
                .background(Color(hex: "E2E2E2"))

            // === Scrollable Content ===
            ScrollView {
                VStack(spacing: 0) {
                    Spacer().frame(height: 24)

                    VStack(spacing: 20) {
                        ProfileInfoRow(label: "이름", value: userProfile?.name ?? "-")
                        ProfileInfoRow(label: "닉네임", value: userProfile?.nickname ?? "-")
                        ProfileInfoRow(label: "이메일", value: userProfile?.email ?? "-")
                        ProfileInfoRow(label: "휴대폰", value: userProfile?.phone ?? "-")
                        ProfileInfoRow(label: "주소", value: addressText)
                        ProfileInfoRow(label: "생년월일", value: userProfile?.birthDate ?? "-")
                        ProfileInfoRow(label: "성별", value: genderText)
                    }
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 32)

                    // === 수정 Button ===
                    Button(action: onEditTapped) {
                        Text("수정")
                            .font(AppFont.semibold(16))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(
                                LinearGradient(
                                    colors: [
                                        Color(hex: "6092FF"),
                                        Color(hex: "2563EB"),
                                        Color(hex: "1551D3"),
                                    ],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 32)
                }
            }
        }
        .background(Color.white)
    }
}

private struct ProfileInfoRow: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "8F8F8F"))
            Text(value)
                .font(AppFont.regular(16))
                .foregroundColor(Color(hex: "121212"))
            Spacer().frame(height: 6)
            Divider()
                .background(Color(hex: "EDEDED"))
        }
    }
}

private struct MenuItemRow: View {
    let icon: String
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(height: 28)
                    .frame(width: 40, alignment: .center)
                Text(title)
                    .font(AppFont.medium(14))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
                Text(">")
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
        }
    }
}

private struct CompanyInfoFooter: View {
    var onTermsTapped: () -> Void
    var onPrivacyTapped: () -> Void

    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(spacing: 0) {
            // Top separator
            Divider()
                .background(Color(hex: "E2E2E2"))

            VStack(alignment: .leading, spacing: 0) {
                // Company name
                Text("(주)디플로")
                    .font(AppFont.semibold(13))
                    .foregroundColor(Color(hex: "666666"))

                Spacer().frame(height: 12)

                // Address
                Text("주소 : 대구광역시 수성구 알파시티 1로 42길 11, 1024호 태왕알파시티수성(대흥동)")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))
                    .lineSpacing(3)
                Spacer().frame(height: 2)
                Text("이메일 : contact@di-flo.com")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))
                Spacer().frame(height: 2)
                Text("고객문의 : 070-4798-9299")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))

                Spacer().frame(height: 12)

                Text("대표자 : 하다인")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))
                Spacer().frame(height: 2)
                Text("사업자등록번호 : 892-86-03341")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))

                Spacer().frame(height: 12)

                // Disclaimer
                Text("본 서비스는 위치 기반 스탬프 투어 플랫폼으로, 참여 상점 및 기관이 제공하는 이벤트 및 정보에 대한 책임은 해당 제공자에게 있습니다.")
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))
                    .lineSpacing(3)

                Spacer().frame(height: 16)

                // Links row
                HStack(spacing: 0) {
                    Spacer()
                    Button(action: onTermsTapped) {
                        Text("이용약관")
                            .font(AppFont.medium(11))
                            .foregroundColor(Color(hex: "6B7280"))
                    }
                    Text(" | ")
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "A0A0A0"))
                    Button(action: onPrivacyTapped) {
                        Text("개인정보처리방침")
                            .font(AppFont.medium(11))
                            .foregroundColor(Color(hex: "6B7280"))
                    }
                    Text(" | ")
                        .font(AppFont.regular(11))
                        .foregroundColor(Color(hex: "A0A0A0"))
                    Button {
                        if let url = URL(string: "mailto:contact@di-flo.com") {
                            openURL(url)
                        }
                    } label: {
                        Text("문의하기")
                            .font(AppFont.medium(11))
                            .foregroundColor(Color(hex: "6B7280"))
                    }
                    Spacer()
                }

                Spacer().frame(height: 16)

                // Copyright
                Text("\u{00A9} (주)디플로")
                    .font(AppFont.regular(10))
                    .foregroundColor(Color(hex: "AFBFCC"))
                    .frame(maxWidth: .infinity, alignment: .center)

                Spacer().frame(height: 80)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 24)
        }
        .background(Color(hex: "F5F5F5"))
    }
}

#Preview {
    MyPageView(onLogout: {})
}
