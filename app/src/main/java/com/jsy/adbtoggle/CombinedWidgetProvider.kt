package com.jsy.adbtoggle

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.jsy.adbtoggle.DebugToggle.Target

/** 큰 네모 안에 개발자 / 유선 / 무선 정사각형 칸이 기차처럼 이어져 있고, 칸마다 따로 토글된다. */
class CombinedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        update(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        private class RowIds(val row: Int, val icon: Int, val text: Int)

        private val rows = mapOf(
            Target.DEV_OPTIONS to RowIds(R.id.row_dev, R.id.icon_dev, R.id.text_dev),
            Target.USB to RowIds(R.id.row_usb, R.id.icon_usb, R.id.text_usb),
            Target.WIRELESS to RowIds(R.id.row_wireless, R.id.icon_wireless, R.id.text_wireless),
        )

        fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_combined)
            val granted = DebugToggle.hasPermission(context)
            val state = if (granted) DebugToggle.readState(context) else null

            for ((target, r) in rows) {
                val on = state?.isOn(target) == true
                val label = UiSync.shortLabel(context, target)
                val background = when {
                    state == null -> R.drawable.widget_bg_disabled
                    on -> R.drawable.widget_bg_on
                    else -> R.drawable.widget_bg_off
                }
                val text = if (state == null) label else "$label\n${context.getString(if (on) R.string.widget_on else R.string.widget_off)}"
                views.setInt(r.row, "setBackgroundResource", background)
                views.setImageViewResource(r.icon, UiSync.iconRes(target, on))
                views.setTextViewText(r.text, text)
                views.setOnClickPendingIntent(r.row, BaseToggleWidgetProvider.togglePendingIntent(context, target))
            }
            manager.updateAppWidget(ids, views)
        }
    }
}
