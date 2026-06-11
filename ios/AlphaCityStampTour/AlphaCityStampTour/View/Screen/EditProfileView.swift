//
//  EditProfileView.swift
//  AlphaCityStampTour
//

import SwiftUI
import PhotosUI

struct EditProfileView: View {
    var onBackTapped: () -> Void
    var onLogout: () -> Void
    var initialNickname: String = ""
    var initialEmail: String = ""
    var initialName: String? = nil
    var initialPhone: String? = nil
    var initialAddress: String? = nil
    var initialAddressDetail: String? = nil
    var initialProvider: String? = nil
    var initialBirthDate: String? = nil
    var initialGender: String? = nil
    @ObservedObject var viewModel: MyPageViewModel

    @State private var nickname = ""
    @State private var email = ""
    @State private var currentPassword = ""
    @State private var newPassword = ""
    @State private var name = ""
    @State private var phone = ""
    @State private var verificationCode = ""
    @State private var address = ""
    @State private var addressDetail = ""
    @State private var birthDate = ""
    @State private var birthDateValue = Date()
    @State private var showDatePicker = false
    @State private var gender = ""
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var selectedImage: UIImage? = nil
    @State private var showDeleteAlert = false
    @State private var showToast: String?

    private var isSocialLogin: Bool {
        initialProvider != nil
    }

