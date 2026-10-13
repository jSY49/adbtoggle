package com.jsy.adbtoggle

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var permissionStatus: TextView
    private lateinit var grantGroup: View
    private lateinit var stateGroup: View
    private lateinit var usbStatus: TextView
    private lateinit var wirelessStatus: TextView
    private lateinit var toggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // targetSdk 35+ 는 화면이 시스템 바 뒤까지 확장되므로 직접 여백을 준다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        permissionStatus = findViewById(R.id.permission_status)
        grantGroup = findViewById(R.id.grant_group)
        stateGroup = findViewById(R.id.state_group)
        usbStatus = findViewById(R.id.usb_status)
        wirelessStatus = findViewById(R.id.wireless_status)
        toggleButton = findViewById(R.id.toggle_button)

        findViewById<Button>(R.id.copy_button).setOnClickListener { copyGrantCommand() }
        toggleButton.setOnClickListener {
            UiSync.toggleWithFeedback(this)
            UiSync.refreshTile(this)
            render()
        }
    }

    override fun onResume() {
        super.onResume()
        render()
        UiSync.refreshAll(this)
    }

    private fun render() {
        val granted = DebugToggle.hasPermission(this)
        permissionStatus.setText(if (granted) R.string.permission_granted else R.string.permission_missing)
        grantGroup.visibility = if (granted) View.GONE else View.VISIBLE
        stateGroup.visibility = if (granted) View.VISIBLE else View.GONE
        if (!granted) return

        val state = DebugToggle.readState(this)
        usbStatus.text = getString(R.string.usb_status, UiSync.onOff(this, state.usbOn))
        wirelessStatus.text = getString(R.string.wireless_status, UiSync.onOff(this, state.wirelessOn))
        toggleButton.setText(if (state.anyOn) R.string.button_turn_off else R.string.button_turn_on)
    }

    private fun copyGrantCommand() {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("adb grant", getString(R.string.cmd_grant)))
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }
}
