package com.baked.inscriptainventory.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.baked.inscriptainventory.Adapter.SectionsPagerAdapter
import com.baked.inscriptainventory.R
import com.baked.inscriptainventory.Resource.CallServer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import me.weishu.reflection.Reflection
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


private var TabTitles: MutableList<String> = ArrayList()
private const val TAG = "InscriptaInventory_MA"

class MainActivity(private var InventoryItems: MutableList<MutableList<String>> = ArrayList(),
                   private var InventoryTabs:  MutableList<MutableList<MutableList<String>>> = ArrayList()
) : AppCompatActivity()
{
    private var ipAddressStr = ""
    private var scannedResult = ""
    private val client = OkHttpClient()
    private var sharedPrefs: SharedPreferences? = null//getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
    private val prefsFilename = "SharedPreferences"
    private val initialStateName = "InitialState"
    private val ipAddressName = "IPAddress"
    private val startedAppName = "StartedAppOnce"
    private var currentTab = 0
    companion object {
        var globalIPAddress = ""
        const val globalPortNum = ""
        const val globalEmailStr = ""
        var globalImageIndex = "0"
        var globalPartNumber = "none"
        var globalItemName = "none"
        var globalMinStockLevel = "0"
        var globalStockCount = "0"
        var globalCommentStr = "null"
        var globalItemOnClipboard = false
        var globalDataArray: MutableList<MutableList<String>> = ArrayList()
    }

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
            currentTab = tabs.selectedTabPosition
            intent.putExtra("CurrentTab", currentTab.toString())
            startActivity(intent)
        }
    }

    private fun callServer(){
        val stateStr = sharedPrefs!!.getString(initialStateName, String.toString())
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        globalIPAddress = ipAddressStr
        val urlStr = "http://$ipAddressStr:10827/index.php"
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
                    var comment = "Not found"
                    var row = "Not found"
                    loop@ for (t in InventoryTabs.indices) {
                        for (e in InventoryTabs[t]) {
                            if (codePieces[1] == e[1]) {//check if part number matches given row in Excel spreadsheet data returned in server call
                                sheet = (t + 1).toString()
                                image = e[0]
                                partNumber = e[1]
                                description = e[2]
                                minStockLevel = e[3]
                                onHandNum = e[4]
                                comment = e[5]
                                row = e[6]
                                break@loop
                            }
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
                                currentTab = tabs.selectedTabPosition
                                intent.putExtra("CurrentTab", currentTab.toString())
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

                    val whichTabStr = "MainActivity$sheet"

                    val intent = Intent(this, ItemActionActivity::class.java)
                    intent.putExtra("Sheet", sheet)
                    intent.putExtra("Image", image)
                    intent.putExtra("PartNum", partNumber)
                    intent.putExtra("Item", description)
                    intent.putExtra("MinStockLevel", minStockLevel)
                    intent.putExtra("InStock", onHandNum)
                    intent.putExtra("Comment", comment)
                    intent.putExtra("Row", row)
                    intent.putExtra("FromActivity", whichTabStr)

                    this.startActivity(intent)

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

    @SuppressLint("InflateParams")
    fun addTab(item: MenuItem) {
        val view: View = layoutInflater.inflate(R.layout.dialog_edit_text, null);
        val etTitle = view.findViewById<View>(R.id.et_title) as EditText
        val dialogBuilder = AlertDialog.Builder(this)

        dialogBuilder
            .setMessage("Enter title of tab:")
            .setCancelable(true)
            .setView(view)
            .setPositiveButton("OK", DialogInterface.OnClickListener {
                    dialog, _ ->
                val emptyStringArray: MutableList<String> = ArrayList()
                InventoryTabs.add(mutableListOf(emptyStringArray))
                for (i in InventoryTabs){
                    if (i.size > 0 && i[0].size < 1) i.removeAt(0)
                }
                val inputTitleStr: String = etTitle.text.toString()
                TabTitles.add(inputTitleStr)
                setTabs()
                val tabLayout = tabs as TabLayout//Go to appropriate tab...
                val tab = tabLayout.getTabAt(TabTitles.size - 1)
                tab?.select()
                CallServer(this).makeCall(
                    coordinator_layout,//View
                    ipAddressStr,//IP Address
                    "newTab",//Reason
                    "none",
                    "none",
                    "none",
                    TabTitles.size.toString(),
                    "2",
                    "false",
                    inputTitleStr,
                    "none",
                    "none"
                )
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, _ -> dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle("New Tab")
        alert.show()
    }

    fun pasteItem(item: MenuItem) {
        if (!globalItemOnClipboard){//nothing on clipboard, show dialog
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder
                .setMessage("No item on clipboard. Long-press an item to copy.")
                .setCancelable(true)
                .setPositiveButton("OK", DialogInterface.OnClickListener {
                    dialog, _ -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Item Not Found")
            alert.show()
            return
        }
        currentTab = tabs.selectedTabPosition
        Log.d(TAG, currentTab.toString())
        CallServer(this).makeCall(
            coordinator_layout,
            ipAddressStr,
            "addItem",
            globalStockCount,
            globalPartNumber,
            globalImageIndex,
            (currentTab + 1).toString(),
            "2",
            "false",
            globalItemName,
            globalMinStockLevel,
            globalCommentStr
        )
        globalItemOnClipboard = false
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
                InventoryTabs,
                TabTitles,
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
        val titles = respObj["0"].toString().split("\",\"").toMutableList()//Get tab titles from first array element
        titles[0] = titles[0].substring(2)
        titles[titles.size - 1] = titles[titles.size - 1].substring(0, titles[titles.size - 1].length - 2)

        for (s in 0 until titles.size){
            TabTitles.add(titles[s])
            titles[s] = titles[s].trim()
        }
        EditItemActivity.SendReceiveTabNames(TabTitles)
        AddItemActivity.SendReceiveTabNames(TabTitles)
        DeleteItemActivity.SendReceiveTabNames(TabTitles)

        val sheet1 = JSONObject(respObj["1"].toString())
        var sheet2 = JSONObject(respObj["1"].toString())
        var sheet3 = JSONObject(respObj["1"].toString())
        var sheet4 = JSONObject(respObj["1"].toString())
        var sheet5 = JSONObject(respObj["1"].toString())
        var sheet6 = JSONObject(respObj["1"].toString())
        var sheet7 = JSONObject(respObj["1"].toString())
        var sheet8 = JSONObject(respObj["1"].toString())
        var sheet9 = JSONObject(respObj["1"].toString())
        var sheet10 = JSONObject(respObj["1"].toString())
        var sheet11 = JSONObject(respObj["1"].toString())
        var sheet12 = JSONObject(respObj["1"].toString())

        if (respObj.length() > 12) sheet12 = JSONObject(respObj["12"].toString())
        if (respObj.length() > 11) sheet11 = JSONObject(respObj["11"].toString())
        if (respObj.length() > 10) sheet10 = JSONObject(respObj["10"].toString())
        if (respObj.length() > 9 ) sheet9 = JSONObject(respObj["9"].toString())
        if (respObj.length() > 8 ) sheet8 = JSONObject(respObj["8"].toString())
        if (respObj.length() > 7 ) sheet7 = JSONObject(respObj["7"].toString())
        if (respObj.length() > 6 ) sheet6 = JSONObject(respObj["6"].toString())
        if (respObj.length() > 5 ) sheet5 = JSONObject(respObj["5"].toString())
        if (respObj.length() > 4 ) sheet4 = JSONObject(respObj["4"].toString())
        if (respObj.length() > 3 ) sheet3 = JSONObject(respObj["3"].toString())
        if (respObj.length() > 2 ) sheet2 = JSONObject(respObj["2"].toString())

        if (respObj.length() > 4) {
            tabs.tabMode = TabLayout.MODE_SCROLLABLE
        }else{
            tabs.tabMode = TabLayout.MODE_FIXED
        }

        for (i in 2 until sheet1.length() + 1){
            val str1 = JSONObject(sheet1[i.toString()].toString())["A"].toString()//Column A: Image
            val str2 = JSONObject(sheet1[i.toString()].toString())["B"].toString()//Column B: Part Number
            val str3 = JSONObject(sheet1[i.toString()].toString())["C"].toString()//Column C: Description
            val str4 = JSONObject(sheet1[i.toString()].toString())["D"].toString()//Column D: Min Stock Level
            val str5 = JSONObject(sheet1[i.toString()].toString())["E"].toString()//Column E: Quantity on Hand
            val str6 = JSONObject(sheet1[i.toString()].toString())["F"].toString()//Column F: Comments
            val str7 = i.toString()//Row in Excel sheet
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
            globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "1"))
        }
        var arrayCopy = InventoryItems.toTypedArray()
        InventoryTabs.add(arrayCopy.toMutableList())
        InventoryItems.clear()

        if (respObj.length() > 2) {
            for (i in 2 until sheet2.length() + 1) {
                val str1 = JSONObject(sheet2[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet2[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet2[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet2[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet2[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet2[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "2"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 3) {
            for (i in 2 until sheet3.length() + 1) {
                val str1 = JSONObject(sheet3[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet3[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet3[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet3[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet3[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet3[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "3"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 4) {
            for (i in 2 until sheet4.length() + 1) {
                val str1 = JSONObject(sheet4[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet4[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet4[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet4[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet4[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet4[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "4"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 5) {
            for (i in 2 until sheet5.length() + 1) {
                val str1 = JSONObject(sheet5[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet5[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet5[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet5[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet5[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet5[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "5"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 6) {
            for (i in 2 until sheet6.length() + 1) {
                val str1 = JSONObject(sheet6[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet6[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet6[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet6[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet6[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet6[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "6"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 7) {
            for (i in 2 until sheet7.length() + 1) {
                val str1 = JSONObject(sheet7[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet7[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet7[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet7[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet7[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet7[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "7"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 8) {
            for (i in 2 until sheet8.length() + 1) {
                val str1 = JSONObject(sheet8[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet8[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet8[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet8[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet8[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet8[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "8"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 9) {
            for (i in 2 until sheet9.length() + 1) {
                val str1 = JSONObject(sheet9[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet9[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet9[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet9[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet9[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet9[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "9"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 10) {
            for (i in 2 until sheet10.length() + 1) {
                val str1 = JSONObject(sheet10[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet10[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet10[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet10[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet10[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet10[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "10"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 11) {
            for (i in 2 until sheet11.length() + 1) {
                val str1 = JSONObject(sheet11[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet11[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet11[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet11[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet11[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet11[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "11"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }

        if (respObj.length() > 12) {
            for (i in 2 until sheet12.length() + 1) {
                val str1 = JSONObject(sheet12[i.toString()].toString())["A"].toString()
                val str2 = JSONObject(sheet12[i.toString()].toString())["B"].toString()
                val str3 = JSONObject(sheet12[i.toString()].toString())["C"].toString()
                val str4 = JSONObject(sheet12[i.toString()].toString())["D"].toString()
                val str5 = JSONObject(sheet12[i.toString()].toString())["E"].toString()
                val str6 = JSONObject(sheet12[i.toString()].toString())["F"].toString()
                val str7 = i.toString()
                InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7))
                globalDataArray.add(mutableListOf(str1, str2, str3, str4, str5, str6, str7, "12"))
            }
            arrayCopy = InventoryItems.toTypedArray()
            InventoryTabs.add(arrayCopy.toMutableList())
            InventoryItems.clear()
        }
        Log.d(TAG, globalDataArray.toString())
    }
}
