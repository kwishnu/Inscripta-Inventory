package com.baked.inscriptainventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Context
import android.net.Uri
import java.io.IOException
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_action_activity.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
private const val TAG = "InscriptaInventory"

class ItemActionActivity : AppCompatActivity(){
    private val client = OkHttpClient()
    private var ipAddressStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_close_white_48dp)
        android.app.ActionBar.DISPLAY_HOME_AS_UP
        setContentView(R.layout.item_action_activity)
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        numberToSubmitTxt.postDelayed(Runnable {
            numberToSubmitTxt.requestFocus()
            imm.showSoftInput(numberToSubmitTxt, 0)
            numberToSubmitTxt.clearFocus()

        }, 100)
        radio0.isSelected = true
        val itemName =intent.getStringExtra("Item")
        val imageIndex =intent.getStringExtra("Image")
        val itemPartNum =intent.getStringExtra("PartNum")
        supportActionBar!!.title = getString(R.string.detail_title) + " " + itemPartNum

        inventoryItemName.text = itemName
        val itemImageStr = ImagesArray().IMAGE_URI[(imageIndex!!).toInt()]
        val uri =
            Uri.parse("android.resource://com.baked.inscriptainventory/drawable/$itemImageStr")
        imageView.setImageURI(uri)

        submitButton.setOnClickListener {
            val quantityStr = numberToSubmitTxt.text.toString()
            val addOrRemoveStr = if (radio0.isChecked) "Remove" else "Add"
            submitButton.text = getString(R.string.sent)
            submitButton.isEnabled = false
            submitButton.setBackgroundColor(ContextCompat.getColor(this, R.color.disabledGray))

            callServer(coordinatorLayout, quantityStr, addOrRemoveStr, itemPartNum)


            Log.d(TAG, "clicked")
        }
    }

    private fun callServer(view: View, addOrRemove: String, quantity: String, partNum: String?) {
        ipAddressStr = "10.0.0.225"//ip_input.text.toString()
        val urlStr = "http://$ipAddressStr:80/index.php?AddOrRemove=$addOrRemove&Number=$quantity&PartNumber=$partNum"
        val postBody = "$addOrRemove|$quantity"
        val request = Request.Builder()
            .url(urlStr)
            .post(postBody.toRequestBody(MEDIA_TYPE_MARKDOWN))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response: enter changes manually", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                e.printStackTrace()
            }
            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    this@ItemActionActivity.runOnUiThread(Runnable {

                        if (resp == "Success") {
                            Log.d(TAG, "Yep, success")

                            Snackbar.make(view,"Success\nInventory adjustment made", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        } else {
                            Snackbar.make(view,"Unexpected error\nEnter changes manually", Snackbar.LENGTH_LONG).setAction("Action", null).show()
                        }
                    })
                }
            }
        })
    }

        override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "text/x-markdown; charset=utf-8".toMediaType()
    }
}



