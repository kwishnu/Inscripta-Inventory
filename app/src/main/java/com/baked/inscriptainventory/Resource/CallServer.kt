package com.baked.inscriptainventory.Resource

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import com.baked.inscriptainventory.Fragment.FirstFragment
import com.baked.inscriptainventory.Fragment.SecondFragment
import com.baked.inscriptainventory.Fragment.ThirdFragment
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import java.io.IOException
private const val TAG = "InscriptaInventory_CS"

class CallServer ( private var context: Context) {
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
            val urlStr = "http://$ipAddressStr:10827/index.php?Reason=$reason&InvCount=$invCount" +
                    "&PartNumber=$partNum&ImageNum=$imageNum&Sheet=$sheetNum&RowNum=$rowNum" +
                    "&SendWarning=$sendWarning&ItemName=$itemName" +
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
                    val successful = resp.indexOf("Success") > -1
                    (context as Activity).runOnUiThread(Runnable {
                        if (successful) {
                            var appendStr =  when (reason){
                                "addItem" -> " added"
                                "editItem" -> " edited"
                                "deleteItem" -> " deleted"
                                else -> ""
                            }
                            Snackbar.make(view, "Item successfully$appendStr",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()

                            //Adapter notifyDataSetChanged():
                            val index = (rowNum.toInt() - 2).toString()
                            when (sheetNum){
                                "1" -> FirstFragment.SetAdapterFromActivity(reason, sheetNum, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                "2" -> SecondFragment.SetAdapterFromActivity(reason, sheetNum, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                                "3" -> ThirdFragment.SetAdapterFromActivity(reason, sheetNum, index, imageNum, partNum!!, itemName, minStockLevel, invCount)
                            }
                        } else {
                            Snackbar.make(view,"Unexpected error\nEnter changes manually",
                                Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        }
                    })
                }
            }
        })
    }
}