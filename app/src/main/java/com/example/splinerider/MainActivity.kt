package com.example.splinerider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<GameView>(R.id.gameView).setGameLevel(1)
    }

    fun clearSave(view: View? = null){
        findViewById<GameView>(R.id.gameView).setGameLevel(0)
    }

}