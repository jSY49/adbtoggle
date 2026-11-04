package com.jsy.adbtoggle

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import android.widget.Toast
import com.jsy.adbtoggle.DebugToggle.Target

/** 타일·위젯·화면이 같은 상태를 보도록 맞추고, 실패 시 사용자에게 알리는 도우미. */
object UiSync {

    private val tileServices = mapOf(
        Target.DEV_OPTIONS to DevOptionsTileService::class.java,
        Target.USB to UsbTileService::class.java,
        Target.WIRELESS to WirelessTileService::class.java,
    )

    private val widgetProviders = mapOf(
        Target.DEV_OPTIONS to DevOptionsWidgetProvider::class.java,
        Target.USB to UsbWidgetProvider::class.java,
        Target.WIRELESS to WirelessWidgetProvider::class.java,
    )

    /** 토글하고, 실패하면 토스트로 알리고, 위젯을 갱신한다. 타일/화면 갱신은 호출한 쪽 몫. */
    fun toggleWithFeedback(context: Context, target: Target): DebugToggle.Result {
        val app = context.applicationContext
        val result = DebugToggle.toggle(app, target)
        message(app, result)?.let { Toast.makeText(app, it, Toast.LENGTH_LONG).show() }
        refreshWidgets(app)
        return result
    }

    fun refreshAll(context: Context) {
        refreshWidgets(context)
        refreshTile(context)
    }

    /** 디버깅을 켜면 개발자 모드도 같이 바뀔 수 있어서 항상 세 위젯을 모두 갱신한다. */
    fun refreshWidgets(context: Context) {
        val app = context.applicationContext
        val manager = AppWidgetManager.getInstance(app)
        for ((target, provider) in widgetProviders) {
            val ids = manager.getAppWidgetIds(ComponentName(app, provider))
            if (ids.isNotEmpty()) BaseToggleWidgetProvider.update(app, manager, ids, target)
        }
        val combinedIds = manager.getAppWidgetIds(ComponentName(app, CombinedWidgetProvider::class.java))
        if (combinedIds.isNotEmpty()) CombinedWidgetProvider.update(app, manager, combinedIds)
    }

    /** 타일이 화면에 없어도 시스템이 onStartListening 을 불러 다시 그리게 한다. */
    fun refreshTile(context: Context) {
        val app = context.applicationContext
        for (service in tileServices.values) {
            TileService.requestListeningState(app, ComponentName(app, service))
        }
    }

    fun label(context: Context, target: Target): String = context.getString(
        when (target) {
            Target.DEV_OPTIONS -> R.string.label_dev
            Target.USB -> R.string.label_usb
            Target.WIRELESS -> R.string.label_wireless
        },
    )

    fun shortLabel(context: Context, target: Target): String = context.getString(
        when (target) {
            Target.DEV_OPTIONS -> R.string.short_dev
            Target.USB -> R.string.short_usb
            Target.WIRELESS -> R.string.short_wireless
        },
    )

    fun iconRes(target: Target, on: Boolean): Int = when (target) {
        Target.DEV_OPTIONS -> if (on) R.drawable.ic_dev_on else R.drawable.ic_dev_off
        Target.USB -> if (on) R.drawable.ic_usb_on else R.drawable.ic_usb_off
        Target.WIRELESS -> if (on) R.drawable.ic_wifi_on else R.drawable.ic_wifi_off
    }

    /** 성공이면 null. */
    fun message(context: Context, result: DebugToggle.Result): String? = when (result) {
        is DebugToggle.Result.Success -> null
        DebugToggle.Result.NoPermission -> context.getString(R.string.toast_no_permission)
        is DebugToggle.Result.NotApplied -> context.getString(
            R.string.toast_not_applied,
            onOff(context, result.state.devOptionsOn),
            onOff(context, result.state.usbOn),
            onOff(context, result.state.wirelessOn),
        )
    }

    fun onOff(context: Context, on: Boolean): String =
        context.getString(if (on) R.string.state_on else R.string.state_off)
}
