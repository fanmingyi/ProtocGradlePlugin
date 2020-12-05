package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        com.example.tutorial.AddressBookProtos
        Log.e("MainActivity", "测试  " + com.example.tutorial.AddressBookProtos::class.java)
//        .
//        com.example.tutorial.
//        val build = AddressBookProtos.Builder().build()
    }
}