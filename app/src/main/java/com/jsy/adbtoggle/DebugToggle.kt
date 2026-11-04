package com.jsy.adbtoggle

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * 개발자 모드(development_settings_enabled), 유선 디버깅(adb_enabled),
 * 무선 디버깅(adb_wifi_enabled)을 읽고 쓰는 유일한 곳.
 * 이 앱에서 Settings.Global.putInt 는 이 파일의 [putKey] 에서만 호출한다.
 */
object DebugToggle {

    enum class Target(internal val key: String) {
        DEV_OPTIONS("development_settings_enabled"),
        USB("adb_enabled"),
        WIRELESS("adb_wifi_enabled"),
    }

    private const val PERMISSION = "android.permission.WRITE_SECURE_SETTINGS"

    data class State(val devOptionsOn: Boolean, val usbOn: Boolean, val wirelessOn: Boolean) {
        fun isOn(target: Target): Boolean = when (target) {
            Target.DEV_OPTIONS -> devOptionsOn
            Target.USB -> usbOn
            Target.WIRELESS -> wirelessOn
        }
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
        fun read(target: Target) = Settings.Global.getInt(resolver, target.key, 0) == 1
        return State(
            devOptionsOn = read(Target.DEV_OPTIONS),
            usbOn = read(Target.USB),
            wirelessOn = read(Target.WIRELESS),
        )
    }

    /** 해당 항목만 반대로 바꾼다. */
    fun toggle(context: Context, target: Target): Result =
        setEnabled(context, target, enable = !readState(context).isOn(target))

    fun setEnabled(context: Context, target: Target, enable: Boolean): Result {
        if (!hasPermission(context)) return Result.NoPermission
        try {
            // 디버깅을 켤 때는 개발자 모드가 꺼져 있으면 먼저 켠다.
            if (enable && target != Target.DEV_OPTIONS && !readState(context).devOptionsOn) {
                putKey(context, Target.DEV_OPTIONS, 1)
            }
            putKey(context, target, if (enable) 1 else 0)
        } catch (e: SecurityException) {
            return Result.NoPermission
        }
        // 기기마다 값이 안 먹힐 수 있으므로 반드시 다시 읽어서 확인한다.
        val after = readState(context)
        val applied = after.isOn(target) == enable &&
            (!enable || target == Target.DEV_OPTIONS || after.devOptionsOn)
        return if (applied) Result.Success(after) else Result.NotApplied(after)
    }

    private fun putKey(context: Context, target: Target, value: Int) {
        Settings.Global.putInt(context.contentResolver, target.key, value)
    }
}
