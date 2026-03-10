import SwiftUI

struct ExperienceEventDetailView: View {
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
                Text("체험 이벤트")
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
                let isOpen = event.status != "ended"
                let isFull = event.capacity != nil && (event.participantCount ?? 0) >= event.capacity!

                if hasParticipated {
                    participationCompleteView(event)
                } else {
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(alignment: .leading, spacing: 0) {
                            // 이벤트 이미지
                            eventImage(event)

                            VStack(alignment: .leading, spacing: 0) {
                                // 유형 태그 + 상태
                                HStack(spacing: 8) {
                                    Text("체험")
                                        .font(AppFont.semibold(11))
                                        .foregroundColor(Color(hex: "E67E22"))
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 3)
                                        .background(
                                            Capsule().fill(Color(hex: "FFF3E0"))
                                        )

                                    Text(isOpen && !isFull ? "신청 가능" : "마감")
                                        .font(AppFont.semibold(11))
                                        .foregroundColor(.white)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 3)
                                        .background(
                                            Capsule().fill(isOpen && !isFull ? Color(hex: "E67E22") : Color(hex: "8F8F8F"))
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

                                // 체험 정보 카드
                                experienceInfoCard(event)
                                    .padding(.top, 16)

                                // 이벤트 기간
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("체험 기간")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(Color(hex: "121212"))

                                    Text("\(formatDate(event.startDate)) ~ \(formatDate(event.endDate))")
                                        .font(AppFont.regular(13))
                                        .foregroundColor(Color(hex: "595959"))
                                }
                                .padding(16)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color(hex: "F8F8F8"))
                                )
                                .padding(.top, 16)

                                // 참여 안내
                                VStack(alignment: .leading, spacing: 12) {
                                    Text("참여 안내")
                                        .font(AppFont.semibold(14))
                                        .foregroundColor(Color(hex: "121212"))

                                    guideStep(number: 1, text: "아래 참여하기 버튼을 눌러 신청")
                                    guideStep(number: 2, text: "체험 장소에서 참여 완료 화면 제시")
                                    guideStep(number: 3, text: "현장에서 체험 진행")
                                }
                                .padding(16)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(Color(hex: "F8F8F8"))
                                )
                                .padding(.top, 16)

                                // 참여 버튼
                                if isOpen && !isFull {
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
                                                .fill(Color(hex: "E67E22"))
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

    // MARK: - 체험 정보 카드

    @ViewBuilder
    private func experienceInfoCard(_ event: EventData) -> some View {
        VStack(spacing: 12) {
            if let duration = event.duration {
                infoRow(icon: "clock", label: "체험 시간", value: "\(duration)분")
            }
            if let capacity = event.capacity {
                infoRow(icon: "person.2", label: "정원", value: "1회 \(capacity)명")
            }
            if let location = event.location, !location.isEmpty {
                infoRow(icon: "mappin.and.ellipse", label: "장소", value: location)
            }
            if let price = event.price {
                infoRow(icon: "wonsign.circle", label: "참가비", value: price == 0 ? "무료" : "\(formattedPrice(price))원")
            }
        }
        .padding(16)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(Color(hex: "FFF8F0"))
        )
    }

    private func infoRow(icon: String, label: String, value: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .foregroundColor(Color(hex: "E67E22"))
                .frame(width: 20)
            Text(label)
                .font(AppFont.regular(13))
                .foregroundColor(Color(hex: "595959"))
                .frame(width: 60, alignment: .leading)
            Text(value)
                .font(AppFont.semibold(13))
                .foregroundColor(Color(hex: "121212"))
            Spacer()
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
                    .foregroundColor(Color(hex: "E67E22"))

                Text("신청 완료!")
                    .font(AppFont.bold(24))
                    .foregroundColor(Color(hex: "121212"))

                Text(event.name)
                    .font(AppFont.medium(16))
                    .foregroundColor(Color(hex: "595959"))

                // 체험 정보 요약
                VStack(spacing: 8) {
                    if let duration = event.duration {
                        Text("체험 시간: \(duration)분")
                            .font(AppFont.regular(13))
                            .foregroundColor(Color(hex: "595959"))
                    }
                    if let location = event.location, !location.isEmpty {
                        Text("장소: \(location)")
                            .font(AppFont.regular(13))
                            .foregroundColor(Color(hex: "595959"))
                    }
                }

                VStack(spacing: 8) {
                    HStack(spacing: 4) {
                        Image(systemName: "info.circle.fill")
                            .foregroundColor(Color(hex: "E67E22"))
                            .font(.system(size: 14))
                        Text("참여 확인")
                            .font(AppFont.semibold(14))
                            .foregroundColor(Color(hex: "E67E22"))
                    }
                    Text("체험 장소에서 이 화면을 보여주세요.")
                        .font(AppFont.regular(12))
                        .foregroundColor(Color(hex: "8F8F8F"))
                        .multilineTextAlignment(.center)
                }
                .padding(16)
                .frame(maxWidth: .infinity)
                .background(
                    RoundedRectangle(cornerRadius: 12)
                        .fill(Color(hex: "FFF8F0"))
                )
                .padding(.horizontal, 20)
            }

            Spacer()

            Button(action: { onBackTapped?() }) {
                Text("돌아가기")
                    .font(AppFont.semibold(16))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(Color(hex: "E67E22"))
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

    private func guideStep(number: Int, text: String) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Text("\(number)")
                .font(AppFont.bold(12))
                .foregroundColor(.white)
                .frame(width: 22, height: 22)
                .background(Circle().fill(Color(hex: "E67E22")))
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

    private func formattedPrice(_ price: Int) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        return formatter.string(from: NSNumber(value: price)) ?? "\(price)"
    }
}
