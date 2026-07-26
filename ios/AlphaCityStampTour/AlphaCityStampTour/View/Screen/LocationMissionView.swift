//
//  LocationMissionView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct LocationMissionView: View {
    let mission: MissionData
    var onDismiss: (() -> Void)?
    var onCompleted: (() -> Void)?

    @StateObject private var viewModel = MissionViewModel()

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
                Text("위치 인증 미션")
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

                    // 장소 정보 카드
                    if let place = mission.place {
                        VStack(alignment: .leading, spacing: 10) {
                            Text(place.name)
                                .font(AppFont.semibold(18))
                                .foregroundColor(Color(hex: "121212"))

                            Text("현재 위치를 인증하면 스탬프를 획득할 수 있습니다")
                                .font(AppFont.regular(14))
                                .foregroundColor(Color(hex: "595959"))
                                .lineSpacing(3)

                            HStack(spacing: 6) {
                                Image(systemName: "location.circle")
                                    .font(.system(size: 13))
                                    .foregroundColor(AppColor.primary)
                                Text("GPS 인증 사용")
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
                    }

                    Spacer()

                    // 위치 인증하기 버튼
                    VStack(spacing: 12) {
                        Button(action: {
                            guard let lat = mission.place?.latitude,
                                  let lng = mission.place?.longitude else { return }
                            viewModel.completeLocationMission(
                                missionId: mission.id,
                                targetLat: lat,
                                targetLng: lng
                            )
                        }) {
                            HStack(spacing: 8) {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Image(systemName: "location.fill")
                                        .font(.system(size: 15))
                                    Text("위치 인증하기")
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

                        // GPS 안내
                        Text("위치 서비스가 켜져있는지 확인해주세요")
                            .font(AppFont.regular(13))
                            .foregroundColor(Color(hex: "94A3B8"))
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

        if viewModel.isCompleted, let stamp = viewModel.earnedStamp {
            StampEarnedView(stamp: stamp, onDismiss: {
                onCompleted?()
                onDismiss?()
            })
        }
        }
    }

    private var completionView: some View {
        VStack(spacing: 20) {
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 64, height: 64)
                .foregroundColor(Color(hex: "16A34A"))

            Text("위치 인증 완료!")
                .font(AppFont.bold(24))
                .foregroundColor(Color(hex: "121212"))

            Text(mission.name)
                .font(AppFont.medium(16))
                .foregroundColor(Color(hex: "595959"))

            if let place = mission.place {
                Text(place.name)
                    .font(AppFont.regular(14))
                    .foregroundColor(Color(hex: "828282"))
            }

            if let dist = viewModel.currentDistance {
                Text("현재 거리: \(dist)m")
                    .font(AppFont.medium(14))
                    .foregroundColor(Color(hex: "16A34A"))
            }

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
