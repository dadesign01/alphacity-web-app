//
//  StampEarnedView.swift
//  AlphaCityStampTour
//

import SwiftUI

struct StampEarnedView: View {
    let stamp: StampData?
    var onDismiss: (() -> Void)?

    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture { onDismiss?() }

            VStack(spacing: 0) {
                ZStack(alignment: .topTrailing) {
                    VStack(spacing: 0) {
                        // X 버튼
                        HStack {
                            Spacer()
                            Button(action: { onDismiss?() }) {
                                Image(systemName: "xmark")
                                    .font(.system(size: 16, weight: .medium))
                                    .foregroundColor(Color(hex: "9CA3AF"))
                            }
                        }
                        .padding(.bottom, 4)

                        // 정답입니다!
                        Text("정답입니다!")
                            .font(AppFont.bold(22))
                            .foregroundColor(Color(hex: "121212"))
                            .padding(.bottom, 24)

                        // 스탬프 이미지 (황금 원형 배지)
                        ZStack {
                            // 빛나는 배경
                            Circle()
                                .fill(
                                    RadialGradient(
                                        colors: [Color(hex: "FFF7D6"), Color(hex: "FFE082"), Color(hex: "FFC107")],
                                        center: .center,
                                        startRadius: 0,
                                        endRadius: 65
                                    )
                                )
                                .frame(width: 130, height: 130)

                            // 스탬프 이미지
                            if let imageUrl = stamp?.imageUrl, !imageUrl.isEmpty {
                                let fullUrl = imageUrl.hasPrefix("http") ? imageUrl : APIClient.serverURL + imageUrl
                                AsyncImage(url: URL(string: fullUrl)) { phase in
                                    switch phase {
                                    case .success(let image):
                                        image
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: 90, height: 90)
                                            .clipShape(Circle())
                                    default:
                                        Image("stamp_trophy")
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 80, height: 80)
                                    }
                                }
                            } else {
                                Image(systemName: "seal.fill")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 60, height: 60)
                                    .foregroundColor(Color(hex: "D97706"))
                            }

                            // STAMP 라벨
                            VStack {
                                Spacer()
                                Text("STAMP")
                                    .font(.system(size: 10, weight: .bold))
                                    .foregroundColor(.white)
                                    .tracking(1)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 2)
                                    .background(
                                        RoundedRectangle(cornerRadius: 4)
                                            .fill(Color(hex: "C8860A"))
                                    )
                                    .offset(y: 6)
                            }
                            .frame(width: 130, height: 130)
                        }
                        .padding(.bottom, 20)

                        // 스탬프가 적립되었습니다.
                        Text("스탬프가 적립되었습니다.")
                            .font(AppFont.semibold(16))
                            .foregroundColor(Color(hex: "121212"))
                            .padding(.bottom, 6)

                        // 스탬프 이름
                        if let name = stamp?.name, !name.isEmpty {
                            Text(name)
                                .font(AppFont.regular(13))
                                .foregroundColor(Color(hex: "6B7280"))
                                .multilineTextAlignment(.center)
                                .padding(.bottom, 6)
                        }

                        Spacer().frame(height: 24)

                        // 확인 버튼
                        Button(action: { onDismiss?() }) {
                            Text("확인")
                                .font(AppFont.semibold(16))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(AppColor.primary)
                                )
                        }
                    }
                    .padding(24)
                    .background(Color.white)
                    .cornerRadius(20)
                }
            }
            .padding(.horizontal, 40)
        }
    }
}
