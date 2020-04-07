package com.baked.inscriptainventory

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_item.*
private const val TAG = "InscriptaInventory_AIA"
private const val STOCK_2 = "2"
class AddItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sheetArray = arrayOf("Beta Kits", "Internal Reagents", "Purchased Parts")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, sheetArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner!!.adapter = aa

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

        addButton.setOnClickListener {
            Snackbar.make(content, "Button clicked!",
                Snackbar.LENGTH_LONG).setAction("Action", null).show()
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
