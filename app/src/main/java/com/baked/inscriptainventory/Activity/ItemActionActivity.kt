package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baked.inscriptainventory.*
import com.baked.inscriptainventory.Fragment.*
import com.baked.inscriptainventory.Resource.ImagesArray
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_action_activity.*
import okhttp3.*
import java.io.IOException
private const val TAG = "InscriptaInventory_IAA"
private lateinit var imagesArray: MutableList<String>

class ItemActionActivity : AppCompatActivity(){
    private val client = OkHttpClient()
    private var ipAddressStr = ""
    private var imageIndex = ""
    private var commentStr = ""
    private var shouldClose = 2
    private var seenActivity = false
    private var newQuantity = 0
    private var newValueStr = ""
    private var fromActivity = "Unknown"
    private var sharedPrefs: SharedPreferences? = null
    private val prefsFilename = "SharedPreferences"
    private val initialStateName = "InitialState"
    private val ipAddressName = "IPAddress"
    companion object {
        fun sendReceiveImages(imagesSent: MutableList<String>) {
            imagesArray = imagesSent
        }
        var itemIndexInGlobalArray = -1
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.slide_in_top,
            R.anim.slide_out_bottom
        )
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
        imageIndex = intent?.getStringExtra("Image").toString()
        imageIndex = if (imageIndex.isEmpty() || imageIndex == "null") "0" else imageIndex
        val itemPartNum = intent.getStringExtra("PartNum")
        var minStockLevel = intent.getStringExtra("MinStockLevel")
        var inStock = intent.getStringExtra("InStock")
        minStockLevel = if (minStockLevel!!.isEmpty() || minStockLevel == "null") "0" else minStockLevel
        inStock = if (inStock!!.isEmpty() || inStock == "null") "0" else inStock
        fromActivity = intent.getStringExtra("FromActivity")!!.toString()

        supportActionBar!!.title = if (itemPartNum == "None" || itemPartNum == "null") "" else getString(R.string.detail_title) + " " + itemPartNum
        inventoryItemName.text = itemName
        val minStockLevelNumber = minStockLevel.toInt()

        //Set TextViews and ImageView:
        numInInventory.text = "Inventory Count: $inStock"
        minStockLevelNum.text = "Min. Stock Level: $minStockLevel"
        val path = imagesArray[imageIndex.toInt()]
        Picasso.get()
            .load(path)
            .centerCrop()
            .resize(50, 0)
            .into(imageView)

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
                submitButton.setBackgroundColor(ContextCompat.getColor(this,
                    R.color.disabledGray
                ))
                newValueStr = newQuantity.toString()

                var delimSheetNumStr = ""
                var delimRowNumStr = ""

                for (i in 0 until MainActivity.globalDataArray.size) {
                    if (MainActivity.globalDataArray[i][2] == itemName && MainActivity.globalDataArray[i][1] == itemPartNum) {
                        delimSheetNumStr = MainActivity.globalDataArray[i][7] + "~" + delimSheetNumStr
                        delimRowNumStr = MainActivity.globalDataArray[i][6] + "~" + delimRowNumStr
                    }
                }
                val sheetResultStr = delimSheetNumStr.dropLastWhile { it.toString() == "~" }//Remove terminal ~ characters
                val rowResultStr = delimRowNumStr.dropLastWhile { it.toString() == "~" }

                callServer(
                    constraintLayout,
                    itemName!!,
                    itemPartNum,
                    newValueStr,
                    minStockLevel,
                    sheetResultStr,
                    rowResultStr,
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
    sheetNumArrayStr: String,
    rowNumArrayStr: String,
    sendWarning: String
    ) {
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        val portNum = MainActivity.globalPortNum;
        val emailStr = MainActivity.globalEmailStr;

        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)
        val editor = sharedPrefs!!.edit()

