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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.baked.inscriptainventory.Adapter.ImageGridAdapter
import com.baked.inscriptainventory.Resource.CallServer
import com.baked.inscriptainventory.R
import com.baked.inscriptainventory.Resource.ImagesArray
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_item.*
import okhttp3.*
import java.io.IOException

private const val TAG = "InscriptaInventory_AIA"
private const val STOCK_2 = "2"
private lateinit var tabArray: MutableList<String>

class AddItemActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val client = OkHttpClient()
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
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        ImageGridAdapter.setIGAIndex = 0

        setAdapter(ImagesArray().imageList)
        getImages(content)

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

            val numInStock =
                if (numInStockET.text.isNullOrBlank()) "0" else numInStockET.text.toString()
            val minStock =
                if (minStockLevelET.text.isNullOrBlank()) "0" else minStockLevelET.text.toString()
            val partNum =
                if (partNumberEditText.text.isNullOrBlank()) "None" else partNumberEditText.text.toString()

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

    private fun getImages(view: View) {
        val portNum = MainActivity.globalPortNum;
        val urlStr = "http://$ipAddressStr:$portNum/index.php?GetImages=$ipAddressStr&PortNum=$portNum"
        val request = Request.Builder()
            .url(urlStr)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response: using local images",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show()
                e.printStackTrace()
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    val escapedStr = resp.replace("\\", "")
                    Log.d(TAG, escapedStr)
                    val respArr = ArrayList(escapedStr.split("\",\""))
                    respArr[0] = respArr[0].substring(2)
                    respArr[respArr.size - 1] = respArr[respArr.size - 1].substring(0, respArr[respArr.size - 1].length - 2)

                    Log.d(TAG, respArr.toString())
                    val successful = respArr.size > 1
                    this@AddItemActivity.runOnUiThread(Runnable {
                        if (successful) {
                            setAdapter(respArr)
                        } else {
                            setAdapter(ImagesArray().imageList)
                            Snackbar.make(
                                view, "Error retrieving images",
                                Snackbar.LENGTH_LONG
                            ).setAction("Action", null).show()
                        }
                    })
                }
            }
        })
    }

    private fun setAdapter(images: ArrayList<String>){
        fun imageClickListener(position: Int) {
            imageIndex = position.toString()
            image_rv.adapter?.notifyDataSetChanged()
        }
        val glm = StaggeredGridLayoutManager(2, GridLayoutManager.HORIZONTAL)
        image_rv.layoutManager = glm
        val imageListener = { i: Int -> imageClickListener(i) }
        val iga = ImageGridAdapter(this@AddItemActivity, images, imageListener)
        image_rv.adapter = iga
        image_rv.adapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        ImageGridAdapter.setIGAIndex = 0
        super.onDestroy()
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
}
