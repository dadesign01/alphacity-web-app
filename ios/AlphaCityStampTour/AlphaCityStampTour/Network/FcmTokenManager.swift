//
//  FcmTokenManager.swift
//  AlphaCityStampTour
//

import Foundation

final class FcmTokenManager {
    static let shared = FcmTokenManager()
    private init() {}

    private let fcmTokenKey = "com.alphacity.fcmToken"

    var currentToken: String? {
        get { UserDefaults.standard.string(forKey: fcmTokenKey) }
        set { UserDefaults.standard.set(newValue, forKey: fcmTokenKey) }
    }

    func registerTokenToServer(fcmToken: String) {
        guard let accessToken = TokenManager.shared.accessToken else { return }
        currentToken = fcmToken

        let url = URL(string: "\(APIClient.shared.baseURLString)/users/fcm-token")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        request.httpBody = try? JSONEncoder().encode(["fcmToken": fcmToken])

        URLSession.shared.dataTask(with: request) { _, response, error in
            if let error = error {
                print("[FCM] Token registration failed: \(error)")
            } else {
                let statusCode = (response as? HTTPURLResponse)?.statusCode ?? 0
                print("[FCM] Token registered to server: \(statusCode)")
                print("[FCM] Token: \(fcmToken)")
            }
        }.resume()
    }

    func clearTokenFromServer() {
        guard let accessToken = TokenManager.shared.accessToken else { return }

        let url = URL(string: "\(APIClient.shared.baseURLString)/users/fcm-token")!
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")

        URLSession.shared.dataTask(with: request) { _, _, _ in }.resume()
        currentToken = nil
    }
}
