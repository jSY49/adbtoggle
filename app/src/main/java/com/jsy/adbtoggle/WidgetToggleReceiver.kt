package com.jsy.adbtoggle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jsy.adbtoggle.DebugToggle.Target

/**
 * 위젯 탭을 받는 receiver. exported=false 라서 다른 앱이 토글 브로드캐스트를 보낼 수 없다.
 */
class WidgetToggleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return
        val target = Target.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_TARGET) } ?: return
        UiSync.toggleWithFeedback(context, target) // 위젯 갱신 포함
        UiSync.refreshTile(context)
    }

    companion object {
        const val ACTION_TOGGLE = "com.jsy.adbtoggle.action.TOGGLE"
        const val EXTRA_TARGET = "target"
    }
}
