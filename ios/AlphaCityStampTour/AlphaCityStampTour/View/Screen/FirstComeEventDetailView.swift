import SwiftUI

struct FirstComeEventDetailView: View {
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
                Text("선착순 사은품")
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
                let hasParticipated = event.isParticipated == true || viewModel.myRaffleNumber != nil
                let remaining = event.participantLimit - (event.participantCount ?? 0)
                let isSoldOut = remaining <= 0
                let isOpen = event.status != "ended"

                if hasParticipated {
                    // 참여 완료 화면
                    participationCompleteView(event)
                } else {
                    // 상세 정보 화면
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(alignment: .leading, spacing: 0) {
                            // 이벤트 이미지
                            eventImage(event)

                            VStack(alignment: .leading, spacing: 0) {
                                // 유형 태그 + 상태
                                HStack(spacing: 8) {
                                    Text("선착순")
                                        .font(AppFont.semibold(11))
                                        .foregroundColor(AppColor.primary)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 3)
                                        .background(
                                            Capsule().fill(Color(hex: "EDF7FF"))
                                        )

                                    Text(isOpen && !isSoldOut ? "진행중" : "마감")
                                        .font(AppFont.semibold(11))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 3)
                                        .background(
                                            Capsule().fill(isOpen && !isSoldOut ? AppColor.primary : Color(hex: "8F8F8F"))
                                        )
                                }
                                .padding(.top, 16)

                                // 제목
                                Text(event.name)
                                    .font(AppFont.semibold(18))
                                    .foregroundColor(Color(hex: "121212"))
                                    .padding(.top, 10)

                                // 설명
                                if let description = event.description, !description.isEmpty {
                                    Text(description)
                                        .font(AppFont.regular(13))
                                        .foregroundColor(Color(hex: "595959"))
                                        .lineSpacing(5)
                                        .padding(.top, 12)
                                }

                                // 잔여 수량
                                VStack(spacing: 8) {
                                    HStack {
                                        Text("잔여 수량")
                                            .font(AppFont.regular(12))
                                            .foregroundColor(Color(hex: "595959"))
                                        Spacer()
                                        Text("\(max(remaining, 0))/\(event.participantLimit)명")
                                            .font(AppFont.bold(14))
                                            .foregroundColor(isSoldOut ? Color(hex: "8F8F8F") : AppColor.primary)
                                    }

                                    // 프로그레스 바
                                    GeometryReader { geo in
                                        ZStack(alignment: .leading) {
                                            Capsule()
                                                .fill(Color(hex: "F8F8F8"))
                                                .frame(height: 8)
                                            let progress = event.participantLimit > 0
                                                ? CGFloat(event.participantCount ?? 0) / CGFloat(event.participantLimit)
                                                : 0
                                            Capsule()
                                                .fill(isSoldOut ? Color(hex: "8E8E8E") : AppColor.primary)
                                                .frame(width: geo.size.width * min(progress, 1.0), height: 8)
                                        }
                                    }
                                    .frame(height: 8)
                                }
                                .padding(16)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color(hex: "FAFAFA"))
                                )
                                .padding(.top, 16)

                                // 수령 방법
                                VStack(alignment: .leading, spacing: 12) {
                                    Text("수령 방법")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(Color(hex: "121212"))

                                    receiptStep(number: 1, text: "이벤트 부스 스텝이 참여하기 버튼 클릭")
                                    receiptStep(number: 2, text: "참여완료 화면 확인")
                                    receiptStep(number: 3, text: "사은품 수령")
                                    receiptStep(number: 4, text: "재발급 불가")
                                }
                                .padding(16)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color(hex: "F8F8F8"))
                                )
                                .padding(.top, 16)

                                // 참여 버튼
                                if isOpen && !isSoldOut {
                                    Button(action: {
                                        viewModel.participate(eventId: event.id)
                                    }) {
                                        HStack {
                                            if viewModel.isParticipating {
                                                ProgressView().tint(.white)
                                            } else {
                                                Text("참여하기")
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
                                    .padding(.top, 24)
                                } else {
                                    Text("마감됨")
                                        .font(AppFont.semibold(16))
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 16)
                                        .background(
                                            RoundedRectangle(cornerRadius: 12)
                                                .fill(Color(hex: "CCCCCC"))
                                        )
                                        .padding(.top, 24)
                                }
                            }
                            .padding(.horizontal, 20)
                            .padding(.bottom, 40)
                        }
                    }
                }
            }
        }
        .background(Color.white)
        .navigationBarHidden(true)
        .onAppear {
            viewModel.fetchEventDetail(eventId: eventId)
        }
        .overlay(alignment: .bottom) {
            if let message = viewModel.message, !(viewModel.event?.isParticipated == true || viewModel.myRaffleNumber != nil) {
                Text(message)
                    .font(AppFont.medium(14))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Capsule().fill(Color.black.opacity(0.8)))
                    .padding(.bottom, 40)
            }
        }
    }

    // MARK: - 참여 완료 화면

    @ViewBuilder
    private func participationCompleteView(_ event: EventData) -> some View {
        VStack(spacing: 0) {
            Spacer()

            VStack(spacing: 20) {
                Image(systemName: "checkmark.circle.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 64, height: 64)
                    .foregroundColor(AppColor.primary)

                Text("참여 완료!")
                    .font(AppFont.bold(24))
                    .foregroundColor(Color(hex: "121212"))

                if let number = viewModel.myRaffleNumber ?? event.myRaffleNumber {
                    Text("참여 번호: #\(number)")
                        .font(AppFont.bold(20))
                        .foregroundColor(AppColor.primary)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(Color(hex: "F0F7FF"))
                        )
                }

                Text(event.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "595959"))

                // 재발급 불가 안내
                VStack(spacing: 8) {
                    HStack(spacing: 4) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(Color(hex: "E74C3C"))
                            .font(.system(size: 14))
                        Text("재발급 불가")
                            .font(AppFont.semibold(14))
                            .foregroundColor(Color(hex: "E74C3C"))
                    }
                    Text("사은품 수령 후 재발급이 불가합니다.\n부스에서 이 화면을 보여주세요.")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .multilineTextAlignment(.center)
                }
                .padding(16)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(hex: "FFF5F5"))
                )
                .padding(.horizontal, 20)
            }

            Spacer()

            // 돌아가기 버튼
            Button(action: { onBackTapped?() }) {
                Text("돌아가기")
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

    // MARK: - Components

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

    private func receiptStep(number: Int, text: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Text("\(number)")
                .font(AppFont.bold(12))
                .foregroundColor(.white)
                .frame(width: 22, height: 22)
                .background(Circle().fill(AppColor.primary))
            Text(text)
                .font(AppFont.regular(13))
                .foregroundColor(Color(hex: "595959"))
        }
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
}
