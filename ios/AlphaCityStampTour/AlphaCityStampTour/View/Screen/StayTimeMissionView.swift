//
//  StayTimeMissionView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct StayTimeMissionView: View {
    let mission: MissionData
    var onDismiss: (() -> Void)?
    var onCompleted: (() -> Void)?

    @StateObject private var viewModel = MissionViewModel()

    private var hasPlace: Bool {
        mission.place != nil
    }

    private var needsLocationVerification: Bool {
        hasPlace && !viewModel.locationVerified
    }

    var body: some View {
        ZStack {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 12) {
                Button(action: {
                    viewModel.stopTimer()
                    onDismiss?()
                }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color(hex: "121212"))
                }
                Text("체류시간 미션")
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
                stayTimeContentView
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
                    .fill(Color(hex: "FFFBEB"))
                    .frame(width: 120, height: 120)
                Circle()
                    .stroke(Color(hex: "FDE68A"), lineWidth: 2)
                    .frame(width: 140, height: 140)
                Image(systemName: "mappin.circle.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 48, height: 48)
                    .foregroundColor(Color(hex: "D97706"))
            }

            Spacer().frame(height: 32)

            // 안내 카드
            if let place = mission.place {
                VStack(alignment: .leading, spacing: 10) {
                    Text("위치 확인 필요")
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))

                    Text("\(place.name)에서 위치를 먼저 확인해야\n체류시간 미션에 참여할 수 있습니다.")
                        .font(AppFont.regular(14))
                        .foregroundColor(Color(hex: "595959"))
                        .lineSpacing(3)

                    HStack(spacing: 6) {
                        Image(systemName: "location.circle")
                            .font(.system(size: 13))
                            .foregroundColor(Color(hex: "D97706"))
                        Text("100m 이내에서 인증 가능")
                            .font(AppFont.medium(13))
                            .foregroundColor(Color(hex: "D97706"))
                    }
                    .padding(.top, 4)
                }
                .padding(20)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(
                    RoundedRectangle(cornerRadius: 14)
                        .fill(Color(hex: "FFFBEB"))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(Color(hex: "FDE68A"), lineWidth: 1)
                )
                .padding(.horizontal, 20)
            }

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
                    guard let lat = mission.place?.latitude,
                          let lng = mission.place?.longitude else { return }
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
                            .fill(Color(hex: "D97706"))
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

    // MARK: - 체류시간 콘텐츠 화면

    private var stayTimeContentView: some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 20) {
                // 위치 확인 완료 배지 (위치가 있는 미션인 경우)
                if hasPlace {
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
                }

                // 미션 유형 배지
                Text("체류시간")
                    .font(AppFont.semibold(11))
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 3)
                    .background(Capsule().fill(Color(hex: "D97706")))

                // 미션명
                Text(mission.name)
                    .font(AppFont.semibold(20))
                    .foregroundColor(Color(hex: "121212"))
                    .multilineTextAlignment(.center)

                // 장소명
                if let place = mission.place {
                    HStack(spacing: 6) {
                        Image(systemName: "mappin.and.ellipse")
                            .foregroundColor(Color(hex: "D97706"))
                            .font(.system(size: 14))
                        Text(place.name)
                            .font(AppFont.medium(14))
                            .foregroundColor(Color(hex: "595959"))
                    }
                }

                // 타이머
                Text(viewModel.formattedTime)
                    .font(.system(size: 56, weight: .bold, design: .monospaced))
                    .foregroundColor(viewModel.isTimerRunning ? Color(hex: "D97706") : Color(hex: "121212"))
                    .padding(.vertical, 20)

                // 필요 체류시간 안내
                Text("필요 체류시간: \(mission.stayMinutes ?? 0)분")
                    .font(AppFont.regular(13))
                    .foregroundColor(Color(hex: "828282"))

                if viewModel.isTimerRunning {
                    Text("타이머가 작동 중입니다.\n이 화면을 유지해주세요.")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "D97706"))
                        .multilineTextAlignment(.center)
                        .padding(12)
                        .background(
                            RoundedRectangle(cornerRadius: 10)
                                .fill(Color(hex: "FFFBEB"))
                        )
                }
            }

            Spacer()

            // 시작/중지 버튼
            if viewModel.isTimerRunning {
                Button(action: { viewModel.stopTimer() }) {
                    Text("타이머 중지")
                        .font(AppFont.semibold(16))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(hex: "DC2626"))
                        )
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 40)
            } else if !viewModel.isCompleted {
                Button(action: {
                    viewModel.startStayTimeMission(
                        missionId: mission.id,
                        minutes: mission.stayMinutes ?? 1
                    )
                }) {
                    HStack {
                        if viewModel.isLoading {
                            ProgressView().tint(.white)
                        } else {
                            Image(systemName: "timer")
                                .font(.system(size: 16))
                            Text("체류 시작")
                                .font(AppFont.semibold(16))
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "D97706"))
                    )
                }
                .disabled(viewModel.isLoading)
                .padding(.horizontal, 20)
                .padding(.bottom, 40)
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
                .foregroundColor(Color(hex: "D97706"))

            Text("체류시간 미션 완료!")
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
                            .fill(Color(hex: "D97706"))
                    )
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 40)
        }
    }
}
