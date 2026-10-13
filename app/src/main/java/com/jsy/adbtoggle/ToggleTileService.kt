package com.jsy.adbtoggle

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ToggleTileService : TileService() {

    override fun onStartListening() {
        updateTile()
        // 타일을 열 때 위젯도 같은 상태로 맞춘다 (설정 앱에서 바꾼 경우 포함).
        UiSync.refreshWidgets(this)
    }

    override fun onClick() {
        UiSync.toggleWithFeedback(this)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        if (!DebugToggle.hasPermission(this)) {
            // 탭은 받아야 안내 토스트를 띄울 수 있어서 UNAVAILABLE 대신 INACTIVE + 안내 문구를 쓴다.
            tile.state = Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, R.drawable.ic_debug_off)
            tile.subtitle = getString(R.string.tile_need_permission)
        } else {
            val on = DebugToggle.readState(this).anyOn
            tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.icon = Icon.createWithResource(this, if (on) R.drawable.ic_debug_on else R.drawable.ic_debug_off)
            tile.subtitle = UiSync.onOff(this, on)
        }
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }
}
