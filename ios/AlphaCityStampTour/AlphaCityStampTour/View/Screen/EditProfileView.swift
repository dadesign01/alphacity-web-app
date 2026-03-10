//
//  EditProfileView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct EditProfileView: View {
    var onBackTapped: () -> Void
    var onLogout: () -> Void
    var initialNickname: String = ""
    var initialEmail: String = ""

    @State private var nickname = ""
    @State private var email = ""
    @State private var password = ""
    @State private var passwordConfirm = ""
    private let name = ""
    @State private var phone = ""
    @State private var address = ""
    @State private var addressDetail = ""

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
                Text("개인정보 수정")
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
                    Spacer().frame(height: 32)

                    // === Profile Image ===
                    ZStack(alignment: .bottomTrailing) {
                        Image("IconProfile")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 104, height: 104)
                            .clipShape(Circle())

                        ZStack {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 30, height: 30)
                                .overlay(
                                    Circle().stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                                )
                            Image(systemName: "camera.fill")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 14, height: 14)
                                .foregroundColor(Color(hex: "8F8F8F"))
                        }
                    }

                    Spacer().frame(height: 32)

                    // === Form Fields ===
                    VStack(spacing: 20) {
                        // 닉네임
                        ProfileFormField(
                            label: "닉네임",
                            required: true,
                            text: $nickname,
                            placeholder: "닉네임을 입력해주세요."
                        )

                        // 이메일
                        ProfileFormField(
                            label: "이메일",
                            required: true,
                            text: $email,
                            placeholder: "이메일을 입력해주세요.",
                            helperText: "이메일은 로그인 시 사용됩니다."
                        )

                        // 비밀번호
                        ProfileFormField(
                            label: "비밀번호",
                            required: true,
                            text: $password,
                            placeholder: "8자 이상 입력해주세요.",
                            isSecure: true
                        )

                        // 비밀번호 확인
                        ProfileFormField(
                            label: "비밀번호 확인",
                            required: true,
                            text: $passwordConfirm,
                            placeholder: "비밀번호를 다시 입력해주세요.",
                            isSecure: true
                        )

                        // 이름 (disabled)
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "이름", required: true)
                            HStack {
                                Text(name)
                                    .font(AppFont.regular(14))
                                    .foregroundColor(Color(hex: "BFBFBF"))
                                Spacer()
                                Image("IconLock")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 18)
                            }
                            .padding(.horizontal, 14)
                            .frame(height: 48)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                            )
                        }

                        // 휴대폰
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "휴대폰", required: true)
                            HStack(spacing: 8) {
                                ProfileTextFieldView(
                                    text: $phone,
                                    placeholder: "010-0000-0000"
                                )
                                Button {
                                    // TODO: 본인인증
                                } label: {
                                    Text("본인인증")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(AppColor.primary)
                                        .padding(.horizontal, 16)
                                        .frame(height: 48)
                                        .background(Color(hex: "EDF7FF"))
                                        .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                            }
                        }

                        // 주소
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "주소", required: false)
                            ProfileTextFieldView(
                                text: $address,
                                placeholder: "주소를 입력해주세요."
                            )
                            ProfileTextFieldView(
                                text: $addressDetail,
                                placeholder: "상세주소를 입력해주세요."
                            )
                        }
                    }
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 24)

                    // === Privacy Notice ===
                    HStack {
                        Text("개인정보는 서비스 제공 목적으로만 사용되며,\n관련 법령에 따라 안전하게 관리됩니다.")
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .lineSpacing(4)
                        Spacer()
                    }
                    .padding(16)
                    .background(
                        RoundedRectangle(cornerRadius: 11)
                            .fill(Color(hex: "F8F8F8"))
                    )
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 24)

                    // === Save Button ===
                    Button {
                        // TODO: save
                    } label: {
                        Text("저장하기")
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

                    Spacer().frame(height: 20)

                    // === Logout | Withdraw ===
                    HStack(spacing: 0) {
                        Button {
                            onLogout()
                        } label: {
                            Text("로그아웃")
                                .font(AppFont.regular(12))
                                .foregroundColor(Color(hex: "8F8F8F"))
                        }
                        Text("  |  ")
                            .font(AppFont.regular(12))
                            .foregroundColor(Color(hex: "D9D9D9"))
                        Button {
                            // TODO: withdraw
                        } label: {
                            Text("회원탈퇴")
                                .font(AppFont.regular(12))
                                .foregroundColor(Color(hex: "8F8F8F"))
                        }
                    }

                    Spacer().frame(height: 32)

                    // === Footer ===
                    Text("© 2026 Alpha Stamp. All rights reserved.")
                        .font(AppFont.regular(10))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 40)
                        .background(Color(hex: "F9F9F9"))
                }
            }
        }
        .background(Color.white)
        .onAppear {
            nickname = initialNickname
            email = initialEmail
        }
    }
}

// MARK: - Subviews

private struct ProfileFieldLabel: View {
    let label: String
    let required: Bool

    var body: some View {
        HStack(spacing: 0) {
            Text(label)
                .font(AppFont.medium(14))
                .foregroundColor(Color(hex: "121212"))
            if required {
                Text(" *")
                    .font(AppFont.medium(14))
                    .foregroundColor(AppColor.primary)
            }
        }
    }
}

private struct ProfileFormField: View {
    let label: String
    let required: Bool
    @Binding var text: String
    let placeholder: String
    var helperText: String? = nil
    var isSecure: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            ProfileFieldLabel(label: label, required: required)
            if isSecure {
                SecureField(placeholder, text: $text)
                    .font(AppFont.regular(14))
                    .padding(.horizontal, 14)
                    .frame(height: 48)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                    )
            } else {
                ProfileTextFieldView(text: $text, placeholder: placeholder)
            }
            if let helperText {
                Text(helperText)
                    .font(AppFont.regular(11))
                    .foregroundColor(Color(hex: "8F8F8F"))
            }
        }
    }
}

private struct ProfileTextFieldView: View {
    @Binding var text: String
    let placeholder: String

    var body: some View {
        TextField(placeholder, text: $text)
            .font(AppFont.regular(14))
            .padding(.horizontal, 14)
            .frame(height: 48)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
            )
    }
}

#Preview {
    EditProfileView(onBackTapped: {}, onLogout: {}, initialNickname: "테스트유저", initialEmail: "test@example.com")
}
