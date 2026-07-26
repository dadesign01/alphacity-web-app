//
//  SettingsView.swift
//  AlphaCityStampTour
//

import SwiftUI

enum PolicyType: String {
    case terms, privacy, location

    var title: String {
        switch self {
        case .terms: return "서비스 이용약관"
        case .privacy: return "개인정보 처리방침"
        case .location: return "위치 정보 이용약관"
        }
    }
}

struct SettingsView: View {
    var onBackTapped: () -> Void
    var onLogout: () -> Void = {}
    @ObservedObject var viewModel: MyPageViewModel

    @State private var showPolicy: PolicyType? = nil
    @State private var showDeleteAlert = false
    @State private var notifyNewEvent = true
    @State private var notifyEventUpdate = true
    @State private var notifyMission = true
    @State private var notifyMarketing = false
    @State private var locationConsent = true

    var body: some View {
        if let policy = showPolicy {
            PolicyDetailView(
                policyType: policy,
                onBackTapped: { showPolicy = nil }
            )
        } else {
            settingsContent
        }
    }

    private var settingsContent: some View {
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
                Text("설정")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .frame(height: 50)

            Divider()
                .background(Color(hex: "E2E2E2"))

            // === Content ===
            ScrollView {
                VStack(spacing: 0) {
                    // Notice banner
                    HStack(spacing: 9) {
                        Image("IconBell")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 18, height: 18)
                        Text("알림 허용 시 행사 정보를 놓치지 않을 수 있습니다.")
                            .font(AppFont.regular(14))
                            .foregroundColor(Color(hex: "121212"))
                    }
                    .padding(.horizontal, 15)
                    .padding(.vertical, 7)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(
                        RoundedRectangle(cornerRadius: 11)
                            .fill(Color(hex: "F8F8F8"))
                    )
                    .padding(.horizontal, 20)
                    .padding(.vertical, 6)

                    // === Notification Toggles ===
                    SettingsToggleRow(
                        title: "새로운 행사 알림",
                        subtitle: "수성알파시티 내 새로운 행사 진행 시 알림 받기",
                        isOn: $notifyNewEvent
                    )
                    settingsDivider
                    SettingsToggleRow(
                        title: "이벤트 업데이트 알림",
                        subtitle: "참여 중인 이벤트 변경 시 알림 받기",
                        isOn: $notifyEventUpdate
                    )
                    settingsDivider
                    SettingsToggleRow(
                        title: "미션 알림",
                        subtitle: "새로운 미션 등록 시 알림 받기",
                        isOn: $notifyMission
                    )
                    settingsDivider
                    SettingsToggleRow(
                        title: "마케팅 알림",
                        subtitle: "프로모션 및 혜택 정보 알림 받기",
                        isOn: $notifyMarketing
                    )

                    // === 권한 설정 ===
                    settingsDivider
                    sectionHeader("권한 설정")
                    settingsDivider
                    SettingsToggleRow(
                        title: "위치 정보 이용 동의",
                        subtitle: "위치 기반 미션 참여에 사용됩니다.",
                        isOn: $locationConsent
                    )

                    // === 약관 및 정책 ===
                    settingsDivider
                    sectionHeader("약관 및 정책")
                    settingsDivider
                    SettingsNavRow(title: "서비스 이용약관") { showPolicy = .terms }
                    settingsDivider
                    SettingsNavRow(title: "개인정보 처리방침") { showPolicy = .privacy }
                    settingsDivider
                    SettingsNavRow(title: "위치 정보 이용약관") { showPolicy = .location }

                    // === 계정 ===
                    if viewModel.isLoggedIn {
                        settingsDivider
                        sectionHeader("계정")
                        settingsDivider
                        SettingsNavRow(title: "회원탈퇴") { showDeleteAlert = true }
                    }

                    // === 앱 정보 ===
                    settingsDivider
                    sectionHeader("앱 정보")

                    // 앱 버전
                    VStack(spacing: 8) {
                        HStack {
                            Text("앱 버전")
                                .font(AppFont.medium(16))
                                .foregroundColor(Color(hex: "121212"))
                            Spacer()
                            Text("v1.0.0")
                                .font(AppFont.semibold(14))
                                .foregroundColor(AppColor.primary)
                        }
                        HStack {
                            Text("현재 사용 중인 버전입니다. ")
                                .font(AppFont.regular(12))
                                .foregroundColor(Color(hex: "595959"))
                            Spacer()
                            Button {
                                // TODO: update
                            } label: {
                                Text("업데이트 하기")
                                    .font(AppFont.regular(10))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 9)
                                    .padding(.vertical, 3)
                                    .background(
                                        Capsule().fill(Color(hex: "121212"))
                                    )
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)

                    settingsDivider

                    // Security notice
                    Text("올리모아 앱은 안전한 사용자 경험을 위해\n최신 보안 기술을 적용하고 있습니다.")
                        .font(AppFont.regular(10))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .lineSpacing(4)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 11)
                                .fill(Color(hex: "F8F8F8"))
                        )
                        .padding(.horizontal, 20)
                        .padding(.vertical, 12)

                    // Footer
                    Text("2026 OLLYMOA. All rights reserved.")
                        .font(AppFont.regular(10))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                        .background(Color(hex: "F9F9F9"))
                }
            }
        }
        .background(Color.white)
        .alert("회원탈퇴", isPresented: $showDeleteAlert) {
            Button("취소", role: .cancel) {}
            Button("탈퇴", role: .destructive) {
                Task { await viewModel.deleteAccount() }
            }
        } message: {
            Text("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
        }
        .alert(viewModel.deleteError ?? "", isPresented: Binding(
            get: { viewModel.deleteError != nil },
            set: { if !$0 { viewModel.clearDeleteState() } }
        )) {
            Button("확인") { viewModel.clearDeleteState() }
        }
        .onChange(of: viewModel.deleteSuccess) { _, success in
            if success {
                viewModel.clearDeleteState()
                onLogout()
            }
        }
    }

    private var settingsDivider: some View {
        Divider()
            .background(Color(hex: "B5B5B5"))
    }

    private func sectionHeader(_ title: String) -> some View {
        HStack {
            Text(title)
                .font(AppFont.regular(10))
                .foregroundColor(Color(hex: "8F8F8F"))
                .lineSpacing(4)
            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 9)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "F8F8F8"))
    }
}

