package com.baked.inscriptainventory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
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
        callServer(view_pager)

        scan.setOnClickListener {
            run {
                IntentIntegrator(this@MainActivity).initiateScan()
            }
        }
        catalog.setOnClickListener {
            //todo
        }
    }

    private fun callServer(view: View){
//        ipAddressStr = "10.0.0.225"
        val stateStr = sharedPrefs!!.getString(initialStateName, String.toString())
        ipAddressStr = sharedPrefs!!.getString(ipAddressName, String.toString()).toString()
        val urlStr = "http://$ipAddressStr:80/index.php"
        val request = Request.Builder()
            .url(urlStr)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(view, "No server response: functionality will be limited", Snackbar.LENGTH_LONG)
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

    private fun setTabs(){
        val sectionsPagerAdapter = SectionsPagerAdapter(InventoryItems, this@MainActivity, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

            //QR Code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedResult = result.contents
//                    txtValue.text = scannedResult
            } else {
//                    txtValue.text = "scan failed"
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "yep")
        menu.add("Test2")
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
        Log.d(TAG, "here")//This does nothing, use settingsClicked()
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

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }

    //Parse Excel Json object returned from server:
    private fun parseJsonStr(responseStr: String) {
//        Log.d(TAG, responseStr)
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
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6))
        }
        for (i in 2 until sheet2.length() + 1){
            val str1 = JSONObject(sheet2[i.toString()].toString())["A"].toString()
            val str2 = JSONObject(sheet2[i.toString()].toString())["B"].toString()
            val str3 = JSONObject(sheet2[i.toString()].toString())["C"].toString()
            val str4 = JSONObject(sheet2[i.toString()].toString())["D"].toString()
            val str5 = JSONObject(sheet2[i.toString()].toString())["E"].toString()
            val str6 = JSONObject(sheet2[i.toString()].toString())["F"].toString()
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6))
        }
        for (i in 2 until sheet3.length() + 1){
            val str1 = JSONObject(sheet3[i.toString()].toString())["A"].toString()
            val str2 = JSONObject(sheet3[i.toString()].toString())["B"].toString()
            val str3 = JSONObject(sheet3[i.toString()].toString())["C"].toString()
            val str4 = JSONObject(sheet3[i.toString()].toString())["D"].toString()
            val str5 = JSONObject(sheet3[i.toString()].toString())["E"].toString()
            val str6 = JSONObject(sheet3[i.toString()].toString())["F"].toString()
            InventoryItems.add(mutableListOf(str1, str2, str3, str4, str5, str6))
        }
    }
}


