package com.baked.inscriptainventory

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_item.*
private const val TAG = "InscriptaInventory_EIA"
private const val STOCK_2 = "2"
class EditItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sheetArray = arrayOf("Beta Kits", "Internal Reagents", "Purchased Parts")
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, sheetArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner!!.adapter = aa

        val itemName = intent.getStringExtra("Item")
        val imageIndex = intent.getStringExtra("Image")
        val itemPartNum = intent.getStringExtra("PartNum")
        val minStockLevel = intent.getStringExtra("MinStockLevel")
        val inStock = intent.getStringExtra("InStock")
        val sheetNum = intent.getStringExtra("Sheet")
        val rowNum = intent.getStringExtra("Row")
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()
        var currentSelected: RadioButton? = null
        descriptionEditText.setText(itemName)
        partNumberEditText.setText(itemPartNum)
        numInStockET.setText(inStock)
        minStockLevelET.setText(minStockLevel)
        sheetSelectSpinner.setSelection(sheetNum?.toInt()!! - 1)

        editButton.setOnClickListener {
            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()

            CallServer(this).makeCall(
                content,//View
                ipAddressStr,//IP Address
                "editItem",//Reason
                numInStockET.text.toString(),
                partNumberEditText.text.toString(),
                imageIndex!!,
                sheetNum,
                rowNum!!,
                "false",//No need to send warning
                descriptionEditText.text.toString(),
                minStockLevelET.text.toString()
            )
        }

        when (imageIndex){
            "0" -> {
                radio0.isChecked = true
                currentSelected = radio0
            }
            "1" -> {
                radio1.isChecked = true
                currentSelected = radio1
            }
            "2" -> {
                radio2.isChecked = true
                currentSelected = radio2
            }
            "3" -> {
                radio3.isChecked = true
                currentSelected = radio3
            }
            "4" -> {
                radio4.isChecked = true
                currentSelected = radio4
            }
            "5" -> {
                radio5.isChecked = true
                currentSelected = radio5
            }
            "6" -> {
                radio6.isChecked = true
                currentSelected = radio6
            }
            "7" -> {
                radio7.isChecked = true
                currentSelected = radio7
            }
        }

        listOf<RadioButton>(
            radio0, radio1, radio2, radio3, radio4, radio5, radio6, radio7
        ).forEach {
            it.setOnClickListener { _ ->
                currentSelected?.isChecked = false
                currentSelected = it
                currentSelected?.isChecked = true
            }
        }

        numInStockET.setOnFocusChangeListener() { v, event ->
            numInStockET.hint = if(numInStockET.hasFocus()) "" else STOCK_2
        }
        minStockLevelET.setOnFocusChangeListener() { v, event ->
            minStockLevelET.hint = if(minStockLevelET.hasFocus()) "" else STOCK_2
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
        Log.d(TAG, "Spinner Item Selected: " + sheetArray[position].toString())
    }

}