// MARK: - Toggle Row

private struct SettingsToggleRow: View {
    let title: String
    let subtitle: String
    @Binding var isOn: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 8) {
                Text(title)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                Text(subtitle)
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
            }
            Spacer()
            CustomToggle(isOn: $isOn)
        }
        .padding(.horizontal, 20)
        .frame(height: 68)
    }
}

// MARK: - Custom Toggle

private struct CustomToggle: View {
    @Binding var isOn: Bool

    var body: some View {
        ZStack(alignment: isOn ? .trailing : .leading) {
            RoundedRectangle(cornerRadius: 600)
                .fill(
                    isOn
                        ? LinearGradient(
                            colors: [Color(hex: "6092FF"), Color(hex: "2563EB"), Color(hex: "1551D3")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                        : LinearGradient(
                            colors: [Color(hex: "EDEDED"), Color(hex: "EDEDED")],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                )
                .frame(width: 42, height: 26)

            Circle()
                .fill(Color.white)
                .frame(width: 22, height: 22)
                .shadow(color: .black.opacity(0.16), radius: 2, x: 1, y: 0)
                .padding(2)
        }
        .frame(width: 42, height: 26)
        .onTapGesture { isOn.toggle() }
    }
}

// MARK: - Nav Row

private struct SettingsNavRow: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack {
                Text(title)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
                Text(">")
                    .font(AppFont.regular(16))
                    .foregroundColor(Color(hex: "121212"))
            }
            .padding(.horizontal, 20)
            .frame(height: 68)
        }
    }
}

#Preview {
    SettingsView(onBackTapped: {}, viewModel: MyPageViewModel())
}
