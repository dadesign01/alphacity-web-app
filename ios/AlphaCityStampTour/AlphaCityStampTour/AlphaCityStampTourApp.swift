//
//  AlphaCityStampTourApp.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI
import KakaoSDKCommon
import KakaoSDKAuth
import NidThirdPartyLogin
import KakaoMapsSDK

extension Notification.Name {
    static let deepLinkProgram = Notification.Name("deepLinkProgram")
}

@main
struct AlphaCityStampTourApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        // Pretendard 폰트 등록
        FontRegistration.registerFonts()

        // 카카오 SDK 초기화
        KakaoSDK.initSDK(appKey: "d47f7264eacf91989b2045ee206de4b5")

        // 카카오맵 SDK 초기화
        SDKInitializer.InitSDK(appKey: "d47f7264eacf91989b2045ee206de4b5")

        // 네이버 SDK 초기화
        NidOAuth.shared.initialize(
            appName: "수성알파시티 스탬프 투어",
            clientId: "0YLBJJdHMRJ44evdP6rN",
            clientSecret: "JOFCB4eY0x",
            urlScheme: "com.alphacity.stamptour.naverlogin"
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // 카카오 로그인 URL 콜백
                    if AuthApi.isKakaoTalkLoginUrl(url) {
                        _ = AuthController.handleOpenUrl(url: url)
                        return
                    }

                    // 딥링크: alphacity://program/{id}
                    if url.scheme == "alphacity", url.host == "program",
                       let idStr = url.pathComponents.last, let programId = Int(idStr) {
                        NotificationCenter.default.post(
                            name: .deepLinkProgram,
                            object: nil,
                            userInfo: ["programId": programId]
                        )
                    }
                }
        }
    }
}
