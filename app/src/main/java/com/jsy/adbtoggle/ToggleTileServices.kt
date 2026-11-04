package com.jsy.adbtoggle

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.jsy.adbtoggle.DebugToggle.Target

abstract class BaseToggleTileService(private val target: Target) : TileService() {

    override fun onStartListening() {
        updateTile()
        // 타일을 열 때 위젯도 같은 상태로 맞춘다 (설정 앱에서 바꾼 경우 포함).
        UiSync.refreshWidgets(this)
    }

    override fun onClick() {
        UiSync.toggleWithFeedback(this, target)
        updateTile()
        UiSync.refreshTile(this) // 디버깅을 켜면 개발자 모드 타일도 바뀔 수 있다.
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        if (!DebugToggle.hasPermission(this)) {
            // 탭은 받아야 안내 토스트를 띄울 수 있어서 UNAVAILABLE 대신 INACTIVE + 안내 문구를 쓴다.
            tile.state = Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, UiSync.iconRes(target, false))
            tile.subtitle = getString(R.string.tile_need_permission)
        } else {
            val on = DebugToggle.readState(this).isOn(target)
            tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, UiSync.iconRes(target, on))
            tile.subtitle = UiSync.onOff(this, on)
        }
        tile.label = UiSync.label(this, target)
        tile.updateTile()
    }
}

class DevOptionsTileService : BaseToggleTileService(Target.DEV_OPTIONS)
class UsbTileService : BaseToggleTileService(Target.USB)
class WirelessTileService : BaseToggleTileService(Target.WIRELESS)
