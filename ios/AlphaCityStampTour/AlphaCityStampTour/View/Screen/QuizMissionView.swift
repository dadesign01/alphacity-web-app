//
//  QuizMissionView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct QuizMissionView: View {
    let mission: MissionData
    var programLat: Double? = nil
    var programLng: Double? = nil
    var onDismiss: (() -> Void)?
    var onCompleted: (() -> Void)?

    @StateObject private var viewModel = MissionViewModel()
    @State private var selectedOption: String? = nil
    @State private var textAnswer: String = ""

    private var isShortAnswer: Bool {
        mission.options == nil || mission.options?.isEmpty == true
    }

    private var canSubmit: Bool {
        isShortAnswer ? !textAnswer.trimmingCharacters(in: .whitespaces).isEmpty : selectedOption != nil
    }

    private var targetLat: Double? {
        mission.place?.latitude ?? programLat
    }
    private var targetLng: Double? {
        mission.place?.longitude ?? programLng
    }
    private var hasLocation: Bool {
        targetLat != nil && targetLng != nil
    }
    private var needsLocationVerification: Bool {
        hasLocation && !viewModel.locationVerified
    }

    var body: some View {
        ZStack {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 12) {
                Button(action: { onDismiss?() }) {
                    Image("IconBackArrow")
                        .renderingMode(.original)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 26)
                }
                Text("퀴즈 미션")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)

            Divider().foregroundColor(Color(hex: "E2E2E2"))

            if viewModel.isCompleted {
                completionView
            } else if needsLocationVerification {
                locationVerificationView
            } else {
                quizContentView
            }
        }
        .background(Color.white)
        .alert("알림", isPresented: $viewModel.showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(viewModel.alertMessage)
        }

        if viewModel.isCompleted, let stamp = viewModel.earnedStamp {
            StampEarnedView(stamp: stamp, onDismiss: {
                onCompleted?()
                onDismiss?()
            })
        }
        }
    }

    // MARK: - 위치 확인 화면

    private var locationVerificationView: some View {
        VStack(spacing: 0) {
            Spacer()

            // 위치 아이콘
            ZStack {
                Circle()
                    .fill(Color(hex: "EFF6FF"))
                    .frame(width: 120, height: 120)
                Circle()
                    .stroke(Color(hex: "DBEAFE"), lineWidth: 2)
                    .frame(width: 140, height: 140)
                Image(systemName: "mappin.circle.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .foregroundColor(AppColor.primary)
            }

            Spacer().frame(height: 32)

            // 안내 카드
            let placeName = mission.place?.name ?? "지정 장소"
            VStack(alignment: .leading, spacing: 10) {
                Text("위치 확인 필요")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))

                Text("\(placeName)에서 위치를 먼저 확인해야\n퀴즈 미션에 참여할 수 있습니다.")
                        .font(AppFont.regular(14))
                        .foregroundColor(Color(hex: "595959"))
                        .lineSpacing(3)

                    HStack(spacing: 6) {
                        Image(systemName: "location.circle")
                            .font(.system(size: 13))
                            .foregroundColor(AppColor.primary)
                        Text("100m 이내에서 인증 가능")
                            .font(AppFont.medium(13))
                            .foregroundColor(AppColor.primary)
                    }
                    .padding(.top, 4)
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color(hex: "F8FAFC"))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color(hex: "E2E8F0"), lineWidth: 1)
                )
                .padding(.horizontal, 20)

            // 현재 거리 표시
            if let dist = viewModel.currentDistance {
                Spacer().frame(height: 16)
                Text("현재 거리: \(dist)m")
                    .font(AppFont.semibold(15))
                    .foregroundColor(dist <= 100 ? Color(hex: "16A34A") : Color(hex: "EA580C"))
            }

            Spacer()

            // 위치 확인 버튼
            VStack(spacing: 12) {
                Button(action: {
                    guard let lat = targetLat, let lng = targetLng else { return }
                    viewModel.verifyLocation(targetLat: lat, targetLng: lng)
                }) {
                    HStack(spacing: 8) {
                        if viewModel.isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Image(systemName: "location.fill")
                                .font(.system(size: 15))
                            Text("위치 확인")
                                .font(AppFont.semibold(16))
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(AppColor.primary)
                    )
                }
                .disabled(viewModel.isLoading)

                Text("위치 서비스가 켜져있는지 확인해주세요")
                    .font(AppFont.regular(13))
                    .foregroundColor(Color(hex: "94A3B8"))
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
    }

    // MARK: - 퀴즈 콘텐츠 화면

    private var quizContentView: some View {
        ScrollView(.vertical, showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {
                // 위치 확인 완료 배지 (위치가 있는 미션인 경우)
                if hasLocation {
                    HStack(spacing: 6) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 12))
                            .foregroundColor(Color(hex: "16A34A"))
                        Text("위치 확인 완료 (\(viewModel.currentDistance ?? 0)m)")
                            .font(AppFont.medium(12))
                            .foregroundColor(Color(hex: "16A34A"))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color(hex: "F0FFF4"))
                    )
                    .padding(.top, 24)

                    Spacer().frame(height: 16)
                }

                // 미션 유형 배지
                Text("퀴즈")
                    .font(AppFont.semibold(11))
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 3)
                    .background(Capsule().fill(AppColor.primary))
                    .padding(.top, hasLocation ? 0 : 24)

                // 미션명
                Text(mission.name)
                    .font(AppFont.semibold(20))
                    .foregroundColor(Color(hex: "121212"))
                    .padding(.top, 12)

                // 질문 카드
                if let question = mission.question, !question.isEmpty {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(question)
                            .font(AppFont.semibold(17))
                            .foregroundColor(Color(hex: "121212"))
                            .lineSpacing(5)
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(
                        RoundedRectangle(cornerRadius: 14)
                            .fill(Color(hex: "F0F7FF"))
                    )
                    .padding(.top, 24)
                }

                // 객관식 보기
                if let options = mission.options, !options.isEmpty {
                    VStack(spacing: 10) {
                        ForEach(Array(options.enumerated()), id: \.offset) { index, option in
                            optionRow(index: index, option: option)
                        }
                    }
                    .padding(.top, 20)
                }

                // 서술형 입력
                if isShortAnswer {
                    TextField("정답을 입력하세요", text: $textAnswer)
                        .font(AppFont.medium(15))
                        .padding(.horizontal, 18)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(hex: "E5E7EB"), lineWidth: 1)
                        )
                        .padding(.top, 20)
                }

                // 제출 버튼
                Button(action: {
                    let answer = isShortAnswer ? textAnswer.trimmingCharacters(in: .whitespaces) : (selectedOption ?? "")
                    guard !answer.isEmpty else { return }
                    viewModel.completeQuizMission(missionId: mission.id, answer: answer)
                }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Text("제출하기")
                                .font(AppFont.semibold(16))
                                .foregroundColor(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(canSubmit ? AppColor.primary : Color(hex: "D1D5DB"))
                    )
                }
                .disabled(!canSubmit || viewModel.isLoading)
                .padding(.top, 28)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
    }

    private func optionRow(index: Int, option: String) -> some View {
        let isSelected = selectedOption == option
        return Button(action: {
            selectedOption = option
        }) {
            HStack(spacing: 14) {
                // 라디오 버튼
                ZStack {
                    Circle()
                        .stroke(isSelected ? AppColor.primary : Color(hex: "D1D5DB"), lineWidth: 2)
                        .frame(width: 22, height: 22)
                    if isSelected {
                        Circle()
                            .fill(AppColor.primary)
                            .frame(width: 12, height: 12)
                    }
                }

                Text(option)
                    .font(AppFont.medium(15))
                    .foregroundColor(Color(hex: "121212"))
                    .multilineTextAlignment(.leading)

                Spacer()
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 16)
            .background(
                RoundedRectangle(cornerRadius: 12)
                    .fill(isSelected ? Color(hex: "EFF6FF") : Color.white)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(isSelected ? AppColor.primary : Color(hex: "E5E7EB"), lineWidth: isSelected ? 1.5 : 1)
            )
        }
        .buttonStyle(.plain)
    }

    private var completionView: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 64, height: 64)
                .foregroundColor(Color(hex: "16A34A"))

            Text("퀴즈 미션 완료!")
                .font(AppFont.bold(24))
                .foregroundColor(Color(hex: "121212"))

            Text(mission.name)
                .font(AppFont.medium(16))
                .foregroundColor(Color(hex: "595959"))

            Spacer()

            Button(action: {
                onCompleted?()
                onDismiss?()
            }) {
                Text("확인")
                    .font(AppFont.semibold(16))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(AppColor.primary)
                    )
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
    }
}
