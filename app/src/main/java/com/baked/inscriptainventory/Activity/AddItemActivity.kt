package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
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
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.activity_add_item.radio0
import kotlinx.android.synthetic.main.activity_add_item.radio1

private const val TAG = "InscriptaInventory_AIA"
private const val STOCK_2 = "2"
private lateinit var tabArray: MutableList<String>

class AddItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val ipAddressName = "IPAddress"
    private var ipAddressStr = ""
    private var imageIndex = "0"
    private var sheetNum = "1"
    private var commentStr = "null"
    companion object SendReceiveTabNames {//load array from MainActivity parseJson()
        operator fun invoke(sent: MutableList<String>) {
            tabArray = sent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_chevron_left_white_18dp)
        ActionBar.DISPLAY_HOME_AS_UP
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)
        val partNumber = intent.getStringExtra("PartNum")
        val partNumNotNull = partNumber ?: ""
        partNumberEditText.setText(partNumNotNull)
        val currentTab = intent.getStringExtra("CurrentTab")

        sheetSelectSpinner!!.onItemSelectedListener = this
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, tabArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sheetSelectSpinner!!.adapter = aa
        sheetSelectSpinner.setSelection(currentTab?.toInt()!!)

        numInStockET.setOnFocusChangeListener { v, event ->
            numInStockET.hint = if (numInStockET.hasFocus()) "" else STOCK_2
        }
        minStockLevelET.setOnFocusChangeListener { v, event ->
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

        commentsImage.setOnClickListener {
            showCommentDialog()
        }

        addButton.setOnClickListener {
            if (descriptionEditText.text.isNullOrBlank()) {
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("""""Item Name" is required""")
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
            val partNum =
                if (partNumberEditText.text.isNullOrBlank()) "None" else partNumberEditText.text.toString()
            ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()

            CallServer(this).makeCall(
                content,//View
                ipAddressStr,//IP Address
                "addItem",//Reason
                numInStock,
                partNum,
                imageIndex,
                sheetNum,
                "2",//row number -- not actually used, row is inserted after header
                "false",//No need to send warning
                descriptionEditText.text.toString(),
                minStock,
                commentStr
            )
            addButton.isEnabled = false
            addButton.setBackgroundColor(ContextCompat.getColor(this,
                R.color.disabledGray
            ))
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

    @SuppressLint("InflateParams")
    fun showCommentDialog() {
        val view: View = layoutInflater.inflate(R.layout.dialog_edit_comment, null)
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
            .setNegativeButton("Dismiss", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Item Comment:")
        alert.show()
    }
}
