package com.jsy.adbtoggle

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import android.widget.Toast

/** 타일·위젯·화면이 같은 상태를 보도록 맞추고, 실패 시 사용자에게 알리는 도우미. */
object UiSync {

    /** 토글하고, 실패하면 토스트로 알리고, 위젯을 갱신한다. 타일/화면 갱신은 호출한 쪽 몫. */
    fun toggleWithFeedback(context: Context): DebugToggle.Result {
        val app = context.applicationContext
        val result = DebugToggle.toggle(app)
        message(app, result)?.let { Toast.makeText(app, it, Toast.LENGTH_LONG).show() }
        refreshWidgets(app)
        return result
    }

    fun refreshAll(context: Context) {
        refreshWidgets(context)
        refreshTile(context)
    }

    fun refreshWidgets(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        val ids = manager.getAppWidgetIds(ComponentName(app, ToggleWidgetProvider::class.java))
        if (ids.isNotEmpty()) ToggleWidgetProvider.update(app, manager, ids)
    }

    /** 타일이 화면에 없어도 시스템이 onStartListening 을 불러 다시 그리게 한다. */
    fun refreshTile(context: Context) {
        val app = context.applicationContext
        TileService.requestListeningState(app, ComponentName(app, ToggleTileService::class.java))
    }

    /** 성공이면 null. */
    fun message(context: Context, result: DebugToggle.Result): String? = when (result) {
        is DebugToggle.Result.Success -> null
        DebugToggle.Result.NoPermission -> context.getString(R.string.toast_no_permission)
        is DebugToggle.Result.NotApplied -> context.getString(
            R.string.toast_not_applied,
            onOff(context, result.state.usbOn),
            onOff(context, result.state.wirelessOn),
        )
    }

    fun onOff(context: Context, on: Boolean): String =
        context.getString(if (on) R.string.state_on else R.string.state_off)
}
