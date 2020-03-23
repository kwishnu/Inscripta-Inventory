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

//val seqList: MutableList<MutableList<Int>> = ArrayList()
class MainActivity(private var InventoryItems: MutableList<MutableList<String>> = ArrayList()) : AppCompatActivity() {
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
        InventoryItems.add(mutableListOf("0", "dog"))
        InventoryItems.add(mutableListOf("1","cat"))
        InventoryItems.add(mutableListOf("1","owl"))
        InventoryItems.add(mutableListOf("2","cheetah"))
        InventoryItems.add(mutableListOf("3","raccoon"))
        InventoryItems.add(mutableListOf("4","bird"))
        InventoryItems.add(mutableListOf("5","snake"))
        InventoryItems.add(mutableListOf("6","lizard"))
        InventoryItems.add(mutableListOf("0","hamster"))
        InventoryItems.add(mutableListOf("0","bear"))
        InventoryItems.add(mutableListOf("0","lion"))
        InventoryItems.add(mutableListOf("0","tiger"))
        InventoryItems.add(mutableListOf("0","horse"))
        InventoryItems.add(mutableListOf("0","frog"))
        InventoryItems.add(mutableListOf("0","fish"))
        InventoryItems.add(mutableListOf("0","shark"))
        InventoryItems.add(mutableListOf("0","turtle"))
        InventoryItems.add(mutableListOf("0","elephant"))
        InventoryItems.add(mutableListOf("0","cow"))
        InventoryItems.add(mutableListOf("0","beaver"))
        InventoryItems.add(mutableListOf("0","bison"))
        InventoryItems.add(mutableListOf("0","porcupine"))
        InventoryItems.add(mutableListOf("0","rat"))
        InventoryItems.add(mutableListOf("0","mouse"))
        InventoryItems.add(mutableListOf("0","goose"))
        InventoryItems.add(mutableListOf("0","deer"))
        InventoryItems.add(mutableListOf("0","fox"))
        InventoryItems.add(mutableListOf("0","moose"))
        InventoryItems.add(mutableListOf("0","buffalo"))
        InventoryItems.add(mutableListOf("0","monkey"))
        InventoryItems.add(mutableListOf("0","penguin"))
        InventoryItems.add(mutableListOf("0","parrot"))

//    recyclerView.adapter?.notifyDataSetChanged()

    }

}


