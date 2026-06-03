//
//  GIFImageView.swift
//  AlphaCityStampTour
//

import SwiftUI
import UIKit
import ImageIO

struct GIFImageView: UIViewRepresentable {
    let name: String

    func makeUIView(context: Context) -> UIImageView {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.clipsToBounds = true
        if let image = loadGIF(named: name) {
            imageView.image = image
        }
        return imageView
    }

    func updateUIView(_ uiView: UIImageView, context: Context) {}

    // SwiftUI가 제안한 크기(.frame)를 따르도록 함.
    // 없으면 UIImageView가 GIF 원본 크기(intrinsic size)로 커져 화면을 꽉 채움.
    func sizeThatFits(_ proposal: ProposedViewSize, uiView: UIImageView, context: Context) -> CGSize? {
        proposal.replacingUnspecifiedDimensions()
    }

    private func loadGIF(named name: String) -> UIImage? {
        guard let url = Bundle.main.url(forResource: name, withExtension: "gif"),
              let data = try? Data(contentsOf: url)
        else { return nil }

        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else { return nil }

        let count = CGImageSourceGetCount(source)
        var images: [UIImage] = []
        var duration: Double = 0

        for i in 0..<count {
            guard let cgImage = CGImageSourceCreateImageAtIndex(source, i, nil) else { continue }
            images.append(UIImage(cgImage: cgImage))

            let props = CGImageSourceCopyPropertiesAtIndex(source, i, nil) as? [String: Any]
            let gif = props?[kCGImagePropertyGIFDictionary as String] as? [String: Any]
            let delay = (gif?[kCGImagePropertyGIFUnclampedDelayTime as String] as? Double)
                ?? (gif?[kCGImagePropertyGIFDelayTime as String] as? Double)
                ?? 0.1
            duration += delay
        }

        return images.count > 1
            ? UIImage.animatedImage(with: images, duration: duration)
            : images.first
    }
}
