package com.jsy.adbtoggle

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jsy.adbtoggle.DebugToggle.Target

/** 화면 갱신만 담당한다. 탭 처리는 비공개 [WidgetToggleReceiver] 가 한다. */
abstract class BaseToggleWidgetProvider(private val target: Target) : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        update(context, appWidgetManager, appWidgetIds, target)
    }

    companion object {
        fun update(context: Context, manager: AppWidgetManager, ids: IntArray, target: Target) {
            val views = RemoteViews(context.packageName, R.layout.widget_toggle)

            val granted = DebugToggle.hasPermission(context)
            val on = granted && DebugToggle.readState(context).isOn(target)
            val label = UiSync.label(context, target)
            val (background, stateText) = when {
                !granted -> R.drawable.widget_bg_disabled to context.getString(R.string.widget_need_permission)
                on -> R.drawable.widget_bg_on to context.getString(R.string.widget_on)
                else -> R.drawable.widget_bg_off to context.getString(R.string.widget_off)
            }
            views.setInt(R.id.widget_root, "setBackgroundResource", background)
            views.setImageViewResource(R.id.widget_icon, UiSync.iconRes(target, on))
            views.setTextViewText(R.id.widget_text, "$label\n$stateText")

            views.setOnClickPendingIntent(R.id.widget_root, togglePendingIntent(context, target))

            manager.updateAppWidget(ids, views)
        }

        fun togglePendingIntent(context: Context, target: Target): PendingIntent {
            val intent = Intent(context, WidgetToggleReceiver::class.java)
                .setAction(WidgetToggleReceiver.ACTION_TOGGLE)
                .putExtra(WidgetToggleReceiver.EXTRA_TARGET, target.name)
            // extra 만 다른 intent 는 같은 PendingIntent 로 취급되므로 requestCode 를 항목별로 다르게 준다.
            return PendingIntent.getBroadcast(
                context, target.ordinal, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }
    }
}

class DevOptionsWidgetProvider : BaseToggleWidgetProvider(Target.DEV_OPTIONS)
class UsbWidgetProvider : BaseToggleWidgetProvider(Target.USB)
class WirelessWidgetProvider : BaseToggleWidgetProvider(Target.WIRELESS)
