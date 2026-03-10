import SwiftUI

struct RaffleEventDetailView: View {
    let eventId: Int
    var onBackTapped: (() -> Void)?
    @StateObject private var viewModel = EventDetailViewModel()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack(spacing: 12) {
                Button(action: { onBackTapped?() }) {
                    Image("IconBackArrow")
                        .renderingMode(.original)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 26)
                }
                Text("추첨 이벤트")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)

            Divider().foregroundColor(Color(hex: "E2E2E2"))

            if viewModel.isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let event = viewModel.event {
                ScrollView(.vertical, showsIndicators: false) {
                    VStack(alignment: .leading, spacing: 0) {
                        // 이벤트 이미지
                        eventImage(event)

                        VStack(alignment: .leading, spacing: 0) {
                            // 상태 뱃지 + 제목
                            let isOpen = event.status != "ended"
                            HStack(spacing: 10) {
                                Text(isOpen ? "참여 가능" : "마  감")
                                    .font(AppFont.semibold(11))
                                    .foregroundColor(.white)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 3)
                                    .background(
                                        Capsule().fill(isOpen ? AppColor.primary : Color(hex: "8F8F8F"))
                                    )
                                Text(event.name)
                                    .font(AppFont.semibold(18))
                                    .foregroundColor(Color(hex: "121212"))
                            }
                            .padding(.top, 16)

                            // 경품 정보
                            if let reward = event.reward, !reward.isEmpty {
                                VStack(alignment: .leading, spacing: 6) {
                                    Text("경품 정보")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(Color(hex: "121212"))

                                    HStack(spacing: 3) {
                                        Image("IconCoupon")
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 20, height: 20)
                                        Text("상품: \(reward)")
                                            .font(AppFont.medium(13))
                                            .foregroundColor(AppColor.primary)
                                    }

                                    if let winnerCount = event.winnerCount, winnerCount > 0 {
                                        Text("당첨자 수: \(winnerCount)명")
                                            .font(AppFont.medium(13))
                                            .foregroundColor(Color(hex: "595959"))
                                    }
                                }
                                .padding(14)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color(hex: "F8F8F8"))
                                )
                                .padding(.top, 16)
                            }

                            // 설명
                            if let description = event.description, !description.isEmpty {
                                Text(description)
                                    .font(AppFont.regular(13))
                                    .foregroundColor(Color(hex: "595959"))
                                    .lineSpacing(5)
                                    .padding(.top, 16)
                            }

                            // 이벤트 기간
                            Text("이벤트 기간: \(formatDate(event.startDate)) ~ \(formatDate(event.endDate))")
                                .font(AppFont.regular(12))
                                .foregroundColor(Color(hex: "828282"))
                                .padding(.top, 12)

                            // 참여 인원
                            Text("\(formattedCount(event.participantCount ?? 0))명 참여")
                                .font(AppFont.regular(12))
                                .foregroundColor(Color(hex: "828282"))
                                .padding(.top, 4)

                            Divider()
                                .padding(.top, 20)

                            // 번호 발급 영역
                            raffleNumberSection(event)
                        }
                        .padding(.horizontal, 20)
                    }
                }
            }
        }
        .background(Color.white)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.fetchEventDetail(eventId: eventId)
        }
        .onChange(of: viewModel.message) { msg in
            // 메시지 표시 후 자동 초기화
            if msg != nil {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                    viewModel.message = nil
                }
            }
        }
        .overlay(alignment: .bottom) {
            if let message = viewModel.message {
                Text(message)
                    .font(AppFont.medium(14))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(
                        Capsule().fill(Color.black.opacity(0.8))
                    )
                    .padding(.bottom, 40)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .animation(.easeInOut, value: viewModel.message)
            }
        }
    }

    @ViewBuilder
    private func eventImage(_ event: EventData) -> some View {
        ZStack {
            if let imageUrl = event.imageUrl, !imageUrl.isEmpty {
                let fullURL = imageUrl.hasPrefix("http") ? imageUrl : "\(APIClient.serverURL)\(imageUrl)"
                AsyncImage(url: URL(string: fullURL)) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .scaledToFill()
                            .frame(maxWidth: .infinity)
                            .frame(height: 200)
                            .clipped()
                    default:
                        placeholderImage(event.name)
                    }
                }
            } else {
                placeholderImage(event.name)
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 200)
    }

    private func placeholderImage(_ name: String) -> some View {
        ZStack {
            Color(hex: "E8E8E8")
            Text(String(name.prefix(1)))
                .font(AppFont.bold(40))
                .foregroundColor(Color(hex: "B5B5B5"))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 200)
    }

    @ViewBuilder
    private func raffleNumberSection(_ event: EventData) -> some View {
        let hasParticipated = event.isParticipated == true || viewModel.myRaffleNumber != nil
        let isOpen = event.status != "ended"

        VStack(spacing: 16) {
            if hasParticipated, let number = viewModel.myRaffleNumber ?? event.myRaffleNumber {
                // 번호 발급 완료 상태
                VStack(spacing: 12) {
                    Text("나의 추첨 번호")
                        .font(AppFont.medium(14))
                        .foregroundColor(Color(hex: "595959"))

                    Text("#\(number)")
                        .font(AppFont.bold(48))
                        .foregroundColor(AppColor.primary)

                    Text("추첨은 현장에서 진행됩니다")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color(hex: "F0F7FF"))
                )

                // 비활성 버튼
                Text("번호 발급 완료")
                    .font(AppFont.semibold(16))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "CCCCCC"))
                    )
            } else if isOpen {
                // 발급 가능 상태
                Button(action: {
                    viewModel.participate(eventId: event.id)
                }) {
                    HStack {
                        if viewModel.isParticipating {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Text("번호 발급받기")
                                .font(AppFont.semibold(16))
                                .foregroundColor(.white)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(AppColor.primary)
                    )
                }
                .disabled(viewModel.isParticipating)
            } else {
                // 마감
                Text("이벤트가 종료되었습니다")
                    .font(AppFont.semibold(16))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "CCCCCC"))
                    )
            }
        }
        .padding(.top, 20)
        .padding(.bottom, 40)
    }

    private func formatDate(_ dateStr: String) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withFullDate]
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "yyyy.MM.dd"
        if let date = formatter.date(from: String(dateStr.prefix(10))) {
            return displayFormatter.string(from: date)
        }
        return String(dateStr.prefix(10))
    }

    private func formattedCount(_ count: Int) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter.string(from: NSNumber(value: count)) ?? "\(count)"
    }
}
