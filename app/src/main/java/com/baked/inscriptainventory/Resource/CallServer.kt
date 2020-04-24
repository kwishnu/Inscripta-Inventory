package com.baked.inscriptainventory.Resource

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.baked.inscriptainventory.Activity.MainActivity
import com.baked.inscriptainventory.Fragment.*
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
private const val TAG = "InscriptaInventory_CS"

class CallServer ( private val context: Context) {
    private val client = OkHttpClient()
    private val portNum = MainActivity.globalPortNum
    companion object {
        val MEDIA_TYPE_MARKDOWN = "text/x-markdown; charset=utf-8".toMediaType()
    }
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
        minStockLevel: String,
        commentStr: String
    ){
        val urlStr = "http://$ipAddressStr:$portNum/index.php?Reason=$reason&InvCount=$invCount" +
                "&PartNumber=$partNum&ImageNum=$imageNum&Sheet=$sheetNum&RowNum=$rowNum" +
                "&SendWarning=$sendWarning&ItemName=$itemName" +
                "&MinStockLevel=$minStockLevel&Host=empty&Who=" +
                "empty&Date=empty&Time=empty"
        val postBody = FormBody.Builder()
            .add("CommentStr", commentStr)
            .build()
        val request = Request.Builder()
            .post(postBody)
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
                                "editComment" -> " edited"
                                else -> " added"
                            }
                            Snackbar.make(view, "Item successfully$appendStr",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()

                            //Adapter notifyDataSetChanged():
                            val index = (rowNum.toInt() - 2).toString()
                            if (reason != "newTab" && reason != "editComment") {
                                when (sheetNum){
                                    "1" -> Fragment0.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "2" -> Fragment1.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "3" -> Fragment2.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "4" -> Fragment3.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "5" -> Fragment4.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "6" -> Fragment5.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "7" -> Fragment6.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "8" -> Fragment7.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "9" -> Fragment8.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "10" -> Fragment9.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "11" -> Fragment10.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
                                    "12" -> Fragment11.SetAdapterFromActivity(reason, index, imageNum, partNum!!, itemName, minStockLevel, invCount, commentStr)
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