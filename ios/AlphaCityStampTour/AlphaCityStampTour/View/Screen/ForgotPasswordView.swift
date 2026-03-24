//
//  ForgotPasswordView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct ForgotPasswordView: View {
    @StateObject private var viewModel = ForgotPasswordViewModel()
    @State private var email = ""
    @State private var phone = ""
    @State private var code = ""
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

                // 단계 표시
                let stepText: String = {
                    switch viewModel.step {
                    case .inputInfo: return "1/3 단계"
                    case .verifyCode: return "2/3 단계"
                    case .newPassword: return "3/3 단계"
                    }
                }()
                Text(stepText)
                    .font(AppFont.medium(13))
                    .foregroundStyle(AppColor.primary)

                Spacer().frame(height: 8)

                switch viewModel.step {
                case .inputInfo:
                    inputInfoStep

                case .verifyCode:
                    verifyCodeStep

                case .newPassword:
                    newPasswordStep
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .background(Color.white)

            if viewModel.isLoading {
                Color.black.opacity(0.3).ignoresSafeArea()
                ProgressView().tint(.white).scaleEffect(1.5)
            }
        }
        .navigationBarHidden(true)
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

    // MARK: - Step 1: 이메일 + 전화번호

    private var inputInfoStep: some View {
        VStack(spacing: 0) {
            Text("가입 시 사용한 이메일과\n전화번호를 입력해주세요.")
                .font(AppFont.regular(14))
                .foregroundStyle(Color(hex: "8F8F8F"))
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 40)

            fieldLabel("이메일")
            StyledField(text: $email, placeholder: "example@email.com", keyboardType: .emailAddress)

            Spacer().frame(height: 20)

            fieldLabel("전화번호")
            StyledField(text: $phone, placeholder: "010-0000-0000", keyboardType: .phonePad)

            Spacer().frame(height: 40)

            Button(action: { viewModel.sendCode(email: email, phone: phone) }) {
                Group {
                    if viewModel.isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("인증번호 발송")
                            .font(AppFont.semibold(16))
                    }
                }
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background((!email.isEmpty && !phone.isEmpty) ? AppColor.primary : Color(hex: "D0D5DD"))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .disabled(email.isEmpty || phone.isEmpty || viewModel.isLoading)
        }
    }

    // MARK: - Step 2: 인증코드

    private var verifyCodeStep: some View {
        VStack(spacing: 0) {
            Text("전화번호로 발송된\n6자리 인증코드를 입력해주세요.")
                .font(AppFont.regular(14))
                .foregroundStyle(Color(hex: "8F8F8F"))
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 40)

            fieldLabel("인증코드")
            StyledField(text: $code, placeholder: "6자리 숫자 입력", keyboardType: .numberPad)

            Spacer().frame(height: 12)

            HStack {
                Spacer()
                Button("인증코드를 받지 못하셨나요?") {
                    viewModel.sendCode(email: email, phone: phone)
                }
                .font(AppFont.medium(13))
                .foregroundStyle(AppColor.primary)
            }

            Spacer().frame(height: 40)

            Button(action: { viewModel.verifyCode(code) }) {
                Text("확인")
                    .font(AppFont.semibold(16))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .background(code.count == 6 ? AppColor.primary : Color(hex: "D0D5DD"))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .disabled(code.count != 6)
        }
    }

    // MARK: - Step 3: 새 비밀번호

    private var newPasswordStep: some View {
        VStack(spacing: 0) {
            Text("새로운 비밀번호를 설정해주세요.")
                .font(AppFont.regular(14))
                .foregroundStyle(Color(hex: "8F8F8F"))
                .multilineTextAlignment(.center)

            Spacer().frame(height: 40)

            fieldLabel("새 비밀번호")
            StyledField(text: $newPassword, placeholder: "8자 이상 입력해주세요", isSecure: true)

            Spacer().frame(height: 20)

            fieldLabel("새 비밀번호 확인")
            StyledField(text: $confirmPassword, placeholder: "비밀번호를 다시 입력해주세요", isSecure: true)

            Spacer().frame(height: 40)

            let valid = newPassword.count >= 8 && newPassword == confirmPassword
            Button(action: {
                viewModel.resetPassword(code: code, newPassword: newPassword, confirmPassword: confirmPassword)
            }) {
                Group {
                    if viewModel.isLoading {
                        ProgressView().tint(.white)
                    } else {
                        Text("비밀번호 변경")
                            .font(AppFont.semibold(16))
                    }
                }
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(valid ? AppColor.primary : Color(hex: "D0D5DD"))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .disabled(!valid || viewModel.isLoading)
        }
    }

    // MARK: - Helpers

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

// MARK: - Styled TextField

private struct StyledField: View {
    @Binding var text: String
    let placeholder: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        Group {
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
        .frame(height: 54)
        .background(Color(hex: "F5F5F5"))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

#Preview {
    ForgotPasswordView()
}
