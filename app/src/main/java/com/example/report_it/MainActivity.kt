package com.example.report_it

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import com.example.report_it.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var button1 : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button1)

        button1.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", "scotty9000@gmail.com", null))
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
    }
}