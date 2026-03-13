//
//  RegisterView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct RegisterView: View {
    @StateObject private var viewModel = RegisterViewModel()
    @State private var name = ""
    @State private var email = ""
    @State private var phone = ""
    @State private var verificationCode = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var agreedToTerms = false

    var onRegisterSuccess: () -> Void = {}
    var onBackTapped: () -> Void = {}

    var body: some View {
        ZStack {
            VStack(spacing: 0) {
                // 헤더
                HStack(spacing: 24) {
                    Button(action: onBackTapped) {
                        Image("IconBackArrow")
                            .renderingMode(.original)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 13, height: 26)
                    }

                    Text("회원가입")
                        .font(AppFont.semibold(18))
                        .foregroundStyle(Color(hex: "121212"))

                    Spacer()
                }
                .padding(.horizontal, 20)
                .frame(height: 50)

                Divider()
                    .background(Color(hex: "E2E2E2"))

            ScrollView {
                VStack(spacing: 0) {
                    Spacer().frame(height: 32)

                    // 이름
                    fieldLabel("이름", required: true)
                    RegisterTextField(
                        text: $name,
                        placeholder: "홍길동",
                        iconName: "person.fill"
                    )

                    Spacer().frame(height: 20)

                    // 이메일
                    fieldLabel("이메일", required: true)
                    RegisterTextField(
                        text: $email,
                        placeholder: "example@email.com",
                        iconName: "envelope.fill",
                        keyboardType: .emailAddress
                    )

                    Spacer().frame(height: 20)

                    // 휴대폰
                    fieldLabel("휴대폰", required: true)
                    HStack(spacing: 8) {
                        RegisterTextField(
                            text: $phone,
                            placeholder: "010-1234-5678",
                            iconName: "phone.fill",
                            keyboardType: .phonePad,
                            isDisabled: viewModel.isPhoneVerified
                        )
                        .frame(maxWidth: .infinity)

                        Button(action: { viewModel.sendCode(phone: phone) }) {
                            Text(viewModel.isCodeSent ? "재전송" : "본인인증")
                                .font(AppFont.medium(14))
                                .foregroundStyle(viewModel.isPhoneVerified ? Color(hex: "8F8F8F") : AppColor.primary)
                                .frame(width: 81, height: 48)
                                .background(viewModel.isPhoneVerified ? Color(hex: "F5F5F5") : Color(hex: "EDF7FF"))
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                        .disabled(viewModel.isPhoneVerified || phone.isEmpty)
                    }

                    // 인증번호 입력 (코드 발송 후 표시)
                    if viewModel.isCodeSent && !viewModel.isPhoneVerified {
                        Spacer().frame(height: 12)
                        HStack(spacing: 8) {
                            RegisterTextField(
                                text: $verificationCode,
                                placeholder: "인증번호 6자리",
                                iconName: "number",
                                keyboardType: .numberPad
                            )
                            .frame(maxWidth: .infinity)

                            Button(action: { viewModel.verifyCode(phone: phone, code: verificationCode) }) {
                                Text("확인")
                                    .font(AppFont.medium(14))
                                    .foregroundStyle(.white)
                                    .frame(width: 81, height: 48)
                                    .background(verificationCode.count == 6 ? AppColor.primary : AppColor.primary.opacity(0.5))
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                            }
                            .disabled(verificationCode.count != 6)
                        }
                    }

                    // 인증 완료 표시
                    if viewModel.isPhoneVerified {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 14))
                                .foregroundStyle(Color(hex: "22C55E"))
                            Text("인증 완료")
                                .font(AppFont.medium(12))
                                .foregroundStyle(Color(hex: "22C55E"))
                            Spacer()
                        }
                        .padding(.top, 8)
                    }

                    Spacer().frame(height: 20)

                    // 비밀번호
                    fieldLabel("비밀번호", required: true)
                    RegisterTextField(
                        text: $password,
                        placeholder: "8자 이상 입력해주세요.",
                        iconName: "lock.fill",
                        isSecure: true
                    )

                    Spacer().frame(height: 20)

                    // 비밀번호 확인
                    fieldLabel("비밀번호 확인", required: true)
                    RegisterTextField(
                        text: $confirmPassword,
                        placeholder: "비밀번호를 다시 입력해주세요.",
                        iconName: "lock.fill",
                        isSecure: true
                    )

                    Spacer().frame(height: 24)

                    // 이용약관 동의
                    HStack(spacing: 8) {
                        Button(action: { agreedToTerms.toggle() }) {
                            RoundedRectangle(cornerRadius: 3)
                                .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                                .frame(width: 18, height: 18)
                                .overlay(
                                    agreedToTerms ?
                                    Image(systemName: "checkmark")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundStyle(AppColor.primary)
                                    : nil
                                )
                        }

                        Text("이용약관 및 개인정보처리방침에 동의합니다.")
                            .font(AppFont.medium(14))
                            .foregroundStyle(Color(hex: "121212"))

                        Spacer()

                        Button(action: {}) {
                            Text("자세히 보기 >")
                                .font(AppFont.medium(11))
                                .foregroundStyle(Color(hex: "8D8D8D"))
                        }
                    }

                    Spacer().frame(height: 24)

                    // 가입하기 버튼
                    Button(action: {
                        viewModel.register(
                            name: name,
                            email: email,
                            phone: phone,
                            password: password,
                            confirmPassword: confirmPassword
                        )
                    }) {
                        Text("가입하기")
                            .font(AppFont.semibold(16))
                            .foregroundStyle(Color(hex: "F8F8F8"))
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(isFormValid ? AppColor.primary : AppColor.primary.opacity(0.5))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    .disabled(!isFormValid)

                    Spacer().frame(height: 24)

                    // 로그인 링크
                    HStack(spacing: 4) {
                        Text("이미 계정이 있으신가요?")
                            .font(AppFont.medium(14))
                            .foregroundStyle(Color(hex: "8F8F8F"))

                        Button(action: onBackTapped) {
                            Text("로그인")
                                .font(AppFont.semibold(14))
                                .foregroundStyle(AppColor.primary)
                        }
                    }

                    Spacer().frame(height: 40)

                    // 하단 저작권
                    Text("© 2026 Alpha Stamp. All rights reserved.")
                        .font(AppFont.regular(10))
                        .foregroundStyle(Color(hex: "8F8F8F"))
                        .padding(.bottom, 40)
                }
                .padding(.horizontal, 20)
            } // end ScrollView
            } // end outer VStack
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
        .onChange(of: viewModel.isRegistered) { _, isRegistered in
            if isRegistered { onRegisterSuccess() }
        }
        .alert(viewModel.error ?? "", isPresented: Binding(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("확인") { viewModel.clearError() }
        }
    }

    private var isFormValid: Bool {
        !name.isEmpty && !email.isEmpty && viewModel.isPhoneVerified &&
        password.count >= 8 && password == confirmPassword && agreedToTerms
    }

    private func fieldLabel(_ text: String, required: Bool = false) -> some View {
        HStack(spacing: 2) {
            Text(text)
                .font(AppFont.medium(14))
                .foregroundStyle(Color(hex: "121212"))
            if required {
                Text("*")
                    .font(AppFont.medium(11))
                    .foregroundStyle(AppColor.primary)
            }
            Spacer()
        }
        .padding(.bottom, 8)
    }
}

// MARK: - Register TextField

private struct RegisterTextField: View {
    @Binding var text: String
    let placeholder: String
    let iconName: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var isDisabled: Bool = false

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
                    .disabled(isDisabled)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(isDisabled ? Color(hex: "F5F5F5") : Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
        )
    }
}

#Preview {
    RegisterView()
}
