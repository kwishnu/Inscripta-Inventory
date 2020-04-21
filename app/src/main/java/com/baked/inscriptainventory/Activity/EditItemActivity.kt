package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baked.inscriptainventory.Resource.CallServer
import com.baked.inscriptainventory.R
import kotlinx.android.synthetic.main.activity_edit_item.*
private const val TAG = "InscriptaInventory_EIA"
private const val STOCK_2 = "2"
private lateinit var tabArray: MutableList<String>

class EditItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""
    private var commentStr = ""
    companion object SendReceiveTabNames {
        operator fun invoke(sent: MutableList<String>) {
            tabArray = sent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, tabArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner.isEnabled = false;
        sheetSelectSpinner.isClickable = false
        sheetSelectSpinner!!.adapter = aa

        val itemName = intent.getStringExtra("Item")
        var imageIndex = intent.getStringExtra("Image")
        val itemPartNum = intent.getStringExtra("PartNum")
        val minStockLevel = intent.getStringExtra("MinStockLevel")
        val inStock = intent.getStringExtra("InStock")
        val sheetNum = intent.getStringExtra("Sheet")
        val rowNum = intent.getStringExtra("Row")
        commentStr = intent.getStringExtra("Comment")!!.toString()
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()

        var currentSelected: RadioButton? = null
        descriptionEditText.setText(itemName)
        partNumberEditText.setText(itemPartNum)
        numInStockET.setText(inStock)
        minStockLevelET.setText(minStockLevel)
        sheetSelectSpinner.setSelection(sheetNum?.toInt()!! - 1)

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

        editButton.setOnClickListener {
            if (radio0.isChecked) imageIndex = "0"
            if (radio1.isChecked) imageIndex = "1"
            if (radio2.isChecked) imageIndex = "2"
            if (radio3.isChecked) imageIndex = "3"
            if (radio4.isChecked) imageIndex = "4"
            if (radio5.isChecked) imageIndex = "5"
            if (radio6.isChecked) imageIndex = "6"
            if (radio7.isChecked) imageIndex = "7"
            val numInStock = if (numInStockET.text.isNullOrBlank()) "0" else numInStockET.text.toString()
            val minStock = if (minStockLevelET.text.isNullOrBlank()) "0" else minStockLevelET.text.toString()

            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
           CallServer(this).makeCall(
               content,//View
               ipAddressStr,//IP Address
               "editItem",//Reason
               numInStock,
               partNumberEditText.text.toString(),
               imageIndex!!,
               sheetNum,
               rowNum!!,
               "false",//No need to send warning
               descriptionEditText.text.toString(),
               minStock,
               commentStr
           )
            editButton.isEnabled = false
            editButton.setBackgroundColor(ContextCompat.getColor(this,
                R.color.disabledGray
            ))
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

        commentsImage.setOnClickListener {
            showCommentDialog()
        }

        numInStockET.setOnFocusChangeListener { v, event ->
            numInStockET.hint = if(numInStockET.hasFocus()) "" else STOCK_2
        }
        minStockLevelET.setOnFocusChangeListener { v, event ->
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
        Log.d(TAG, "Spinner Item Selected: " + tabArray[position])
    }

    @SuppressLint("InflateParams")
    fun showCommentDialog() {
        val view: View = layoutInflater.inflate(R.layout.dialog_edit_comment, null);
        val etComment = view.findViewById<View>(R.id.et_comment) as EditText
        if (commentStr != "null") etComment.setText(commentStr)
        val dialogBuilder = this.let { androidx.appcompat.app.AlertDialog.Builder(it) }
        dialogBuilder
            .setMessage("")
            .setCancelable(true)
            .setView(view)
            .setPositiveButton("Save Comment", DialogInterface.OnClickListener { dialog, _ ->
                commentStr = etComment.text.toString()
            })
            .setNegativeButton("Dismiss", DialogInterface.OnClickListener { dialog, _ -> dialog.cancel()

            })
        val alert = dialogBuilder.create()
        alert.setTitle("Item Comment:")
        alert.show()
    }

}
