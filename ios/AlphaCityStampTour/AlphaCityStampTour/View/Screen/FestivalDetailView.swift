//
//  FestivalDetailView.swift
//  AlphaCityStampTour
//

import SwiftUI

@MainActor
final class FestivalDetailViewModel: ObservableObject {
    @Published var detail: FestivalDetailData? = nil
    @Published var isLoading: Bool = false

    private let repository = HomeRepository.shared
    private let selection = FestivalSelection.shared

    func load(_ festivalId: Int) {
        guard !isLoading else { return }
        isLoading = true
        selection.select(festivalId)
        Task {
            do {
                detail = try await repository.fetchFestivalDetail(festivalId)
            } catch {
                print("[FestivalDetailVM] 로드 실패: \(error)")
            }
            isLoading = false
        }
    }
}

struct FestivalDetailView: View {
    let festival: FestivalData
    var onBackTapped: () -> Void = {}
    var onSeeAllPrograms: () -> Void = {}
    var onProgramTapped: (ProgramData) -> Void = { _ in }
    var onNavigateToMap: (Double, Double) -> Void = { _, _ in }

    @StateObject private var viewModel = FestivalDetailViewModel()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Top bar
                HStack(spacing: 8) {
                    Button(action: onBackTapped) {
                        Image("IconBackArrow")
                            .renderingMode(.original)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 13, height: 26)
                    }
                    Text("축제 상세")
                        .font(AppFont.semibold(18))
                        .foregroundColor(Color(hex: "121212"))
                    Spacer()
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)

                // Banner
                let bannerUrl = festival.bannerUrl ?? festival.imageUrl
                if let url = bannerUrl, !url.isEmpty {
                    let fullUrl = url.hasPrefix("http") ? url : "\(APIClient.serverURL)\(url)"
                    AsyncImage(url: URL(string: fullUrl)) { phase in
                        switch phase {
                        case .success(let img): img.resizable().scaledToFill()
                        default: Color(hex: "EDF7FF")
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 220)
                    .clipped()
                } else {
                    Image("ProgramImg1")
                        .resizable()
                        .scaledToFill()
                        .frame(maxWidth: .infinity)
                        .frame(height: 220)
                        .clipped()
                }

                VStack(alignment: .leading, spacing: 0) {
                    Text(festival.name)
                        .font(AppFont.bold(22))
                        .foregroundColor(Color(hex: "121212"))
                        .padding(.top, 20)

                    Text("\(String(festival.startDate.prefix(10))) ~ \(String(festival.endDate.prefix(10)))")
                        .font(AppFont.medium(13))
                        .foregroundColor(Color(hex: "3D608D"))
                        .padding(.top, 8)

                    if let addr = festival.address, !addr.isEmpty {
                        Text(addr)
                            .font(AppFont.regular(13))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .padding(.top, 4)
                    }

                    if let desc = festival.description, !desc.isEmpty {
                        Text(desc)
                            .font(AppFont.regular(14))
                            .foregroundColor(Color(hex: "121212"))
                            .lineSpacing(6)
                            .padding(.top, 16)
                    }

                    HStack(spacing: 8) {
                        if let lat = festival.latitude, let lng = festival.longitude {
                            Button(action: { onNavigateToMap(lat, lng) }) {
                                Text("지도보기")
                                    .font(AppFont.semibold(15))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 48)
                                    .background(Color(hex: "6366F1"))
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                            }
                        }
                        Button(action: onSeeAllPrograms) {
                            Text("프로그램 보러가기")
                                .font(AppFont.semibold(15))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 48)
                                .background(AppColor.primary)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                        }
                    }
                    .padding(.top, 20)

                    if let programs = viewModel.detail?.programs, !programs.isEmpty {
                        Text("이 축제의 프로그램")
                            .font(AppFont.semibold(16))
                            .foregroundColor(Color(hex: "121212"))
                            .padding(.top, 24)

                        VStack(spacing: 8) {
                            ForEach(programs, id: \.id) { p in
                                Button(action: {
                                    onProgramTapped(ProgramData(
                                        id: p.id,
                                        festivalId: festival.id,
                                        name: p.name,
                                        description: nil,
                                        category: p.category,
                                        subcategory: nil,
                                        hasCoupon: nil,
                                        imageUrl: p.imageUrl,
                                        operatingHours: nil,
                                        location: p.location,
                                        latitude: p.latitude,
                                        longitude: p.longitude,
                                        phone: nil,
                                        speaker: nil,
                                        ownerName: nil,
                                        storeCode: nil,
                                        operatingDays: nil,
                                        startDate: p.startDate,
                                        endDate: p.endDate,
                                        status: p.status ?? "in_progress",
                                        events: nil,
                                        storeCoupons: nil,
                                        stores: nil
                                    ))
                                }) {
                                    HStack(spacing: 12) {
                                        if let url = p.imageUrl, !url.isEmpty {
                                            let fullUrl = url.hasPrefix("http") ? url : "\(APIClient.serverURL)\(url)"
                                            AsyncImage(url: URL(string: fullUrl)) { phase in
                                                switch phase {
                                                case .success(let img): img.resizable().scaledToFill()
                                                default: Color(hex: "EDF7FF")
                                                }
                                            }
                                            .frame(width: 72, height: 72)
                                            .clipShape(RoundedRectangle(cornerRadius: 10))
                                        } else {
                                            Image("ProgramImg1")
                                                .resizable()
                                                .scaledToFill()
                                                .frame(width: 72, height: 72)
                                                .clipShape(RoundedRectangle(cornerRadius: 10))
                                        }
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(p.name)
                                                .font(AppFont.semibold(14))
                                                .foregroundColor(Color(hex: "121212"))
                                            let cat: String = {
                                                switch p.category {
                                                case "food": return "맛집"
                                                case "exhibition": return "전시"
                                                case "seminar": return "세미나"
                                                case "experience": return "체험"
                                                default: return p.category ?? ""
                                                }
                                            }()
                                            if !cat.isEmpty {
                                                Text(cat)
                                                    .font(AppFont.medium(12))
                                                    .foregroundColor(Color(hex: "8F8F8F"))
                                            }
                                        }
                                        Spacer()
                                    }
                                    .padding(.vertical, 6)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.top, 12)
                    } else if viewModel.isLoading {
                        Text("불러오는 중...")
                            .font(AppFont.regular(13))
                            .foregroundColor(Color(hex: "8F8F8F"))
                            .padding(.top, 20)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 32)
            }
        }
        .background(Color.white)
        .onAppear {
            viewModel.load(festival.id)
        }
    }
}
