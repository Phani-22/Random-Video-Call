package com.phani.helloworld.models

import android.webkit.JavascriptInterface
import com.phani.helloworld.activities.CallActivity

public class InterfaceKt(var callActivity: CallActivity) {

    @JavascriptInterface
    fun onPeerConnected() {
        callActivity.onPeerConnected()
    }
}