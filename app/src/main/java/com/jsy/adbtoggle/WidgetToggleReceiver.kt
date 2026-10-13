package com.jsy.adbtoggle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 위젯 탭을 받는 receiver. exported=false 라서 다른 앱이 토글 브로드캐스트를 보낼 수 없다.
 */
class WidgetToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return
        UiSync.toggleWithFeedback(context) // 위젯 갱신 포함
        UiSync.refreshTile(context)
    }

    companion object {
        const val ACTION_TOGGLE = "com.jsy.adbtoggle.action.TOGGLE"
    }
}
