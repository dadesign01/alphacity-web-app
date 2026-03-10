//
//  QuizMissionView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct QuizMissionView: View {
    let mission: MissionData
    var onDismiss: (() -> Void)?
    var onCompleted: (() -> Void)?

    @StateObject private var viewModel = MissionViewModel()
    @State private var selectedOption: String? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 12) {
                Button(action: { onDismiss?() }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color(hex: "121212"))
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
            } else {
                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 0) {
                        // 미션 유형 배지
                        Text("퀴즈")
                            .font(AppFont.semibold(11))
                            .foregroundColor(.white)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 3)
                            .background(Capsule().fill(AppColor.primary))
                            .padding(.top, 24)

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

                        // 제출 버튼
                        Button(action: {
                            guard let answer = selectedOption else { return }
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
                                    .fill(selectedOption != nil ? AppColor.primary : Color(hex: "D1D5DB"))
                            )
                        }
                        .disabled(selectedOption == nil || viewModel.isLoading)
                        .padding(.top, 28)
                    }
                    .padding(.horizontal, 20)
                    .padding(.bottom, 40)
                }
            }
        }
        .background(Color.white)
        .alert("알림", isPresented: $viewModel.showAlert) {
            Button("확인", role: .cancel) {}
        } message: {
            Text(viewModel.alertMessage)
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
