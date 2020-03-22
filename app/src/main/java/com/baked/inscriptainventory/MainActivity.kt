package com.baked.inscriptainventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.content.Intent
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import android.util.Log

var InventoryItems: ArrayList<String> = ArrayList()

class MainActivity : AppCompatActivity() {
    private var scannedResult: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
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
            //todo
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                scannedResult = result.contents
//                    txtValue.text = scannedResult
                Log.d(scannedResult, "InsInv")
            } else {
//                    txtValue.text = "scan failed"
                Log.d(scannedResult, "InsInv")
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
}


