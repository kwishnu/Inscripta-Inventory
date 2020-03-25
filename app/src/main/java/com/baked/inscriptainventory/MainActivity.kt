package com.baked.inscriptainventory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


//val seqList: MutableList<MutableList<Int>> = ArrayList()
class MainActivity(private var InventoryItems: MutableList<MutableList<String>> = ArrayList()) : AppCompatActivity() {
    private var ipAddressStr = ""
    private val client = OkHttpClient()
    private var scannedResult: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addItems()
        run()
//        val sectionsPagerAdapter = SectionsPagerAdapter(InventoryItems, this, supportFragmentManager)
//        val viewPager: ViewPager = findViewById(R.id.view_pager)
//        viewPager.adapter = sectionsPagerAdapter
//        val tabs: TabLayout = findViewById(R.id.tabs)
//        tabs.setupWithViewPager(viewPager)

        scan.setOnClickListener {
            run {
                IntentIntegrator(this@MainActivity).initiateScan()
            }
        }
        catalog.setOnClickListener {
            run()

            //todo
        }
    }

    private fun run(){
        ipAddressStr = "10.0.0.225"//ip_input.text.toString()
        val urlStr = "http://$ipAddressStr:80/index.php"
        val request = Request.Builder()
            .url(urlStr)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response){
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val resp = response.body!!.string()
//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }
//                    resp = response.body!!.string()
//                    println(resp)
//                    println(response.body!!.string())
//                    textView?.text ="test" //resp
                    this@MainActivity.runOnUiThread(Runnable {
                        val parts = resp.split("~|~")
//                        txtValue.text = resp
//                        Log.d("InscriptaInventory", parts[0])
//                        val responseData = resp.body().string()
                        val sheet1 = JSONObject(parts[0])
                        val sheet2 = JSONObject(parts[1])
                        val sheet3 = JSONObject(parts[2])
                        val headingsSheet1 = sheet1.getString("2")
                        val headingsSheet2 = sheet2.getString("2")
                        val headingsSheet3 = sheet3.getString("2")
                        Log.d("InscriptaInventory", headingsSheet1)
                        Log.d("InscriptaInventory", headingsSheet2)
                        Log.d("InscriptaInventory", headingsSheet3)

                        val sectionsPagerAdapter = SectionsPagerAdapter(InventoryItems, this@MainActivity, supportFragmentManager)
                        val viewPager: ViewPager = findViewById(R.id.view_pager)
                        viewPager.adapter = sectionsPagerAdapter
                        val tabs: TabLayout = findViewById(R.id.tabs)
                        tabs.setupWithViewPager(viewPager)

                    })
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedResult = result.contents
//                    txtValue.text = scannedResult
                Log.d("InsInv", scannedResult.toString())
            } else {
//                    txtValue.text = "scan failed"
                Log.d("InsInv", scannedResult.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addItems() {
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("1", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("1", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("2", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("3", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("4", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("5", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("6", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))
        InventoryItems.add(mutableListOf("0", "TB.STRIP02_1X90ML_YEB_Beta", "SubHeading", "X"))

//        viewPager.adapter?.notifyDataSetChanged()

    }

}


