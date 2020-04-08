package com.baked.inscriptainventory.Resource

//Required for ZXing barcode scanning
import androidx.multidex.MultiDexApplication

class KtApplication: MultiDexApplication(){
    override fun onCreate() {
        super.onCreate()
    }
}