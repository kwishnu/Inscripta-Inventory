package com.baked.inscriptainventory

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_action_activity.*
import okhttp3.*
import java.io.IOException
private const val TAG = "InscriptaInventory_IAA"

class ItemActionActivity : AppCompatActivity(){
    private val client = OkHttpClient()
    private var ipAddressStr = ""
    private var submitted = false
    private var shouldClose = 2
    private var seenActivity = false
    private var newQuantity = 0
    private var newValueStr = ""
    private var fromActivity = "Unknown"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white_48dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        setContentView(R.layout.item_action_activity)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //show keyboard after delay
        numberToSubmitTxt.postDelayed(Runnable {
            numberToSubmitTxt.requestFocus()
            imm.showSoftInput(numberToSubmitTxt, 0)
        }, 100)
        numberToSubmitTxt.postDelayed(Runnable {
            seenActivity = true
        }, 1000)

        //Close activity on retract keyboard:
        constraintLayout.viewTreeObserver.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
            val heightDiff: Int = constraintLayout.rootView.height - constraintLayout.height
            if (heightDiff > 100) {
                if ((shouldClose > 1) && seenActivity){
                    startMainActivity()
                } else {
                    shouldClose++
                }
            }
        })

        radio0.isSelected = true
        val itemName = intent.getStringExtra("Item")
        val imageIndex = intent.getStringExtra("Image")
        val itemPartNum = intent.getStringExtra("PartNum")
        val minStockLevel = intent.getStringExtra("MinStockLevel")
        val inStock = intent.getStringExtra("InStock")
        val sheetNum = intent.getStringExtra("Sheet")
        val rowNum = intent.getStringExtra("Row")
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()

        supportActionBar!!.title = getString(R.string.detail_title) + " " + itemPartNum
        inventoryItemName.text = itemName
        val itemImageStr = ImagesArray().IMAGE_URI[(imageIndex!!).toInt()]
        val uri = Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$itemImageStr")
        imageView.setImageURI(uri)
        numInInventory.text = "Inventory Count: $inStock"
        minStockLevelNum.text = "Min. Stock Level: $minStockLevel"

        submitButton.setOnClickListener {
            val quantityStr = if (numberToSubmitTxt.text.isNullOrBlank()) "1" else numberToSubmitTxt.text.toString()
            val addOrRemoveStr = if (radio0.isChecked) "Remove" else "Add"

            if ((quantityStr.toInt() > inStock!!.toInt() && addOrRemoveStr == "Remove")){
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder
                    .setMessage("Number exceeds stated number in stock. Please adjust accordingly")
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, _ -> dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Adjust Quantities")
                alert.show()

            } else {
                newQuantity = if (radio0.isChecked) (inStock.toInt() - quantityStr.toInt()) else (inStock.toInt() + quantityStr.toInt())
                val sendWarning = if (newQuantity <= minStockLevel!!.toInt()) "true" else "false"

                shouldClose = 0
                submitButton.text = getString(R.string.sent)
                submitButton.isEnabled = false
                submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledGray))
                newValueStr = newQuantity.toString()

                callServer(
                    constraintLayout,
                    newValueStr,
                    itemPartNum,
                    sheetNum!!,
                    rowNum!!,
                    sendWarning
                )
            }
        }

    }

private fun callServer(
        view: View,
        newCount: String,
        partNum: String?,
        sheetNum: String,
        rowNum: String,
        sendWarning: String
    ) {
        ipAddressStr = "10.0.0.225"//ip_input.text.toString()
        val urlStr = "http://$ipAddressStr:80/index.php?NewCount=$newCount" +
                "&PartNumber=$partNum&Sheet=$sheetNum&RowNum=$rowNum&SendWarning=$sendWarning"
        val request = Request.Builder()
            .url(urlStr)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response: enter changes manually",
                Snackbar.LENGTH_LONG).setAction("Action", null).show()
                e.printStackTrace()
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    this@ItemActionActivity.runOnUiThread(Runnable {
                        val intent = Intent()
                        if (resp == "Success") {
                            Snackbar.make(view,"Success\nInventory adjustment made",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()
                            numInInventory.text = "Inventory Count: $newQuantity"
                            val index = (rowNum.toInt() - 2).toString()
                            intent.putExtra("index", index)
                            intent.putExtra("newValue", newValueStr)
                            setResult(Activity.RESULT_OK, intent)
                            when (fromActivity){
                                "MainActivity1" -> FirstFragment.SetAdapterFromActivity(index, newValueStr)
                                "MainActivity2" -> SecondFragment.SetAdapterFromActivity(index, newValueStr)
                                "MainActivity3" -> ThirdFragment.SetAdapterFromActivity(index, newValueStr)
                            }
                        } else {
                            Snackbar.make(view,"Unexpected error\nEnter changes manually",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()
                            setResult(Activity.RESULT_CANCELED, intent)
                        }
                    })
                }
            }
        })
    }

    private fun startMainActivity() {
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        startMainActivity()
//            onBackPressed()
        return true
    }
}