        val stateStr = sharedPrefs!!.getString(initialStateName, String.toString()).toString()
        val urlStr = "http://$ipAddressStr:$portNum/index.php?IP=$ipAddressStr&PortNum=$portNum"
        val request = Request.Builder()
            .url(urlStr)
            .build()
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }

            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    this@ItemActionActivity.runOnUiThread(Runnable {
                        if (resp == stateStr){
                            val urlStr2 = "http://$ipAddressStr:$portNum/index.php?Reason=changeCount&InvCount=$invCount" +
                                    "&PartNumber=$partNum&Sheet=$sheetNumArrayStr&RowNum=$rowNumArrayStr" +
                                    "&SendWarning=$sendWarning&ItemName=$itemName&ImageNum=0" +
                                    "&MinStockLevel=$minStockLevel$emailStr"
                            val postBody = FormBody.Builder()
                                .add("CommentStr", commentStr)
                                .build()
                            val request2 = Request.Builder()
                                .post(postBody)
                                .url(urlStr2)
                                .build()
                            client.newCall(request2).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Snackbar.make(view, "No server response: enter changes manually",
                                        Snackbar.LENGTH_LONG).setAction("Action", null).show()
                                    e.printStackTrace()
                                }
                                @SuppressLint("SetTextI18n")
                                override fun onResponse(call: Call, response: Response){
                                    response.use {
                                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                        val resp2 = response.body!!.string()
                                        val successful = resp2.indexOf("Success") > -1 || resp2.indexOf("SERVER") > -1
                                        Log.d(TAG, resp2)
                                        this@ItemActionActivity.runOnUiThread(Runnable {
                                            val intent = Intent()
                                            if (successful) {
                                                Snackbar.make(view,"Success\nInventory adjustment made",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show()
                                                numInInventory.text = "Inventory Count: $newQuantity"

//                            Adapter notifyDataSetChanged(): (onActivityResult when from RecyclerView click, from MainActivity* when from QR scan)
                                                val rowNumArray = rowNumArrayStr.split("~")
                                                val sheetArray = sheetNumArrayStr.split("~")
                                                val imageNum = "null"
                                                val reason = "changeCount"

                                                for (i in sheetArray.indices){
                                                    itemIndexInGlobalArray = -1
                                                    val theSheet = sheetArray[i]
                                                    for (j in 0 until MainActivity.globalDataArray.size) {
                                                        if (MainActivity.globalDataArray[j][7] != theSheet) {
                                                            itemIndexInGlobalArray++
                                                        } else {
                                                            break
                                                        }
                                                    }
                                                    itemIndexInGlobalArray = itemIndexInGlobalArray + rowNumArray[i].toInt() - 1

                                                    val index = (rowNumArray[i].toInt() - 2).toString()
                                                    when (sheetArray[i]){
                                                        "0" -> Fragment0.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "1" -> Fragment1.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "2" -> Fragment2.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "3" -> Fragment3.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "4" -> Fragment4.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "5" -> Fragment5.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "6" -> Fragment6.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "7" -> Fragment7.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "8" -> Fragment8.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "9" -> Fragment9.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "10" -> Fragment10.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                        "11" -> Fragment11.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                                    }
                                                }
                                                client.newCall(request).enqueue(object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        Snackbar.make(view, "No server response", Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show()
                                                    }

                                                    override fun onResponse(call: Call, response: Response){
                                                        response.use {
                                                            if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                                            val resp3 = response.body!!.string()
                                                            this@ItemActionActivity.runOnUiThread(Runnable {


                                                                editor.putString(initialStateName, resp3)
                                                                editor.apply()


                                                            })

                                                        }
                                                    }
                                                })

                                                setResult(Activity.RESULT_OK, intent)
                                            } else {
                                                Snackbar.make(view,"Unexpected error\nEnter changes manually",
                                                    Snackbar.LENGTH_LONG).setAction("Action", null).show()
                                                setResult(Activity.RESULT_CANCELED, intent)
                                            }
                                        })
                                    }
                                }
                            })
                        } else {
                            val dialogBuilder = AlertDialog.Builder(this@ItemActionActivity)
                            dialogBuilder
                                .setMessage("InventoryXls file has been edited and needs to be refreshed. Close to restart?")
                                .setCancelable(true)
                                .setPositiveButton("Close App", DialogInterface.OnClickListener {
                                        dialog, _ ->
                                    closeApp()
                                })
                                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                        dialog, _ -> dialog.cancel()
                                })

                            val alert = dialogBuilder.create()
                            alert.setTitle("Server File Changed")
                            alert.show()
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
        return true
    }

    fun closeApp() {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}





