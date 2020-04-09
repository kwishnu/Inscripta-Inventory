package com.baked.inscriptainventory.Activity

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.baked.inscriptainventory.R
import com.baked.inscriptainventory.Adapter.SectionsPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import me.weishu.reflection.Reflection
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

private const val TAG = "InscriptaInventory_MA"

class MainActivity(private var InventoryItems: MutableList<MutableList<String>> = ArrayList()) : AppCompatActivity() {
    private var ipAddressStr = ""
    private var scannedResult = ""
    private val client = OkHttpClient()
    private var sharedPrefs: SharedPreferences? = null//getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
    private val prefsFilename = "SharedPreferences"
    private val initialStateName = "InitialState"
    private val ipAddressName = "IPAddress"
    private val startedAppName = "StartedAppOnce"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = toolbar
        setSupportActionBar(toolbar)
        sharedPrefs = this.getSharedPreferences(prefsFilename, 0)
        val editor = sharedPrefs!!.edit()

        //Shared preferences for first launch of app
        if (!sharedPrefs!!.getBoolean(startedAppName, false)) {
            val jsonStrFile = "json_string.txt"
            val jsonString = application.assets.open(jsonStrFile).bufferedReader().use{
                it.readText()
            }
            editor.putString(initialStateName, jsonString)
            editor.putString(ipAddressName, resources.getString(R.string.home_ip_address))
            editor.putBoolean("StartedAppOnce", true)
            editor.apply()
        }

        setContentView(R.layout.activity_main)
        callServer()//Load array with Excel data

        scan.setOnClickListener {//QR Code Floating Action Button
            run {
                IntentIntegrator(this@MainActivity).initiateScan()
            }
        }
        add.setOnClickListener {//Add inventory item Floating Action Button
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }
    }

    private fun callServer(){// ipAddressStr = "10.0.0.225"
        val stateStr = sharedPrefs!!.getString(initialStateName, String.toString())
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        val urlStr = "http://$ipAddressStr:10827/index.php"//was 80
        val request = Request.Builder()
            .url(urlStr)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(coordinator_layout, "No server response: functionality will be limited", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                this@MainActivity.runOnUiThread(Runnable {
                    parseJsonStr(stateStr.toString())
                    setTabs()
                })
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
                    this@MainActivity.runOnUiThread(Runnable {
                        parseJsonStr(resp)
                        setTabs()
                    })
                }
            }
        })
    }

