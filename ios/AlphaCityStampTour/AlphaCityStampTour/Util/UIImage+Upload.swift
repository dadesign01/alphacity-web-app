//
//  UIImage+Upload.swift
//  AlphaCityStampTour
//

import UIKit

extension UIImage {
    /// 업로드용 JPEG 데이터 (긴 변 제한 + 압축, 서버 5MB 제한 대응)
    func jpegDataForUpload(maxDimension: CGFloat = 1600, quality: CGFloat = 0.85) -> Data? {
        let pixelSize = CGSize(width: size.width * scale, height: size.height * scale)
        let ratio = min(1, maxDimension / max(pixelSize.width, pixelSize.height))
        let target = CGSize(width: pixelSize.width * ratio, height: pixelSize.height * ratio)

        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1
        let resized = UIGraphicsImageRenderer(size: target, format: format).image { _ in
            draw(in: CGRect(origin: .zero, size: target))
        }
        return resized.jpegData(compressionQuality: quality)
    }
}
