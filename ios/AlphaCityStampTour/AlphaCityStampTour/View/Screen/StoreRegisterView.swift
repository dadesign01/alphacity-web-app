//
//  StoreRegisterView.swift
//  AlphaCityStampTour
//

import SwiftUI
import Combine
import PhotosUI

private enum StoreCategory: String, CaseIterable {
    case cafe, restaurant, shopping, hotel, convenience

    var label: String {
        switch self {
        case .cafe: return "카페"
        case .restaurant: return "음식점"
        case .shopping: return "쇼핑"
        case .hotel: return "호텔"
        case .convenience: return "편의시설"
        }
    }

    var iconName: String {
        switch self {
        case .cafe: return "IconStoreCafe"
        case .restaurant: return "IconStoreRestaurant"
        case .shopping: return "IconStoreShopping"
        case .hotel: return "IconStoreHotel"
        case .convenience: return "IconStoreConvenience"
        }
    }
}

private func hideKeyboard() {
    UIApplication.shared.sendAction(
        #selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil
    )
}

struct StoreRegisterView: View {
    var onBackTapped: () -> Void

    @StateObject private var viewModel = StoreRegisterViewModel()
    @State private var currentStep = 1
    @State private var selectedCategory: StoreCategory? = nil
    @State private var selectedPhotoItem: PhotosPickerItem? = nil
    @State private var selectedImage: UIImage? = nil

    // Step 2 form fields
    @State private var storeName = ""
    @State private var storeAddress = ""
    @State private var storeAddressDetail = ""
    @State private var phoneNumber = ""
    @State private var ownerName = ""
    @State private var selectedDays: Set<String> = []
    @State private var openTime = "09:00"
    @State private var closeTime = "20:00"
    @State private var storeCode = ""
    @State private var storeDescription = ""
    @State private var errorMessage: String? = nil
    @State private var showError = false

    // 시간 입력 자동 포맷터: 숫자만 받아 'HH:MM' 형태로 자동 콜론 삽입
    private func formatTimeInput(_ input: String) -> String {
        let digits = String(input.filter { $0.isNumber }.prefix(4))
        if digits.count <= 2 { return digits }
        let idx = digits.index(digits.startIndex, offsetBy: 2)
        return String(digits[..<idx]) + ":" + String(digits[idx...])
    }

    var body: some View {
        VStack(spacing: 0) {
            // === Header ===
            HStack(spacing: 24) {
                Button {
                    if currentStep > 1 { currentStep -= 1 } else { onBackTapped() }
                } label: {
                    Image("IconBackArrow")
                        .renderingMode(.original)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 26)
                }
                Text("상점등록")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .frame(height: 50)

            Divider()
                .background(Color(hex: "E2E2E2"))

            // === Progress Bar ===
            HStack(spacing: 7) {
                ForEach(1...3, id: \.self) { step in
                    RoundedRectangle(cornerRadius: 100)
                        .fill(step <= currentStep ? AppColor.primary : Color(hex: "F8F8F8"))
                        .frame(height: 9)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 6)

            // === Content ===
            switch currentStep {
            case 1:
                step1CategorySelection
            case 2:
                step2DetailForm
            case 3:
                step3Completion
            default:
                EmptyView()
            }
        }
        .background(Color.white)
        .alert("등록 실패", isPresented: $showError) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(errorMessage ?? "등록에 실패했습니다")
        }
    }

    // ===================== Step 1: Category Selection =====================

