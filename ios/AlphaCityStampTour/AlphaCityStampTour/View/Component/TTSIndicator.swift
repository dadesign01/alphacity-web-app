//
//  TTSIndicator.swift
//  AlphaCityStampTour
//

import SwiftUI

struct TTSIndicator: View {
    let isSpeaking: Bool
    @State private var animateBar1 = false
    @State private var animateBar2 = false
    @State private var animateBar3 = false

    var body: some View {
        if isSpeaking {
            HStack(spacing: 2.5) {
                SoundBar(height: animateBar1 ? 14 : 5)
                SoundBar(height: animateBar2 ? 16 : 4)
                SoundBar(height: animateBar3 ? 12 : 6)
            }
            .frame(width: 22, height: 22)
            .onAppear {
                withAnimation(.easeInOut(duration: 0.4).repeatForever(autoreverses: true)) {
                    animateBar1 = true
                }
                withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true).delay(0.1)) {
                    animateBar2 = true
                }
                withAnimation(.easeInOut(duration: 0.35).repeatForever(autoreverses: true).delay(0.2)) {
                    animateBar3 = true
                }
            }
            .onDisappear {
                animateBar1 = false
                animateBar2 = false
                animateBar3 = false
            }
        } else {
            Image(systemName: "speaker.wave.2.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 16, height: 16)
                .foregroundColor(AppColor.primary)
        }
    }
}

private struct SoundBar: View {
    let height: CGFloat

    var body: some View {
        RoundedRectangle(cornerRadius: 2)
            .fill(AppColor.primary)
            .frame(width: 4, height: height)
    }
}