//QR Code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//                2_1001228_001007880075_2003 product code format
//                2_1001148_001007180039_2006 product code format
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedResult = result.contents
                if (scannedResult.indexOf("_") > 0) {
                    val codePieces = scannedResult.split("_")
                    var sheet = "Not found"
                    var image = "Not found"
                    var partNumber = "Not found"
                    var description = "Not found"
                    var minStockLevel = "Not found"
                    var onHandNum = "Not found"
                    var row = "Not found"

                    for (e in InventoryItems){
                        if (codePieces[1] == e[2]){//check if part number matches given row in Excel spreadsheet data returned in server call
                            sheet = e[0]
                            image = e[1]
                            partNumber = e[2]
                            description = e[3]
                            minStockLevel = e[4]
                            onHandNum = e[5]
                            row = e[6]
                            break
                        }
                    }
                    if (sheet == "Not found"){//Part number not found, show dialog to add item
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder
                            .setMessage("Item not found. Add item?")
                            .setCancelable(true)
                            .setPositiveButton("OK", DialogInterface.OnClickListener {
                                    dialog, _ ->
                                val intent = Intent(this, AddItemActivity::class.java)
                                intent.putExtra("PartNum", codePieces[1])
                                startActivity(intent)
                            })
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                    dialog, _ -> dialog.cancel()
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Not Found")
                        alert.show()
                        return
                    }

                    var whichTabStr = ""
                    whichTabStr = when (sheet){
                        "1" -> "MainActivity1"
                        "2" -> "MainActivity2"
                        "3" -> "MainActivity3"
                        else -> return
                    }
                    val intent = Intent(this, ItemActionActivity::class.java)
                    intent.putExtra("Sheet", sheet)
                    intent.putExtra("Image", image)
                    intent.putExtra("PartNum", partNumber)
                    intent.putExtra("Item", description)
                    intent.putExtra("MinStockLevel", minStockLevel)
                    intent.putExtra("InStock", onHandNum)
                    intent.putExtra("Row", row)
                    intent.putExtra("FromActivity", whichTabStr)

                    this.startActivity(intent)
Log.d(TAG, "Yep, you are here")
                    val tabLayout = tabs as TabLayout//Go to appropriate tab...
                    val tab = tabLayout.getTabAt(sheet.toInt() - 1)
                    tab?.select()
                } else {//scanned result contains no underscore character, not an inventory QR code
                    val dialogBuilder = AlertDialog.Builder(this)
                    dialogBuilder
                        .setMessage("$scannedResult is not a recognized product code")
                        .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, _ -> dialog.cancel()
                        })

                    val alert = dialogBuilder.create()
                    alert.setTitle("Not a Product Code")
                    alert.show()
                }
            } else {
                Snackbar.make(coordinator_layout, "Scan failed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
        Log.d(TAG, "here")//This does nothing, use settingsClicked(), etc
            true
        }
        else ->  super.onOptionsItemSelected(item)
        }
    }

    fun settingsClicked(item: MenuItem) {
        val intent = Intent(this, SettingsActivity::class.java)
        this.startActivity(intent)
    }

    fun closeApp(item: MenuItem) {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onBackPressed() {
        this.finishAffinity()
    }

    override fun attachBaseContext(base: Context?) {//gets rid of non-SDK-components messages
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    private fun setTabs(){
        val sectionsPagerAdapter =
            SectionsPagerAdapter(
                InventoryItems,
                this@MainActivity,
                supportFragmentManager
            )
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    //Parse Excel Json object returned from server:
    private fun parseJsonStr(responseStr: String) {
        val respObj = JSONObject(responseStr)
        val sheet1 = JSONObject(respObj["0"].toString())
        val sheet2 = JSONObject(respObj["1"].toString())
        val sheet3 = JSONObject(respObj["2"].toString())

        for (i in 2 until sheet1.length() + 1){
            val str1 = JSONObject(sheet1[i.toString()].toString())["A"].toString()//Column A: Sheet
            val str2 = JSONObject(sheet1[i.toString()].toString())["B"].toString()//Column B: Image
            val str3 = JSONObject(sheet1[i.toString()].toString())["C"].toString()//Column C: Part Number
            val str4 = JSONObject(sheet1[i.toString()].toString())["D"].toString()//Column D: Description
            val str5 = JSONObject(sheet1[i.toString()].toString())["E"].toString()//Column E: Min Stock Level
            val str6 = JSONObject(sheet1[i.toString()].toString())["F"].toString()//Column F: Quantity on Hand
            val str7 = i.toString()//Row in Excel sheet
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
        }
        for (i in 2 until sheet2.length() + 1){
            val str1 = JSONObject(sheet2[i.toString()].toString())["A"].toString()
            val str2 = JSONObject(sheet2[i.toString()].toString())["B"].toString()
            val str3 = JSONObject(sheet2[i.toString()].toString())["C"].toString()
            val str4 = JSONObject(sheet2[i.toString()].toString())["D"].toString()
            val str5 = JSONObject(sheet2[i.toString()].toString())["E"].toString()
            val str6 = JSONObject(sheet2[i.toString()].toString())["F"].toString()
            val str7 = i.toString()
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
        }
        for (i in 2 until sheet3.length() + 1){
            val str1 = JSONObject(sheet3[i.toString()].toString())["A"].toString()
            val str2 = JSONObject(sheet3[i.toString()].toString())["B"].toString()
            val str3 = JSONObject(sheet3[i.toString()].toString())["C"].toString()
            val str4 = JSONObject(sheet3[i.toString()].toString())["D"].toString()
            val str5 = JSONObject(sheet3[i.toString()].toString())["E"].toString()
            val str6 = JSONObject(sheet3[i.toString()].toString())["F"].toString()
            val str7 = i.toString()
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
        }
    }
}
