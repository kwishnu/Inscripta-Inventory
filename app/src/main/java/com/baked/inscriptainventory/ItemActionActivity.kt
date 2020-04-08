package com.baked.inscriptainventory

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
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
    private var imageIndex = ""
    private var shouldClose = 2
    private var seenActivity = false
    private var newQuantity = 0
    private var newValueStr = ""
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null//getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
    private val prefsFilename = "SharedPreferences"
    private val initialStateName = "InitialState"
    private val ipAddressName = "IPAddress"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white_48dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        setContentView(R.layout.item_action_activity)
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)

        //show keyboard after delay
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        imageIndex = intent.getStringExtra("Image")
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
        val minStockLevelNumber = minStockLevel?.toInt()

        //Set TextViews and ImageView:
        numInInventory.text = "Inventory Count: $inStock"
        minStockLevelNum.text = "Min. Stock Level: $minStockLevel"
        imageView.setImageURI(uri)

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
                val sendWarning = if (newQuantity <= minStockLevelNumber!!) "true" else "false"

                shouldClose = 0
                submitButton.text = getString(R.string.sent)
                submitButton.isEnabled = false
                submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledGray))
                newValueStr = newQuantity.toString()

                callServer(
                    constraintLayout,
                    itemName!!,
                    itemPartNum,
                    newValueStr,
                    minStockLevel,
                    sheetNum!!,
                    rowNum!!,
                    sendWarning
                )
            }
        }
    }

private fun callServer(
    view: View,
    itemName: String,
    partNum: String?,
    invCount: String,
    minStockLevel: String,
    sheetNum: String,
    rowNum: String,
    sendWarning: String
    ) {
        val stateStr = sharedPrefs!!.getString(initialStateName, String.toString())
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        val urlStr = "http://$ipAddressStr:80/index.php?Reason=changeCount&InvCount=$invCount" +
                "&PartNumber=$partNum&Sheet=$sheetNum&RowNum=$rowNum" +
                "&SendWarning=$sendWarning&ItemName=$itemName&ImageNum=0" +
                "&MinStockLevel=$minStockLevel"
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
                    val successful = resp.indexOf("Success") > -1 || resp.indexOf("SERVER") > -1

                    this@ItemActionActivity.runOnUiThread(Runnable {
                        val intent = Intent()
                        if (successful) {
                            Snackbar.make(view,"Success\nInventory adjustment made",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()
                            numInInventory.text = "Inventory Count: $newQuantity"

                            //Adapter notifyDataSetChanged():
                            val index = (rowNum.toInt() - 2).toString()
                            intent.putExtra("index", index)
                            intent.putExtra("newValue", newValueStr)
                            setResult(Activity.RESULT_OK, intent)
                            when (fromActivity){
                                "MainActivity1" -> FirstFragment.SetAdapterFromActivity("changeCount", sheetNum, index, imageIndex, partNum!!, itemName, minStockLevel, newValueStr)
                                "MainActivity2" -> SecondFragment.SetAdapterFromActivity("changeCount", sheetNum, index, imageIndex, partNum!!, itemName, minStockLevel, newValueStr)
                                "MainActivity3" -> ThirdFragment.SetAdapterFromActivity("changeCount", sheetNum, index, imageIndex, partNum!!, itemName, minStockLevel, newValueStr)
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





