package com.baked.inscriptainventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.content.Intent
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.io.IOException
import okhttp3.*
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log


class MainActivity(private var InventoryItems: ArrayList<String> = ArrayList()) : AppCompatActivity() {
    private var ipAddressStr = ""
    private val client = OkHttpClient()
    private var scannedResult: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addItems()
        val sectionsPagerAdapter = SectionsPagerAdapter(InventoryItems, this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

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

    fun run(){
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
//                        txtValue.text = resp
Log.d("InscriptaInventory", resp)

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
        InventoryItems.add("dog")
        InventoryItems.add("cat")
        InventoryItems.add("owl")
        InventoryItems.add("cheetah")
        InventoryItems.add("raccoon")
        InventoryItems.add("bird")
        InventoryItems.add("snake")
        InventoryItems.add("lizard")
        InventoryItems.add("hamster")
        InventoryItems.add("bear")
        InventoryItems.add("lion")
        InventoryItems.add("tiger")
        InventoryItems.add("horse")
        InventoryItems.add("frog")
        InventoryItems.add("fish")
        InventoryItems.add("shark")
        InventoryItems.add("turtle")
        InventoryItems.add("elephant")
        InventoryItems.add("cow")
        InventoryItems.add("beaver")
        InventoryItems.add("bison")
        InventoryItems.add("porcupine")
        InventoryItems.add("rat")
        InventoryItems.add("mouse")
        InventoryItems.add("goose")
        InventoryItems.add("deer")
        InventoryItems.add("fox")
        InventoryItems.add("moose")
        InventoryItems.add("buffalo")
        InventoryItems.add("monkey")
        InventoryItems.add("penguin")
        InventoryItems.add("parrot")

//    recyclerView.adapter?.notifyDataSetChanged()

    }

}


