package com.baked.inscriptainventory.Resource

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.baked.inscriptainventory.Fragment.*
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import java.io.IOException
private const val TAG = "InscriptaInventory_CS"

class CallServer ( private val context: Context) {
    private val client = OkHttpClient()

    fun makeCall (
        view: View,
        ipAddressStr: String,
        reason: String,
        invCount: String,
        partNum: String?,
        imageNum: String,
        sheetNum: String,
        rowNum: String,
        sendWarning: String,
        itemName: String,
        minStockLevel: String
        ){
            val urlStr = "http://$ipAddressStr:80/index.php?Reason=$reason&InvCount=$invCount" +
                    "&PartNumber=$partNum&ImageNum=$imageNum&Sheet=$sheetNum&RowNum=$rowNum" +
                    "&SendWarning=$sendWarning&ItemName=$itemName" +
                    "&MinStockLevel=$minStockLevel&Host=empty&Who=" +
                    "empty&Date=empty&Time=empty"
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
                    Log.d(TAG, resp)
                    val successful = resp.indexOf("Success") > -1
                    (context as Activity).runOnUiThread(Runnable {
                        if (successful) {
                            val appendStr =  when (reason){
                                "addItem" -> " added"
                                "editItem" -> " edited"
                                "deleteItem" -> " deleted"
                                else -> " added"
                            }
                            Snackbar.make(view, "Item successfully$appendStr",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()

                            //Adapter notifyDataSetChanged():
                            val index = (rowNum.toInt() - 2).toString()
                            if (reason != "newTab") {
                                when (sheetNum){
                                    "1" -> Fragment0.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "2" -> Fragment1.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "3" -> Fragment2.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "4" -> Fragment3.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "5" -> Fragment4.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "6" -> Fragment5.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "7" -> Fragment6.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "8" -> Fragment7.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "9" -> Fragment8.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "10" -> Fragment9.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "11" -> Fragment10.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                    "12" -> Fragment11.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                }
                            }
                        } else {
                            Snackbar.make(view,"Error\nInventoryXls file may be open\nEnter changes manually",
                                Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        }
                    })
                }
            }
        })
    }
}