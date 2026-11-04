package com.jsy.adbtoggle

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jsy.adbtoggle.DebugToggle.Target

class MainActivity : AppCompatActivity() {

    private class Row(val status: TextView, val toggle: SwitchCompat)

    private lateinit var permissionStatus: TextView
    private lateinit var grantGroup: View
    private lateinit var stateGroup: View
    private lateinit var rows: Map<Target, Row>

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
        rows = mapOf(
            Target.DEV_OPTIONS to Row(findViewById(R.id.dev_status), findViewById(R.id.dev_switch)),
            Target.USB to Row(findViewById(R.id.usb_status), findViewById(R.id.usb_switch)),
            Target.WIRELESS to Row(findViewById(R.id.wireless_status), findViewById(R.id.wireless_switch)),
        )

        findViewById<Button>(R.id.copy_button).setOnClickListener { copyGrantCommand() }
        for ((target, row) in rows) {
            row.toggle.setOnClickListener {
                UiSync.toggleWithFeedback(this, target)
                UiSync.refreshTile(this)
                render()
            }
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
        for ((target, row) in rows) {
            val on = state.isOn(target)
            row.status.text = getString(R.string.status_line, UiSync.label(this, target), UiSync.onOff(this, on))
            row.toggle.isChecked = on
        }
    }

    private fun copyGrantCommand() {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("adb grant", getString(R.string.cmd_grant)))
        Toast.makeText(this, R.string.toast_copied, Toast.LENGTH_SHORT).show()
    }
}
