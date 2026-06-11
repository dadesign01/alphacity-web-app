//
//  StoreRegisterViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class StoreRegisterViewModel: ObservableObject {
    @Published var isSubmitting = false
    @Published var submitResult: SubmitResult? = nil
    @Published var isUploadingImage = false
    @Published var uploadedImageUrl: String? = nil

    private let repository = StoreRepository.shared
    private let client = APIClient.shared

    enum SubmitResult {
        case success
        case error(String)
    }

    // MARK: - 대표 이미지 업로드

    func uploadImage(_ data: Data) {
        guard !isUploadingImage else { return }
        isUploadingImage = true

        Task {
            do {
                uploadedImageUrl = try await client.uploadImage(data, filename: "store_\(Int(Date().timeIntervalSince1970)).jpg")
            } catch {
                print("[StoreRegisterVM] 이미지 업로드 실패: \(error)")
                uploadedImageUrl = nil
            }
            isUploadingImage = false
        }
    }

    func registerStore(
        name: String,
        category: String,
        ownerName: String,
        phone: String,
        address: String,
        addressDetail: String,
        description: String,
        storeCode: String,
        operatingDays: String,
        openTime: String,
        closeTime: String
    ) {
        guard !isSubmitting else { return }
        isSubmitting = true

        Task {
            let request = StoreRegisterRequest(
                name: name,
                category: category,
                ownerName: ownerName,
                phone: phone,
                address: address.isEmpty ? nil : address,
                addressDetail: addressDetail.isEmpty ? nil : addressDetail,
                description: description.isEmpty ? nil : description,
                imageUrl: uploadedImageUrl,
                storeCode: storeCode.isEmpty ? nil : storeCode,
                operatingDays: operatingDays.isEmpty ? nil : operatingDays,
                openTime: openTime.isEmpty ? nil : openTime,
                closeTime: closeTime.isEmpty ? nil : closeTime
            )

            do {
                _ = try await repository.registerStore(request)
                submitResult = .success
            } catch {
                print("[StoreRegisterVM] 등록 실패: \(error)")
                submitResult = .error(error.localizedDescription)
            }

            isSubmitting = false
        }
    }

    func clearResult() {
        submitResult = nil
    }
}
