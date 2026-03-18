//
//  ContentView.swift
//  AlphaCityStampTour
//
//  Created by 윤종서 on 2/25/26.
//

import SwiftUI

enum AppScreen {
    case login
    case register
    case forgotPassword
    case home
}

struct ContentView: View {
    @StateObject private var loginViewModel = LoginViewModel()
    @State private var currentScreen: AppScreen = TokenManager.shared.isLoggedIn ? .home : .login
    @State private var deepLinkProgramId: Int? = nil
    @State private var showSplash = !TokenManager.shared.isLoggedIn

    var body: some View {
        ZStack {
            NavigationStack {
                switch currentScreen {
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
                        },
                        onNavigateToRegister: {
                            currentScreen = .login
                        }
                    )
                }
            }
            .onReceive(NotificationCenter.default.publisher(for: .deepLinkProgram)) { notification in
                if let programId = notification.userInfo?["programId"] as? Int {
                    showSplash = false
                    currentScreen = .home
                    deepLinkProgramId = programId
                }
            }

            if showSplash {
                SplashView(onStartTapped: {
                    showSplash = false
                })
            }
        }
    }
}

#Preview {
    ContentView()
}
