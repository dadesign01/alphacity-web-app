//
//  ForgotPasswordView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct ForgotPasswordView: View {
    @StateObject private var viewModel = ForgotPasswordViewModel()
    @State private var email = ""
    @State private var newPassword = ""
    @State private var confirmPassword = ""

    var onBackTapped: () -> Void = {}

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                // 헤더
                HStack {
                    Button(action: onBackTapped) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundStyle(Color(hex: "121212"))
                    }

                    Spacer()

                    Text("비밀번호 찾기")
                        .font(AppFont.semibold(18))
                        .foregroundStyle(Color(hex: "121212"))

                    Spacer()

                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .medium))
                        .opacity(0)
                }
                .padding(.top, 16)

                Spacer().frame(height: 48)

                // 안내 텍스트
                Text("가입 시 사용한 이메일을 입력하고\n새로운 비밀번호를 설정하세요.")
                    .font(AppFont.regular(14))
                    .foregroundStyle(Color(hex: "8F8F8F"))
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)

                Spacer().frame(height: 40)

                // 이메일
                fieldLabel("이메일")
                ForgotPasswordTextField(
                    text: $email,
                    placeholder: "example@email.com",
                    iconName: "envelope.fill",
                    keyboardType: .emailAddress
                )

                Spacer().frame(height: 20)

                // 새 비밀번호
                fieldLabel("새 비밀번호")
                ForgotPasswordTextField(
                    text: $newPassword,
                    placeholder: "8자 이상 입력해주세요.",
                    iconName: "lock.fill",
                    isSecure: true
                )

                Spacer().frame(height: 20)

                // 새 비밀번호 확인
                fieldLabel("새 비밀번호 확인")
                ForgotPasswordTextField(
                    text: $confirmPassword,
                    placeholder: "비밀번호를 다시 입력해주세요.",
                    iconName: "lock.fill",
                    isSecure: true
                )

                Spacer().frame(height: 40)

                // 비밀번호 변경 버튼
                Button(action: {
                    viewModel.resetPassword(
                        email: email,
                        newPassword: newPassword,
                        confirmPassword: confirmPassword
                    )
                }) {
                    Text("비밀번호 변경")
                        .font(AppFont.semibold(16))
                        .foregroundStyle(Color(hex: "F8F8F8"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(isFormValid ? AppColor.primary : AppColor.primary.opacity(0.5))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
                .disabled(!isFormValid)

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
        .navigationBarHidden(true)
        .onChange(of: viewModel.isSuccess) { _, isSuccess in
            if isSuccess { onBackTapped() }
        }
        .alert(viewModel.error ?? "", isPresented: Binding(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인") { viewModel.clearError() }
        }
        .alert("비밀번호가 변경되었습니다", isPresented: $viewModel.isSuccess) {
            Button("로그인하기") { onBackTapped() }
        }
    }

    private var isFormValid: Bool {
        !email.isEmpty && newPassword.count >= 8 && newPassword == confirmPassword
    }

    private func fieldLabel(_ text: String) -> some View {
        HStack {
            Text(text)
                .font(AppFont.medium(14))
                .foregroundStyle(Color(hex: "121212"))
            Spacer()
        }
        .padding(.bottom, 8)
    }
}

// MARK: - TextField

private struct ForgotPasswordTextField: View {
    @Binding var text: String
    let placeholder: String
    let iconName: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: iconName)
                .font(.system(size: 14))
                .foregroundStyle(Color(hex: "C7C7C7"))
                .frame(width: 17, height: 17)

            if isSecure {
                SecureField(placeholder, text: $text)
                    .font(AppFont.medium(14))
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
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
        )
    }
}

#Preview {
    ForgotPasswordView()
}
