//
//  StoreDetailSheet.swift
//  AlphaCityStampTour
//

import SwiftUI
import AVFoundation

struct StoreDetailSheet: View {
    let program: ProgramData
    var onDismiss: () -> Void
    @StateObject private var ttsManager = StoreTTSManager()

    private let darkBg = Color(hex: "1B2038")
    private let cardBg = Color(hex: "252B45")

    var body: some View {
        ScrollView(.vertical, showsIndicators: false) {
            VStack(alignment: .leading, spacing: 0) {
                // Category tag + Close button
                HStack {
                    Text("맛집")
                        .font(AppFont.medium(12))
                        .foregroundColor(.white.opacity(0.8))
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(Color.white.opacity(0.15))
                        .clipShape(RoundedRectangle(cornerRadius: 6))

                    Spacer()

                    Button(action: {
                        ttsManager.stop()
                        onDismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(.white)
                    }
                }

                // Store name
                Text(program.name)
                    .font(AppFont.bold(22))
                    .foregroundColor(.white)
                    .padding(.top, 12)

                // Store image
                if let imageUrl = program.imageUrl, !imageUrl.isEmpty {
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
                            placeholderImage
                        }
                    }
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 16)
                } else {
                    placeholderImage
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .padding(.top, 16)
                }

                // Description card with TTS
                if let description = program.description, !description.isEmpty {
                    HStack(alignment: .top, spacing: 12) {
                        Text(description)
                            .font(AppFont.regular(14))
                            .foregroundColor(.white.opacity(0.85))
                            .lineSpacing(8)

                        Spacer()

                        Button(action: {
                            if ttsManager.isSpeaking {
                                ttsManager.stop()
                            } else {
                                ttsManager.speak(description)
                            }
                        }) {
                            Image(systemName: ttsManager.isSpeaking ? "stop.fill" : "speaker.wave.2.fill")
                                .font(.system(size: 18))
                                .foregroundColor(.white.opacity(0.7))
                        }
                    }
                    .padding(16)
                    .background(cardBg)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 16)
                }

                // No mission message
                Text("미션이 등록되지 않은 상점입니다")
                    .font(AppFont.medium(14))
                    .foregroundColor(.white.opacity(0.5))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.white.opacity(0.08))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.top, 20)
            }
            .padding(20)
        }
        .background(darkBg)
        .onDisappear {
            ttsManager.stop()
        }
    }

    private var placeholderImage: some View {
        ZStack {
            cardBg
            Text(String(program.name.prefix(1)))
                .font(AppFont.bold(40))
                .foregroundColor(.white.opacity(0.3))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 200)
    }
}

// MARK: - TTS Manager

private class StoreTTSManager: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    private let synthesizer = AVSpeechSynthesizer()
    @Published var isSpeaking = false

    override init() {
        super.init()
        synthesizer.delegate = self
    }

    func speak(_ text: String) {
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = AVSpeechSynthesisVoice(language: "ko-KR")
        utterance.rate = 0.5
        synthesizer.speak(utterance)
        isSpeaking = true
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        DispatchQueue.main.async {
            self.isSpeaking = false
        }
    }
}
