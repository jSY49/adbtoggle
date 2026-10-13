package com.jsy.adbtoggle

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** 화면 갱신만 담당한다. 탭 처리는 비공개 [WidgetToggleReceiver] 가 한다. */
class ToggleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        update(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun update(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_toggle)

            val granted = DebugToggle.hasPermission(context)
            val on = granted && DebugToggle.readState(context).anyOn
            when {
                !granted -> {
                    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_disabled)
                    views.setImageViewResource(R.id.widget_icon, R.drawable.ic_debug_off)
                    views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_need_permission))
                }
                on -> {
                    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_on)
                    views.setImageViewResource(R.id.widget_icon, R.drawable.ic_debug_on)
                    views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_on))
                }
                else -> {
                    views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_off)
                    views.setImageViewResource(R.id.widget_icon, R.drawable.ic_debug_off)
                    views.setTextViewText(R.id.widget_text, context.getString(R.string.widget_off))
                }
            }

            val intent = Intent(context, WidgetToggleReceiver::class.java).setAction(WidgetToggleReceiver.ACTION_TOGGLE)
            val pending = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            manager.updateAppWidget(ids, views)
        }
    }
}
