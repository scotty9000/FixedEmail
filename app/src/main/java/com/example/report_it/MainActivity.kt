package com.example.report_it

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.report_it.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var button1 : Button

    val REQUEST_CODE = 2000
    private lateinit var imageView : ImageView
    private lateinit var button2 : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)

        button1.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", "scotty9000@gmail.com", null))
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        button2.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(cameraIntent)
        }
    }

    private val resultLauncher  = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
        if(result.resultCode == Activity.RESULT_OK) {
            if(result.data == null) {
                Toast.makeText(this, "data is null", Toast.LENGTH_SHORT).show()
            }else {
                //imageView.setImageBitmap(result.data!!.extras!!.get("data") as Bitmap)
                Toast.makeText(this, "data is not null", Toast.LENGTH_SHORT).show()
            }
        }
    }


}