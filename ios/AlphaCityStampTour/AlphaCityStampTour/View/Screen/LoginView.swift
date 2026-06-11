//
//  LoginView.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI

struct LoginView: View {
    @ObservedObject var viewModel: LoginViewModel
    @State private var email = ""
    @State private var password = ""

    var onLoginSuccess: () -> Void = {}
    var onGuestTapped: () -> Void = {}
    var onRegisterTapped: () -> Void = {}
    var onForgotPasswordTapped: () -> Void = {}

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                Spacer().frame(height: 70)

                // 타이틀
                Text("로그인")
                    .font(AppFont.bold(28))
                    .foregroundStyle(Color(hex: "121212"))

                Spacer().frame(height: 12)

                // 서브타이틀
                Text("축제와 함께 스탬프 여행을 시작하세요!")
                    .font(AppFont.regular(14))
                    .foregroundStyle(Color(hex: "121212"))

                Spacer().frame(height: 50)

                // 이메일 입력
                LoginTextField(
                    text: $email,
                    placeholder: "example@email.com",
                    iconName: "IconEmail",
                    keyboardType: .emailAddress
                )

                Spacer().frame(height: 12)

                // 비밀번호 입력
                LoginTextField(
                    text: $password,
                    placeholder: "********",
                    iconName: "IconLock",
                    isSecure: true
                )

                // 비밀번호 찾기
                HStack {
                    Spacer()
                    Button(action: onForgotPasswordTapped) {
                        Text("비밀번호를 잊어버리셨나요?")
                            .font(AppFont.medium(14))
                            .foregroundStyle(Color(hex: "8F8F8F"))
                    }
                }
                .padding(.top, 10)

                Spacer().frame(height: 48)

                // 로그인 버튼
                Button(action: { viewModel.login(email: email, password: password) }) {
                    Text("로그인")
                        .font(AppFont.semibold(16))
                        .foregroundStyle(Color(hex: "F8F8F8"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(AppColor.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                // 3회 이상 실패 시 비밀번호 찾기 안내
                if viewModel.showForgotPasswordHint {
                    Spacer().frame(height: 12)

                    HStack {
                        Text("로그인에 3회 이상 실패했습니다.")
                            .font(AppFont.medium(13))
                            .foregroundStyle(Color(hex: "795548"))

                        Spacer()

                        Button(action: onForgotPasswordTapped) {
                            Text("비밀번호 찾기")
                                .font(AppFont.semibold(13))
                                .foregroundStyle(AppColor.primary)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color(hex: "FFF8E1"))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(hex: "FFB74D"), lineWidth: 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Spacer().frame(height: 12)

                // 비회원으로 계속하기 버튼
                Button(action: onGuestTapped) {
                    Text("비회원으로 계속하기")
                        .font(AppFont.semibold(16))
                        .foregroundStyle(AppColor.primary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color(hex: "EDF7FF"))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Spacer().frame(height: 48)

                // "또는" 구분선
                HStack(spacing: 16) {
                    Rectangle()
                        .fill(Color(hex: "DBDBDB"))
                        .frame(height: 1)

                    Text("또는")
                        .font(AppFont.medium(14))
                        .foregroundStyle(Color(hex: "8F8F8F"))

                    Rectangle()
                        .fill(Color(hex: "DBDBDB"))
                        .frame(height: 1)
                }

                Spacer().frame(height: 28)

                // 소셜 로그인 버튼
                HStack(spacing: 12) {
                    // Apple
                    Button(action: { viewModel.loginWithApple() }) {
                        ZStack {
                            Circle()
                                .fill(Color.black)
                                .frame(width: 48, height: 48)
                            Image(systemName: "apple.logo")
                                .font(.system(size: 22))
                                .foregroundStyle(.white)
                                .offset(y: -1)
                        }
                    }

                    // 카카오
                    Button(action: { viewModel.loginWithKakao() }) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "FFE200"))
                                .frame(width: 48, height: 48)
                            Image("IconKakao")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 28, height: 28)
                        }
                    }

                    // 네이버
                    Button(action: { viewModel.loginWithNaver() }) {
                        ZStack {
                            Circle()
                                .fill(Color(hex: "03C75A"))
                                .frame(width: 48, height: 48)
                            Image("IconNaver")
                                .resizable()
                                .scaledToFit()
                                .frame(width: 23, height: 22)
                        }
                    }
                }

                Spacer().frame(height: 28)

                // 회원가입 링크
                HStack(spacing: 4) {
                    Text("아직 계정이 없으신가요?")
                        .font(AppFont.medium(14))
                        .foregroundStyle(Color(hex: "8F8F8F"))

                    Button(action: onRegisterTapped) {
                        Text("회원가입")
                            .font(AppFont.semibold(14))
                            .foregroundStyle(AppColor.primary)
                    }
                }

                Spacer()

                // 하단 저작권
                Text("© 2026 Alpha Stamp. All rights reserved.")
                    .font(AppFont.regular(10))
                    .foregroundStyle(Color(hex: "8F8F8F"))
                    .padding(.bottom, 40)
            }
            .padding(.horizontal, 20)
            .background(Color.white)

            // 로딩 오버레이
            if viewModel.isLoading {
                Color.black.opacity(0.3)
                    .ignoresSafeArea()
                ProgressView()
                    .tint(.white)
                    .scaleEffect(1.5)
            }
        }
        .onChange(of: viewModel.isLoggedIn) { _, isLoggedIn in
            if isLoggedIn { onLoginSuccess() }
        }
        .alert(viewModel.error ?? "", isPresented: Binding(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인") { viewModel.clearError() }
        }
    }
}

// MARK: - Login TextField

private struct LoginTextField: View {
    @Binding var text: String
    let placeholder: String
    let iconName: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        HStack(spacing: 12) {
            Image(iconName)
                .resizable()
                .scaledToFit()
                .frame(width: 17, height: 17)
                .opacity(0.4)

            if isSecure {
                SecureField(placeholder, text: $text)
                    .font(AppFont.medium(14))
                    .keyboardType(keyboardType)
            } else {
                TextField(placeholder, text: $text)
                    .font(AppFont.medium(14))
                    .keyboardType(keyboardType)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
            }
        }
        .padding(.horizontal, 16)
        .frame(height: 48)
        .background(Color(hex: "F5F5F5"))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(hex: "F0F0F0"), lineWidth: 1)
        )
    }
}

#Preview {
    LoginView(viewModel: LoginViewModel())
}
