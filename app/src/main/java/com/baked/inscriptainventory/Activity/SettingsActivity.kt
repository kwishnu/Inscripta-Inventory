package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.baked.inscriptainventory.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.settings_layout.*

class SettingsActivity : AppCompatActivity() {
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private val portName = "Port"
    private var ipAddressStr = ""
    private var portNumStr = ""
    lateinit var editor: SharedPreferences.Editor

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        ip_address_edittext.postDelayed(Runnable {
            ip_address_edittext.requestFocus()
            imm.showSoftInput(ip_address_edittext, 0)
        }, 100)
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        portNumStr = sharedPrefs!!.getString(portName, String.toString()).toString()
        ip_address_edittext.setText(ipAddressStr)
        port_num_edittext.setText(portNumStr)

        ip_clear_button.setOnClickListener {
            ip_address_edittext.setText("")
        }

        port_clear_button.setOnClickListener {
            port_num_edittext.setText("")
        }

        ip_save_button.setOnClickListener {
            editor = sharedPrefs!!.edit()
            editor.putString(ipAddressName, ip_address_edittext.text.toString())
            editor.apply()

            Snackbar.make(settings, "IP Address saved", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }

        port_save_button.setOnClickListener {
            editor = sharedPrefs!!.edit()
            editor.putString(portName, port_num_edittext.text.toString())
            editor.apply()

            Snackbar.make(settings, "Port Number saved", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}