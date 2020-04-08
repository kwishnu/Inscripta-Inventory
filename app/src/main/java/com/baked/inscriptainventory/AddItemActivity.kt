package com.baked.inscriptainventory

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.activity_add_item.radio0
import kotlinx.android.synthetic.main.activity_add_item.radio1
import kotlinx.android.synthetic.main.item_action_activity.*

private const val TAG = "InscriptaInventory_AIA"
private const val STOCK_2 = "2"

class AddItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sheetArray = arrayOf("Beta Kits", "Internal Reagents", "Purchased Parts")
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""
    private var imageIndex = "0"
    private var sheetNum = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, sheetArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner!!.adapter = aa

        numInStockET.setOnFocusChangeListener() { v, event ->
            numInStockET.hint = if (numInStockET.hasFocus()) "" else STOCK_2
        }
        minStockLevelET.setOnFocusChangeListener() { v, event ->
            minStockLevelET.hint = if (minStockLevelET.hasFocus()) "" else STOCK_2
        }
        var currentSelected = radio0

        listOf<RadioButton>(
            radio0, radio1, radio2, radio3, radio4, radio5, radio6, radio7
        ).forEach {
            it.setOnClickListener { _ ->
                currentSelected.isChecked = false
                currentSelected = it
                currentSelected.isChecked = true
            }
        }

        addButton.setOnClickListener {
            if (descriptionEditText.text.isNullOrBlank() || partNumberEditText.text.isNullOrBlank()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("""""Item Name" and "Part Number" fields are required""")
                    .setCancelable(true)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Empty Field")
                alert.show()
                return@setOnClickListener
            }
            if (radio0.isChecked) imageIndex = "0"
            if (radio1.isChecked) imageIndex = "1"
            if (radio2.isChecked) imageIndex = "2"
            if (radio3.isChecked) imageIndex = "3"
            if (radio4.isChecked) imageIndex = "4"
            if (radio5.isChecked) imageIndex = "5"
            if (radio6.isChecked) imageIndex = "6"
            if (radio7.isChecked) imageIndex = "7"
            val numInStock =
                if (numInStockET.text.isNullOrBlank()) "0" else numInStockET.text.toString()
            val minStock =
                if (minStockLevelET.text.isNullOrBlank()) "0" else minStockLevelET.text.toString()

            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
            CallServer(this).makeCall(
                content,//View
                ipAddressStr,//IP Address
                "addItem",//Reason
                numInStock,
                partNumberEditText.text.toString(),
                imageIndex,
                sheetNum,
                "1",
                "false",//No need to send warning
                descriptionEditText.text.toString(),
                minStock
            )
            addButton.isEnabled = false
            addButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledGray))
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d(TAG, "Spinner")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        sheetNum = (position + 1).toString()
    }

}
