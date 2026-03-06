//
//  ContentView.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI

enum AppScreen {
    case splash
    case login
    case register
    case forgotPassword
    case home
}

struct ContentView: View {
    @StateObject private var loginViewModel = LoginViewModel()
    @State private var currentScreen: AppScreen = .splash
    @State private var deepLinkProgramId: Int? = nil

    var body: some View {
        NavigationStack {
            switch currentScreen {
            case .splash:
                SplashView(onStartTapped: {
                    currentScreen = .login
                })

            case .login:
                LoginView(
                    viewModel: loginViewModel,
                    onLoginSuccess: {
                        currentScreen = .home
                    },
                    onGuestTapped: {
                        currentScreen = .home
                    },
                    onRegisterTapped: {
                        currentScreen = .register
                    },
                    onForgotPasswordTapped: {
                        currentScreen = .forgotPassword
                    }
                )

            case .register:
                RegisterView(
                    onRegisterSuccess: {
                        currentScreen = .home
                    },
                    onBackTapped: {
                        currentScreen = .login
                    }
                )

            case .forgotPassword:
                ForgotPasswordView(
                    onBackTapped: {
                        currentScreen = .login
                    }
                )

            case .home:
                MainTabView(
                    deepLinkProgramId: $deepLinkProgramId,
                    onLogout: {
                        currentScreen = .login
                    }
                )
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: .deepLinkProgram)) { notification in
            if let programId = notification.userInfo?["programId"] as? Int {
                currentScreen = .home
                deepLinkProgramId = programId
            }
        }
    }
}

#Preview {
    ContentView()
}