    private var isNameLocked: Bool {
        if let initialName = initialName, !initialName.isEmpty {
            return true
        }
        return false
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
                    PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                        ZStack(alignment: .bottomTrailing) {
                            if let selectedImage {
                                Image(uiImage: selectedImage)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 104, height: 104)
                                    .clipShape(Circle())
                            } else {
                                Image("IconProfile")
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 104, height: 104)
                                    .clipShape(Circle())
                            }

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
                    }
                    .onChange(of: selectedPhotoItem) { newItem in
                        Task {
                            if let data = try? await newItem?.loadTransferable(type: Data.self),
                               let uiImage = UIImage(data: data) {
                                selectedImage = uiImage
                            }
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

                        // 비밀번호 (소셜 로그인 사용자에게는 숨김)
                        if !isSocialLogin {
                            ProfileFormField(
                                label: "현재 비밀번호",
                                required: false,
                                text: $currentPassword,
                                placeholder: "현재 비밀번호를 입력해주세요.",
                                isSecure: true
                            )

                            ProfileFormField(
                                label: "새 비밀번호",
                                required: false,
                                text: $newPassword,
                                placeholder: "새 비밀번호를 입력해주세요. (8자 이상)",
                                isSecure: true
                            )
                        }

                        // 이름
                        if isNameLocked {
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
                                .background(
                                    RoundedRectangle(cornerRadius: 8)
                                        .fill(Color(hex: "F5F5F5"))
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                                )
                            }
                        } else {
                            ProfileFormField(
                                label: "이름",
                                required: true,
                                text: $name,
                                placeholder: "이름을 입력해주세요."
                            )
                        }

                        // 휴대폰
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "휴대폰", required: false)
                            HStack(spacing: 8) {
                                ProfileTextFieldView(
                                    text: $phone,
                                    placeholder: "010-0000-0000"
                                )
                                .disabled(viewModel.isPhoneVerified)
                                .onChange(of: phone) { _ in
                                    if viewModel.isPhoneVerified || viewModel.isCodeSent {
                                        viewModel.resetVerificationState()
                                        verificationCode = ""
                                    }
                                }
                                Button {
                                    viewModel.sendCode(phone: phone)
                                } label: {
                                    Text(viewModel.isCodeSent ? "재전송" : "본인인증")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(viewModel.isPhoneVerified ? Color(hex: "8F8F8F") : AppColor.primary)
                                        .padding(.horizontal, 16)
                                        .frame(height: 48)
                                        .background(viewModel.isPhoneVerified ? Color(hex: "F5F5F5") : Color(hex: "EDF7FF"))
                                        .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                                .disabled(viewModel.isPhoneVerified || phone.isEmpty)
                            }

                            // 인증번호 입력 (코드 발송 후, 인증 완료 전)
                            if viewModel.isCodeSent && !viewModel.isPhoneVerified {
                                Spacer().frame(height: 6)
                                HStack(spacing: 8) {
                                    ProfileTextFieldView(
                                        text: $verificationCode,
                                        placeholder: "인증번호 6자리"
                                    )
                                    .keyboardType(.numberPad)
                                    Button {
                                        viewModel.verifyCode(phone: phone, code: verificationCode)
                                    } label: {
                                        Text("확인")
                                            .font(AppFont.semibold(14))
                                            .foregroundColor(.white)
                                            .padding(.horizontal, 16)
                                            .frame(height: 48)
                                            .background(
                                                verificationCode.count == 6
                                                    ? AppColor.primary
                                                    : AppColor.primary.opacity(0.5)
                                            )
                                            .clipShape(RoundedRectangle(cornerRadius: 8))
                                    }
                                    .disabled(verificationCode.count != 6)
                                }
                            }

                            // 인증 완료 표시
                            if viewModel.isPhoneVerified {
                                HStack(spacing: 4) {
                                    Image(systemName: "checkmark.circle.fill")
                                        .font(.system(size: 14))
                                        .foregroundColor(Color(hex: "22C55E"))
                                    Text("인증완료")
                                        .font(AppFont.medium(12))
                                        .foregroundColor(Color(hex: "22C55E"))
                                    Spacer()
                                }
                                .padding(.top, 2)
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

                        // 생년월일
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "생년월일", required: false)
                            Button {
                                showDatePicker = true
                            } label: {
                                HStack {
                                    Text(birthDate.isEmpty ? "생년월일을 선택해주세요." : birthDate)
                                        .font(AppFont.regular(14))
                                        .foregroundColor(birthDate.isEmpty ? Color(hex: "BFBFBF") : Color(hex: "121212"))
                                    Spacer()
                                }
                                .padding(.horizontal, 14)
                                .frame(height: 48)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                                )
                            }
                        }
                        .sheet(isPresented: $showDatePicker) {
                            VStack(spacing: 16) {
                                Text("생년월일 선택")
                                    .font(AppFont.semibold(18))
                                    .padding(.top, 20)
                                DatePicker(
                                    "",
                                    selection: $birthDateValue,
                                    displayedComponents: .date
                                )
                                .datePickerStyle(.wheel)
                                .labelsHidden()
                                .environment(\.locale, Locale(identifier: "ko_KR"))

                                Button {
                                    let formatter = DateFormatter()
                                    formatter.dateFormat = "yyyy-MM-dd"
                                    birthDate = formatter.string(from: birthDateValue)
                                    showDatePicker = false
                                } label: {
                                    Text("확인")
                                        .font(AppFont.semibold(16))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .frame(height: 48)
                                        .background(AppColor.primary)
                                        .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                                .padding(.horizontal, 20)
                                .padding(.bottom, 20)
                            }
                            .presentationDetents([.height(340)])
                        }

                        // 성별
                        VStack(alignment: .leading, spacing: 6) {
                            ProfileFieldLabel(label: "성별", required: false)
                            HStack(spacing: 16) {
                                ForEach([("male", "남성"), ("female", "여성"), ("other", "기타")], id: \.0) { value, label in
                                    Button {
                                        gender = value
                                    } label: {
                                        HStack(spacing: 4) {
                                            Image(systemName: gender == value ? "largecircle.fill.circle" : "circle")
                                                .font(.system(size: 20))
                                                .foregroundColor(gender == value ? AppColor.primary : Color(hex: "BFBFBF"))
                                            Text(label)
                                                .font(AppFont.medium(14))
                                                .foregroundColor(Color(hex: "121212"))
                                        }
                                    }
                                }
                            }
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
                        guard !nickname.trimmingCharacters(in: .whitespaces).isEmpty else {
                            showToast = "닉네임을 입력해주세요."
                            return
                        }
                        if !isSocialLogin && !newPassword.isEmpty && currentPassword.isEmpty {
                            showToast = "현재 비밀번호를 입력해주세요."
                            return
                        }
                        Task {
                            await viewModel.updateProfile(
                                nickname: nickname,
                                currentPassword: (!isSocialLogin && !currentPassword.isEmpty) ? currentPassword : nil,
                                newPassword: (!isSocialLogin && !newPassword.isEmpty) ? newPassword : nil,
                                phone: viewModel.isPhoneVerified ? phone : nil,
                                name: name.trimmingCharacters(in: .whitespaces).isEmpty ? nil : name,
                                address: address.trimmingCharacters(in: .whitespaces).isEmpty ? nil : address,
                                addressDetail: addressDetail.trimmingCharacters(in: .whitespaces).isEmpty ? nil : addressDetail,
                                birthDate: birthDate.isEmpty ? nil : birthDate,
                                gender: gender.isEmpty ? nil : gender
                            )
                        }
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
                            showDeleteAlert = true
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
            name = initialName ?? ""
            phone = initialPhone ?? ""
            address = initialAddress ?? ""
            addressDetail = initialAddressDetail ?? ""
            birthDate = initialBirthDate ?? ""
            gender = initialGender ?? ""
            // 생년월일 문자열로 DatePicker 초기값 설정
            if let bd = initialBirthDate, !bd.isEmpty {
                let formatter = DateFormatter()
                formatter.dateFormat = "yyyy-MM-dd"
                if let date = formatter.date(from: bd) {
                    birthDateValue = date
                }
            }
            // 전화번호가 이미 저장되어 있으면 인증 완료 상태로 설정
            if let p = initialPhone, !p.isEmpty {
                viewModel.setPhoneVerifiedFromProfile()
            }
        }
        .alert("회원탈퇴", isPresented: $showDeleteAlert) {
            Button("취소", role: .cancel) {}
            Button("탈퇴", role: .destructive) {
                Task {
                    await viewModel.deleteAccount()
                }
            }
        } message: {
            Text("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
        }
        .onChange(of: viewModel.saveSuccess) { success in
            if success {
                showToast = "저장되었습니다."
                viewModel.clearSaveState()
                onBackTapped()
            }
        }
        .onChange(of: viewModel.saveError) { error in
            if let error = error {
                showToast = error
                viewModel.clearSaveState()
            }
        }
        .onChange(of: viewModel.deleteSuccess) { success in
            if success {
                viewModel.clearDeleteState()
                onLogout()
            }
        }
        .onChange(of: viewModel.deleteError) { error in
            if let error = error {
                showToast = error
                viewModel.clearDeleteState()
            }
        }
        .onChange(of: viewModel.verificationError) { error in
            if let error = error {
                showToast = error
            }
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
    EditProfileView(onBackTapped: {}, onLogout: {}, initialNickname: "테스트유저", initialEmail: "test@example.com", initialName: nil, initialBirthDate: nil, initialGender: nil, viewModel: MyPageViewModel())
}
