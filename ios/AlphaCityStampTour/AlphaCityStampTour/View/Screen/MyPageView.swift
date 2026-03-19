//
//  MyPageView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct MyPageView: View {
    @StateObject private var viewModel = MyPageViewModel()
    @State private var showActivityHistory = false
    @State private var showEditProfile = false
    @State private var showSettings = false
    @State private var showStoreRegister = false
    @State private var showMyCoupons = false
    @State private var showStampExchange = false
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
        if showActivityHistory {
            ActivityHistoryView(onBackTapped: { showActivityHistory = false })
        } else if showEditProfile {
            EditProfileView(
                onBackTapped: { showEditProfile = false },
                onLogout: onLogout,
                initialNickname: viewModel.userProfile?.nickname ?? "",
                initialEmail: viewModel.userProfile?.email ?? "",
                initialName: viewModel.userProfile?.name,
                initialPhone: viewModel.userProfile?.phone,
                initialAddress: viewModel.userProfile?.address,
                initialAddressDetail: viewModel.userProfile?.addressDetail,
                initialProvider: viewModel.userProfile?.provider,
                viewModel: viewModel
            )
        } else if showSettings {
            SettingsView(onBackTapped: { showSettings = false })
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
                    MenuItemRow(icon: "IconEditProfile", title: "개인정보 수정") { showEditProfile = true }
                    menuDivider
                    MenuItemRow(icon: "IconStoreRegister", title: "상점 등록") { showStoreRegister = true }

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

#Preview {
    MyPageView(onLogout: {})
}
