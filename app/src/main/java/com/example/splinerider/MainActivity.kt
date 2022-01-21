package com.example.splinerider

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPref = getPreferences(MODE_PRIVATE)
        findViewById<GameView>(R.id.gameView).setSharedPreferences(sharedPref)
    }

    fun clearSave(view: View? = null) {
        sharedPref.edit().putInt("savedLevel", 1).apply()
        findViewById<GameView>(R.id.gameView).invalidate()
    }

}