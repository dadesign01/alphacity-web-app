package com.alphacity.stamptour.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alphacity.stamptour.network.ApiService
import com.alphacity.stamptour.ui.screen.LoginScreen
import com.alphacity.stamptour.ui.screen.MainScreen
import com.alphacity.stamptour.ui.screen.PhoneLogin
import com.alphacity.stamptour.ui.screen.QrStampScreen
import com.alphacity.stamptour.ui.screen.SplashScreen
import com.alphacity.stamptour.ui.screen.StampEarnedScreen
import com.alphacity.stamptour.ui.screen.StampNotEarnedScreen
import com.alphacity.stamptour.ui.screen.TermsScreen
import com.alphacity.stamptour.web.WebSocialLogin
import com.alphacity.stamptour.web.WebTokenManager
import web.QrTarget

object Routes {
    const val LOGIN = "login"
    const val PHONE_LOGIN = "phone_login"
    const val HOME = "home"
    const val QR_STAMP = "qr_stamp"
    const val STAMP_EARNED = "stamp_earned"
    const val STAMP_NOT_EARNED = "stamp_not_earned"
    const val TERMS = "terms"
}

@Composable
fun AppNavigation(
    deepLinkProgramId: Int? = null,
    qrTarget: QrTarget? = null,
) {
    val navController = rememberNavController()

    var isLoggedIn by remember {
        mutableStateOf<Boolean?>(null)
    }

    var showSplash by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        if (!WebTokenManager.isLoggedIn()) {
            isLoggedIn = false
            showSplash = true
            return@LaunchedEffect
        }

        try {
            val response = ApiService.getUserProfile()

            if (response.success && response.data != null) {
                isLoggedIn = true
                showSplash = false

                println("=== AUTH CHECK ===")
                println("accessToken = EXISTS")
                println("GET /users/me = SUCCESS")
                println("userId = ${response.data.id}")
                println("==================")
            } else {
                WebTokenManager.clear()
                isLoggedIn = false
                showSplash = true

                println("=== AUTH CHECK ===")
                println("accessToken = EXISTS")
                println("GET /users/me = FAILED")
                println("token cleared")
                println("==================")
            }
        } catch (e: Exception) {
            isLoggedIn = true
            showSplash = false

            println("=== AUTH CHECK ===")
            println("GET /users/me ERROR")
            println("message = ${e.message}")
            println("==================")
        }
    }

    val startDestination = when {
        qrTarget != null -> Routes.QR_STAMP
        isLoggedIn == true -> Routes.HOME
        isLoggedIn == false -> Routes.LOGIN
        else -> Routes.LOGIN
    }

    println("=== APP NAVIGATION ===")
    println("qrTarget = $qrTarget")
    println("isLoggedIn = $isLoggedIn")
    println("startDestination = $startDestination")
    println("======================")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoggedIn != null || qrTarget != null) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
            ) {

                // =====================================================
                // 로그인
                // =====================================================

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onPhoneLoginClick = {
                            navController.navigate(Routes.PHONE_LOGIN)
                        },

                        onKakaoLoginClick = {
                            WebSocialLogin.loginWithKakao()
                        },

                        onNaverLoginClick = {
                            WebSocialLogin.loginWithNaver()
                        },

                        onGuestClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }

                // =====================================================
                // 휴대폰 로그인
                // =====================================================

                composable(Routes.PHONE_LOGIN) {
                    PhoneLogin(
                        qrTarget = qrTarget,

                        onAuthSuccess = {
                            if (qrTarget != null) {
                                navController.navigate(Routes.QR_STAMP) {
                                    popUpTo(Routes.PHONE_LOGIN) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.PHONE_LOGIN) {
                                        inclusive = true
                                    }
                                }
                            }
                        },

                        onGuestClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.PHONE_LOGIN) {
                                    inclusive = true
                                }
                            }
                        },

                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }

                // =====================================================
                // 홈
                // =====================================================

                composable(Routes.HOME) {
                    MainScreen(
                        deepLinkProgramId = deepLinkProgramId,

                        onLogout = {
                            WebTokenManager.clear()

                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.HOME) {
                                    inclusive = true
                                }
                            }
                        },

                        onNavigateToRegister = {
                            navController.navigate(Routes.PHONE_LOGIN) {
                                popUpTo(Routes.HOME) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }

                // =====================================================
                // QR 스탬프
                // =====================================================

                composable(Routes.QR_STAMP) {
                    QrStampScreen(
                        target = qrTarget,

                        onLoginRequired = {
                            navController.navigate(Routes.PHONE_LOGIN) {
                                popUpTo(Routes.QR_STAMP) {
                                    inclusive = true
                                }
                            }
                        },

                        onAlreadyCollected = {
                            navController.navigate(
                                Routes.STAMP_NOT_EARNED
                            ) {
                                popUpTo(Routes.QR_STAMP) {
                                    inclusive = true
                                }
                            }
                        },

                        onEarned = {
                            navController.navigate(
                                Routes.STAMP_EARNED
                            ) {
                                popUpTo(Routes.QR_STAMP) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }

                // =====================================================
                // 스탬프 적립 완료
                // =====================================================

                composable(Routes.STAMP_EARNED) {
                    StampEarnedScreen(
                        onEventClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.STAMP_EARNED) {
                                    inclusive = true
                                }
                            }
                        },

                        onStampClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.STAMP_EARNED) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }

                // =====================================================
                // 스탬프 미적립
                // =====================================================

                composable(Routes.STAMP_NOT_EARNED) {
                    StampNotEarnedScreen(
                        onEventClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.STAMP_NOT_EARNED) {
                                    inclusive = true
                                }
                            }
                        },

                        onStampClick = {
                            // TODO: 적립한 스탬프 화면 연결
                        },
                    )
                }

                // =====================================================
                // 약관
                // =====================================================

                composable(Routes.TERMS) {
                    TermsScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                    )
                }
            }
        }

        // ============================================================
        // 스플래시
        // ============================================================

        if (
            showSplash &&
            deepLinkProgramId == null &&
            qrTarget == null
        ) {
            SplashScreen(
                onStartClick = {
                    showSplash = false
                },
            )
        }
    }
}