package com.baked.inscriptainventory

import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import okhttp3.*

private const val TAG = "InscriptaInventory_CS"

class CallServer {
    private val client = OkHttpClient()


    fun makeCall (
        view: View,
        ipAddressStr: String,
        invCount: String,
        partNum: String?,
        sheetNum: String,
        rowNum: String,
        sendWarning: String,
        itemName: String,
        minStockLevel: String

    ){
        val urlStr = "http://$ipAddressStr:80/index.php?NewCount=$invCount" +
                "&PartNumber=$partNum&Sheet=$sheetNum&RowNum=$rowNum" +
                "&SendWarning=$sendWarning&ItemName=$itemName" +
                "&MinStockLevel=$minStockLevel"
        val request = Request.Builder()
            .url(urlStr)
            .build()

        Snackbar.make(view,"It worked :-)",
            Snackbar.LENGTH_LONG).setAction("Action", null).show()

//        Log.d (TAG, params[1])

    }


}