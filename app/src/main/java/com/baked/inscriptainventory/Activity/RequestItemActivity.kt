package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baked.inscriptainventory.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.request_item_activity.*
import okhttp3.*
import java.io.IOException
private const val TAG = "InscriptaInventory_RIA"
private lateinit var imagesArray: MutableList<String>

class RequestItemActivity : AppCompatActivity(){
    private val client = OkHttpClient()
    private var ipAddressStr = ""
    private var imageIndex = ""
    private var commentStr = ""
    companion object {
        fun sendReceiveImages(imagesSent: MutableList<String>) {
            imagesArray = imagesSent
        }
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
        setContentView(R.layout.request_item_activity)

        ipAddressStr = MainActivity.globalIPAddress

        val itemName = intent.getStringExtra("Item")
        imageIndex = intent?.getStringExtra("Image").toString()
        imageIndex = if (imageIndex.isEmpty() || imageIndex == "null") "0" else imageIndex

        var itemPartNum = intent.getStringExtra("PartNum")
        itemPartNum = if (itemPartNum!!.isEmpty() || itemPartNum == "null") "None" else itemPartNum

        supportActionBar!!.title = "Send Email Request"
        inventoryItemName.text = "Item Name:\n    $itemName"
        inventoryItemNumber.text = "Catalog Number:\n    $itemPartNum"

        val path = imagesArray[imageIndex.toInt()]
        Picasso.get()
            .load(path)
            .centerCrop()
            .resize(50, 0)
            .into(imageView)

        submitButton.setOnClickListener {
            val quantityStr = if (numberToRequestTxt.text.isNullOrBlank()) "(Number not specified)" else numberToRequestTxt.text.toString()
            val sender = if (nameTxt.text.isNullOrBlank()) "(Name not given)" else nameTxt.text.toString()
            commentStr = if (additionalCommentTxt.text.isNullOrBlank()) "(No comments)" else additionalCommentTxt.text.toString()

            submitButton.isEnabled = false
            submitButton.setBackgroundColor(ContextCompat.getColor(this,
                R.color.disabledGray
            ))

            callServer(
                content,
                itemName!!,
                itemPartNum,
                quantityStr,
                sender,
                commentStr
            )
        }
    }

    private fun callServer(
    view: View,
    itemName: String,
    partNum: String?,
    numRequested: String,
    senderStr: String,
    commentStr: String
    ) {
        val portNum = MainActivity.globalPortNum;
        val emailStr = MainActivity.globalEmailStr;
        val urlStr = "http://$ipAddressStr:$portNum/index.php?SendEmailRequest=Send&ItemName=$itemName&PartNumber=$partNum" +
                "&NumRequested=$numRequested&Sender=$senderStr$emailStr"
        val postBody = FormBody.Builder()
            .add("CommentStr", commentStr)
            .build()
        val request = Request.Builder()
            .post(postBody)
            .url(urlStr)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response: Email not sent",
                Snackbar.LENGTH_LONG).setAction("Action", null).show()
                e.printStackTrace()
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    val successful = resp.indexOf("Success") > -1 || resp.indexOf("SERVER") > -1
Log.d(TAG, resp)
                    this@RequestItemActivity.runOnUiThread(Runnable {
                        val intent = Intent()
                        if (successful) {
                            Snackbar.make(view,"Success\nEmail has been sent",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        } else {
                            Snackbar.make(view,"Unexpected error\nEmail failed",
                            Snackbar.LENGTH_LONG).setAction("Action", null).show()
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
}