    private var step1CategorySelection: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 100)

                // 상점 일러스트 아이콘
                Image("IconStoreRegister")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                Spacer().frame(height: 16)

                // Title
                Text("상점 카테고리를 선택하세요.")
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                Spacer().frame(height: 6)

                Text("운영하시는 상점의 유형을 선택해주세요.")
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                Spacer().frame(height: 40)

                // Category items
                ForEach(StoreCategory.allCases, id: \.self) { category in
                    let isSelected = selectedCategory == category
                    Button {
                        selectedCategory = category
                    } label: {
                        HStack(spacing: 0) {
                            Spacer().frame(width: 22)
                            Image(category.iconName)
                                .renderingMode(.template)
                                .resizable()
                                .scaledToFit()
                                .frame(width: 25, height: 25)
                                .foregroundColor(isSelected ? AppColor.primary : Color(hex: "121212"))
                            Spacer().frame(width: 21)
                            Text(category.label)
                                .font(AppFont.medium(16))
                                .foregroundColor(isSelected ? AppColor.primary : Color(hex: "121212"))
                            Spacer()
                        }
                        .frame(height: 59)
                        .background(
                            RoundedRectangle(cornerRadius: 15)
                                .fill(isSelected ? Color(hex: "EDF7FF") : Color(hex: "F8F8F8"))
                        )
                        .overlay(
                            isSelected ?
                            RoundedRectangle(cornerRadius: 15)
                                .stroke(AppColor.primary, lineWidth: 2)
                            : nil
                        )
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 5)
                }

                Spacer().frame(height: 80)

                // Next button
                GradientActionButton(title: "다음 단계가기") {
                    if selectedCategory != nil {
                        currentStep = 2
                    } else {
                        errorMessage = "상점 카테고리를 선택해주세요"
                        showError = true
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
        }
    }

    // ===================== Step 2: Detail Form =====================

    private var step2DetailForm: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 40)

                // 선택한 카테고리 아이콘 미리보기
                if let category = selectedCategory {
                    ZStack {
                        RoundedRectangle(cornerRadius: 18)
                            .fill(Color(hex: "EDF7FF"))
                            .frame(width: 72, height: 72)
                        Image(category.iconName)
                            .resizable()
                            .renderingMode(.template)
                            .scaledToFit()
                            .frame(width: 36, height: 36)
                            .foregroundColor(AppColor.primary)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                    Spacer().frame(height: 20)
                }

                // Title
                Text("선택하신 \(selectedCategory?.label ?? "")의 정보를 입력하세요.")
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                Spacer().frame(height: 6)

                Text("등록하실 상점의 상세 정보를 입력해주세요.")
                    .font(AppFont.regular(12))
                    .foregroundColor(Color(hex: "595959"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)

                Spacer().frame(height: 40)

                // 대표 이미지
                Text("대표 이미지")
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "121212"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 21)

                Spacer().frame(height: 8)

                // Image upload picker
                PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                    ZStack {
                        if let selectedImage {
                            Image(uiImage: selectedImage)
                                .resizable()
                                .scaledToFill()
                                .frame(maxWidth: .infinity)
                                .frame(height: 229)
                                .clipShape(RoundedRectangle(cornerRadius: 15))
                        } else {
                            VStack(spacing: 8) {
                                Image("IconUpload")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 24, height: 24)
                                Text("이미지를 업로드하세요")
                                    .font(AppFont.medium(16))
                                    .foregroundColor(Color(hex: "BFBFBF"))
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 229)
                            .background(
                                RoundedRectangle(cornerRadius: 15)
                                    .fill(Color(hex: "F8F8F8"))
                            )
                        }

                        if viewModel.isUploadingImage {
                            RoundedRectangle(cornerRadius: 15)
                                .fill(Color.black.opacity(0.3))
                                .frame(maxWidth: .infinity)
                                .frame(height: 229)
                            ProgressView()
                                .tint(.white)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .onChange(of: selectedPhotoItem) { _, newItem in
                    Task {
                        guard let item = newItem,
                              let data = try? await item.loadTransferable(type: Data.self),
                              let uiImage = UIImage(data: data) else { return }
                        selectedImage = uiImage
                        if let uploadData = uiImage.jpegDataForUpload() {
                            viewModel.uploadImage(uploadData)
                        }
                    }
                }

                Spacer().frame(height: 30)

                // 상점명
                formLabel("상점명", required: true)
                Spacer().frame(height: 8)
                formTextField($storeName, placeholder: "알파시티 카페")

                Spacer().frame(height: 20)

                // 상점 주소
                formLabel("상점 주소", required: true)
                Spacer().frame(height: 8)
                formTextField($storeAddress, placeholder: "대구광역시 수성구 알파시티 2로 33")
                Spacer().frame(height: 6)
                formTextField($storeAddressDetail, placeholder: "태왕알파시티 302호")

                Spacer().frame(height: 20)

                // 전화 번호
                formLabel("전화 번호", required: true)
                Spacer().frame(height: 8)
                formTextField($phoneNumber, placeholder: "053-123-4567")

                Spacer().frame(height: 20)

                // 운영 요일
                formLabel("운영 요일", required: true)
                Spacer().frame(height: 8)
                HStack(spacing: 4) {
                    ForEach(["월", "화", "수", "목", "금", "토", "일"], id: \.self) { day in
                        let isSelected = selectedDays.contains(day)
                        Button {
                            if selectedDays.contains(day) {
                                selectedDays.remove(day)
                            } else {
                                selectedDays.insert(day)
                            }
                        } label: {
                            Text(day)
                                .font(AppFont.medium(16))
                                .foregroundColor(isSelected ? AppColor.primary : Color(hex: "8F8F8F"))
                                .frame(width: 48, height: 48)
                                .background(
                                    RoundedRectangle(cornerRadius: 13)
                                        .fill(isSelected ? Color(hex: "EDF7FF") : Color(hex: "F8F8F8"))
                                )
                                .overlay(
                                    isSelected ?
                                    RoundedRectangle(cornerRadius: 13)
                                        .stroke(AppColor.primary, lineWidth: 1)
                                    : nil
                                )
                        }
                    }
                }
                .padding(.horizontal, 21)

                Spacer().frame(height: 20)

                // 운영 시간
                formLabel("운영 시간", required: true)
                Spacer().frame(height: 8)
                HStack(spacing: 0) {
                    TextField("09:00", text: $openTime)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                        .keyboardType(.numberPad)
                        .onChange(of: openTime) { _, v in openTime = formatTimeInput(v) }
                        .padding(.horizontal, 10)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                        )

                    Text("~")
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                        .padding(.horizontal, 12)

                    TextField("20:00", text: $closeTime)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                        .keyboardType(.numberPad)
                        .onChange(of: closeTime) { _, v in closeTime = formatTimeInput(v) }
                        .padding(.horizontal, 10)
                        .frame(height: 48)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                        )
                }
                .padding(.horizontal, 21)

                Spacer().frame(height: 20)

                // 대표자 이름
                formLabel("대표자 이름", required: true)
                Spacer().frame(height: 8)
                formTextField($ownerName, placeholder: "홍길동")

                Spacer().frame(height: 20)

                // 상점 고유 코드
                formLabel("상점 고유 코드", required: true)
                Spacer().frame(height: 8)
                HStack(spacing: 7) {
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                            .frame(height: 48)
                        Text(storeCode.isEmpty ? "#" : storeCode)
                            .font(AppFont.medium(14))
                            .foregroundColor(storeCode.isEmpty ? Color(hex: "BFBFBF") : Color(hex: "121212"))
                            .padding(.horizontal, 10)
                    }

                    Button {
                        storeCode = "#\(UUID().uuidString.prefix(8).uppercased())"
                    } label: {
                        Text("생    성")
                            .font(AppFont.medium(16))
                            .foregroundColor(AppColor.primary)
                            .frame(width: 81, height: 48)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(Color(hex: "EDF7FF"))
                            )
                    }
                }
                .padding(.horizontal, 21)

                Spacer().frame(height: 6)

                Text("상점 고유 코드는 생성버튼을 클릭하면 자동 생성됩니다.")
                    .font(AppFont.medium(14))
                    .foregroundColor(Color(hex: "8F8F8F"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 21)

                Spacer().frame(height: 20)

                // 상점 설명
                formLabel("상점 설명", required: true)
                Spacer().frame(height: 8)
                ZStack(alignment: .topLeading) {
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
                        .frame(height: 96)

                    if storeDescription.isEmpty {
                        Text("상점에 대해 간단히 소개해주세요.")
                            .font(AppFont.medium(16))
                            .foregroundColor(Color(hex: "BFBFBF"))
                            .padding(10)
                    }

                    TextEditor(text: $storeDescription)
                        .font(AppFont.medium(16))
                        .foregroundColor(Color(hex: "121212"))
                        .frame(height: 96)
                        .padding(5)
                        .scrollContentBackground(.hidden)
                }
                .padding(.horizontal, 21)

                Spacer().frame(height: 20)

                // Notice
                Text("등록 신청 후 관리자 검토를 거쳐 승인됩니다.\n승인 완료 시 알림을 보내드립니다.")
                    .font(AppFont.regular(10))
                    .foregroundColor(Color(hex: "8F8F8F"))
                    .lineSpacing(4)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 11)
                            .fill(Color(hex: "F8F8F8"))
                    )
                    .padding(.horizontal, 20)

                Spacer().frame(height: 20)

                // Submit button
                GradientActionButton(title: viewModel.isSubmitting ? "등록 중..." : "등록 신청하기") {
                    guard !viewModel.isSubmitting else { return }
                    hideKeyboard()
                    if storeName.isEmpty || ownerName.isEmpty || phoneNumber.isEmpty {
                        errorMessage = "상점명, 운영자명, 연락처를 입력해주세요"
                        showError = true
                        return
                    }
                    viewModel.registerStore(
                        name: storeName,
                        category: selectedCategory?.rawValue ?? "",
                        ownerName: ownerName,
                        phone: phoneNumber,
                        address: storeAddress,
                        addressDetail: storeAddressDetail,
                        description: storeDescription,
                        storeCode: storeCode,
                        operatingDays: selectedDays.sorted().joined(separator: ","),
                        openTime: openTime.replacingOccurrences(of: " ", with: ""),
                        closeTime: closeTime.replacingOccurrences(of: " ", with: "")
                    )
                }
                .padding(.horizontal, 20)
                .onReceive(viewModel.$submitResult) { result in
                    guard let result else { return }
                    switch result {
                    case .success:
                        currentStep = 3
                        viewModel.clearResult()
                    case .error(let message):
                        errorMessage = message
                        showError = true
                        viewModel.clearResult()
                    }
                }

                Spacer().frame(height: 30)
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .simultaneousGesture(TapGesture().onEnded { hideKeyboard() })
        .toolbar {
            // 숫자패드(운영시간) 등 리턴키 없는 키보드도 닫을 수 있도록 키보드 상단에 완료 버튼 제공
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("완료") { hideKeyboard() }
            }
        }
    }

    // ===================== Step 3: Completion =====================

    private var step3Completion: some View {
        VStack {
            Spacer()

            Image("Walk")
                .resizable()
                .scaledToFit()
                .frame(width: 69, height: 81)

            Spacer().frame(height: 18)

            Text("정상적으로 등록 요청되었습니다!")
                .font(AppFont.medium(16))
                .foregroundColor(Color(hex: "121212"))

            Spacer().frame(height: 16)

            Text("관리자의승인 후 정식 등록됩니다.\n승인 후 알림으로 알려드립니다.")
                .font(AppFont.regular(12))
                .foregroundColor(Color(hex: "595959"))
                .lineSpacing(4)
                .multilineTextAlignment(.center)

            Spacer()

            GradientActionButton(title: "홈으로 돌아가기") {
                onBackTapped()
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 20)
        }
    }

    // ===================== Helper Views =====================

    private func formLabel(_ label: String, required: Bool = false) -> some View {
        HStack(spacing: 0) {
            Text(label)
                .font(AppFont.medium(16))
                .foregroundColor(Color(hex: "121212"))
            if required {
                Text(" *")
                    .font(AppFont.medium(11))
                    .foregroundColor(AppColor.primary)
            }
            Spacer()
        }
        .padding(.horizontal, 20)
    }

    private func formTextField(_ text: Binding<String>, placeholder: String) -> some View {
        TextField(placeholder, text: text)
            .font(AppFont.medium(16))
            .foregroundColor(Color(hex: "121212"))
            .padding(.horizontal, 10)
            .frame(height: 48)
            .background(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(hex: "E9E9E9"), lineWidth: 1)
            )
            .padding(.horizontal, 20)
    }
}

// MARK: - Gradient Action Button

private struct GradientActionButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(AppFont.semibold(16))
                .foregroundColor(Color(hex: "F8F8F8"))
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(
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
                )
        }
    }
}

#Preview {
    StoreRegisterView(onBackTapped: {})
}
