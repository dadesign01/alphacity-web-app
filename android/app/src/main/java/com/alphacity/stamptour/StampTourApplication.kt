package com.alphacity.stamptour

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StampTourApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오 SDK 초기화 - 카카오 디벨로퍼스에서 발급받은 네이티브 앱 키
        KakaoSdk.init(this, "d47f7264eacf91989b2045ee206de4b5")

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, "d47f7264eacf91989b2045ee206de4b5")

        // 네이버 SDK 초기화
        NaverIdLoginSDK.initialize(
            this,
            "0YLBJJdHMRJ44evdP6rN",
            "JOFCB4eY0x",
            "수성알파시티 스탬프 투어",
        )
    }
}
