package com.example.report_it

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.report_it.databinding.ActivityMainBinding
import java.io.File
import java.io.File.createTempFile
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var photoImageView : ImageView
    private lateinit var button2 : Button

    lateinit var imageFilePath: String

    lateinit var uriForImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button2 = findViewById(R.id.button2)

        photoImageView = findViewById(R.id.photoImageView)

//        button1.setOnClickListener {
////            val emailIntent = Intent(Intent.ACTION_SENDTO,
////            Uri.fromParts("mailto", "scotty9000@gmail.com", null))
////            startActivity(Intent.createChooser(emailIntent, "Send email..."))
//            val emailIntent = Intent(Intent.ACTION_SEND)
//            emailIntent.type = "message/rfc822"
//            val to = arrayOf("scotty9000@gmail.com")
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
//            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//            emailIntent.putExtra(Intent.EXTRA_STREAM, uriForImage)
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "DogShit")
//            emailIntent.putExtra(Intent.EXTRA_TEXT, "attached photo has location embedded")
//            startActivity(Intent.createChooser(emailIntent, "Send email..."))
//        }


        button2.setOnClickListener {
            try {
                val imageFile = createImageFile()
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val authorities = packageName + ".fileprovider"
                val imageUri = FileProvider.getUriForFile(this, authorities, imageFile)
                Log.d("MainActivity", "imageUri =" + imageUri.toString())
                uriForImage = imageUri
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                resultLauncher.launch(cameraIntent)
            } catch(e: Exception) {
                Toast.makeText(this,"Could not create file!", Toast.LENGTH_LONG).show()
            }

        }
    }

    private val resultLauncher  = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "result is ok", Toast.LENGTH_LONG).show()
            photoImageView.setImageBitmap(setScaledBitmap())
            sendEmail()
            }
        }


    @Throws(IOException::class)
    fun createImageFile(): File {
        val timestamp = SimpleDateFormat("HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timestamp +"_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        Log.d("MainActivity", imageFilePath )
        return imageFile
    }

    private fun setScaledBitmap(): Bitmap {
        val imageViewWidth = photoImageView.width
        val imageViewHeight = photoImageView.height

        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFilePath, bmOptions)
        val bitmapWidth = bmOptions.outWidth
        val bitmapHeight = bmOptions.outHeight

        val scaleFactor = Math.min(bitmapWidth/imageViewWidth, bitmapHeight/imageViewHeight)
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(imageFilePath, bmOptions)
    }

    private fun sendEmail() {
        val emailSelectorIntent = Intent(Intent.ACTION_SENDTO)
        emailSelectorIntent.setData(Uri.parse("mailto:"))
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        val to = arrayOf("scotty9000@gmail.com")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        emailIntent.putExtra(Intent.EXTRA_STREAM, uriForImage)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "DogShit")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "attached photo has location embedded")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}