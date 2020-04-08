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
import kotlinx.android.synthetic.main.activity_delete_item.*
private const val TAG = "InscriptaInventory_DIA"
private const val STOCK_2 = "2"

class DeleteItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sheetArray = arrayOf("Beta Kits", "Internal Reagents", "Purchased Parts")
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, sheetArray)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner.isEnabled = false;
        sheetSelectSpinner.isClickable = false
        sheetSelectSpinner!!.adapter = aa

        val itemName = intent.getStringExtra("Item")
        val imageIndex = intent.getStringExtra("Image")
        val itemPartNum = intent.getStringExtra("PartNum")
        val minStockLevel = intent.getStringExtra("MinStockLevel")
        val inStock = intent.getStringExtra("InStock")
        val sheetNum = intent.getStringExtra("Sheet")
        val rowNum = intent.getStringExtra("Row")
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()
        descriptionEditText.setText(itemName)
        partNumberEditText.setText(itemPartNum)
        numInStockET.setText(inStock)
        minStockLevelET.setText(minStockLevel)
        sheetSelectSpinner.setSelection(sheetNum?.toInt()!! - 1)

        when (imageIndex){
            "0" -> radio0.isChecked = true
            "1" -> radio1.isChecked = true
            "2" -> radio2.isChecked = true
            "3" -> radio3.isChecked = true
            "4" -> radio4.isChecked = true
            "5" -> radio5.isChecked = true
            "6" -> radio6.isChecked = true
            "7" -> radio7.isChecked = true
        }

        numInStockET.setOnFocusChangeListener() { v, event ->
            numInStockET.hint = if(numInStockET.hasFocus()) "" else STOCK_2
        }

        minStockLevelET.setOnFocusChangeListener() { v, event ->
            minStockLevelET.hint = if(minStockLevelET.hasFocus()) "" else STOCK_2
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

        deleteButton.setOnClickListener {
            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder
                .setMessage("Delete this item from InventoryXls.xslx?")
                .setCancelable(true)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, _ ->
                    CallServer(this).makeCall(
                        content,//View
                        ipAddressStr,//IP Address
                        "deleteItem",//Reason
                        "none",
                        "none",
                        "none",
                        sheetNum,
                        rowNum!!,
                        "false",//No need to send warning
                        "none",
                        "none"
                    )
                    deleteButton.isEnabled = false
                    deleteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledGray))
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, _ -> dialog.cancel()
                })

            val alert = dialogBuilder.create()
            alert.setTitle("Confirm Delete")
            alert.show()
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
