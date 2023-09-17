package com.chrome.chattingapp

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "bdf128af394154b78b37ac84926de2c9")
    }
}