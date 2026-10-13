package com.jsy.adbtoggle

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * USB 디버깅(adb_enabled)과 무선 디버깅(adb_wifi_enabled)을 함께 읽고 쓰는 유일한 곳.
 * 이 앱에서 Settings.Global.putInt 는 이 파일의 [writeBoth] 에서만 호출한다.
 */
object DebugToggle {

    private const val KEY_USB = "adb_enabled"
    private const val KEY_WIRELESS = "adb_wifi_enabled"
    private const val PERMISSION = "android.permission.WRITE_SECURE_SETTINGS"

    data class State(val usbOn: Boolean, val wirelessOn: Boolean) {
        /** 둘 중 하나라도 켜져 있으면 "켜짐"으로 본다. */
        val anyOn: Boolean get() = usbOn || wirelessOn
    }

    sealed interface Result {
        /** 요청한 대로 바뀐 것을 다시 읽어서 확인했다. */
        data class Success(val state: State) : Result

        /** WRITE_SECURE_SETTINGS 권한이 없다. */
        data object NoPermission : Result

        /** 쓰기는 했지만 다시 읽어 보니 요청한 값과 다르다. */
        data class NotApplied(val state: State) : Result
    }

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun readState(context: Context): State {
        val resolver = context.contentResolver
        return State(
            usbOn = Settings.Global.getInt(resolver, KEY_USB, 0) == 1,
            wirelessOn = Settings.Global.getInt(resolver, KEY_WIRELESS, 0) == 1,
        )
    }

    /** 현재 상태가 켜짐이면 둘 다 끄고, 꺼짐이면 둘 다 켠다. */
    fun toggle(context: Context): Result = setEnabled(context, enable = !readState(context).anyOn)

    fun setEnabled(context: Context, enable: Boolean): Result {
        if (!hasPermission(context)) return Result.NoPermission
        try {
            writeBoth(context, enable)
        } catch (e: SecurityException) {
            return Result.NoPermission
        }
        // 기기마다 값이 안 먹힐 수 있으므로 반드시 다시 읽어서 확인한다.
        val after = readState(context)
        val applied = after.usbOn == enable && after.wirelessOn == enable
        return if (applied) Result.Success(after) else Result.NotApplied(after)
    }

    /** 켤 때는 USB → 무선, 끌 때는 무선 → USB 순서로 쓴다. */
    private fun writeBoth(context: Context, enable: Boolean) {
        val resolver = context.contentResolver
        val value = if (enable) 1 else 0
        if (enable) {
            Settings.Global.putInt(resolver, KEY_USB, value)
            Settings.Global.putInt(resolver, KEY_WIRELESS, value)
        } else {
            Settings.Global.putInt(resolver, KEY_WIRELESS, value)
            Settings.Global.putInt(resolver, KEY_USB, value)
        }
    }
}
