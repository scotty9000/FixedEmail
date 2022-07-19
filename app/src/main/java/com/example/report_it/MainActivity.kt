package com.example.report_it

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.UniversalTimeScale.toBigDecimal
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.databinding.DataBindingUtil
import androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE
import androidx.exifinterface.media.ExifInterface.TAG_MODEL
import com.example.report_it.databinding.ActivityMainBinding
import java.io.File
import java.io.File.createTempFile
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val emailFields: EmailFields = EmailFields("",
        "",
        ""
    )

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var gpsLong : String
    private lateinit var gpsLat : String

    lateinit var imageFilePath: String

    lateinit var uriForImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        getEmailFields() // from Shared Preferences File

        binding.button1.setOnClickListener {
            do_all()
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            Log.d("MainActivity", "EDIT pressed")
            startActivity(intent)
        }
    }

    private fun do_all() {
        getImageUri()

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForImage)
        resultLauncher.launch(cameraIntent)
    }

    private val resultLauncher  = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Log.d("MainActivity", "Camera Intent RESULT OK")

            getLocation()

            } else {
            Log.d("MainActivity", "Camera Intent RESULT NOT OK")
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timestamp = SimpleDateFormat("HHmmss", Locale.US).format(Date())
        val imageFileName = timestamp +"_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if(!storageDir!!.exists()) storageDir.mkdirs()
        val imageFile = createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = imageFile.absolutePath
        Log.d("MainActivity", imageFilePath )
        return imageFile
    }

    private fun getImageUri() {
        try {
            val imageFile = createImageFile()
            val authorities = packageName + ".fileprovider"
            uriForImage = FileProvider.getUriForFile(this, authorities, imageFile)
            Log.d("MainActivity", "imageUri =" + uriForImage.toString())
        } catch (e: Exception) {
            Toast.makeText(this, "Could not create file!", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendEmail() {
        Toast.makeText(this, "sending email ...", Toast.LENGTH_LONG).show()
        val emailSelectorIntent = Intent(Intent.ACTION_SENDTO)
        emailSelectorIntent.setData(Uri.parse("mailto:"))
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        val to = arrayOf(emailFields.address)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        emailIntent.putExtra(Intent.EXTRA_STREAM, uriForImage)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailFields.subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailFields.body  + "\n" + "Latitude = " + gpsLat + "\n" + "Longitude = " + gpsLong)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }


    private fun getLocation() {
        Toast.makeText(this, "getting location ...", Toast.LENGTH_LONG).show()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }

        if (SDK_INT >= android.os.Build.VERSION_CODES.R)
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, this.mainExecutor, locationCallback)
    }


    private val locationCallback = Consumer<Location> { location ->
        if (null != location) {
            gpsLat = location.latitude.toBigDecimal().toString()
            gpsLong = location.longitude.toBigDecimal().toString()

            var exif = ExifInterface(imageFilePath)
            exif.setLatLong (location.latitude, location.longitude)
            exif.saveAttributes()
            sendEmail()
        } else {
            Toast.makeText(this, " location is NULL ", Toast.LENGTH_LONG).show()
        }
    }

    private fun getEmailFields() {
        val pref = getSharedPreferences("EmailPrefs", Context.MODE_PRIVATE)
        emailFields.address = pref.getString("Address", "").toString()
        emailFields.subject = pref.getString("Subject", "").toString()
        emailFields.body = pref.getString("Body", "").toString()
        binding.emailFields = emailFields
        Log.d("MainActivity", emailFields.address )
    }
}