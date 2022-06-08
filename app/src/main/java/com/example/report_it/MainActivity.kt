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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE
import androidx.exifinterface.media.ExifInterface.TAG_MODEL
import java.io.File
import java.io.File.createTempFile
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private lateinit var gpsLong : String
    private lateinit var gpsLat : String

    private lateinit var button1 : Button

    lateinit var imageFilePath: String

    lateinit var uriForImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button1 = findViewById(R.id.button1)

        button1.setOnClickListener {
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

            getLocation()
            //getExifInfo()
            //sendEmail()
            } else {
            Toast.makeText(this, "result is bad", Toast.LENGTH_LONG).show()
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
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Latitude = " + gpsLat + "Longitude = " + gpsLong)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    private fun getExifInfo() {

        Toast.makeText(this, "in getExifInfo", Toast.LENGTH_LONG).show()


        try {
            var exif = ExifInterface(imageFilePath)

            val modelTag = exif.getAttribute(TAG_MODEL)
            val gpsLongTag = exif.getAttribute(TAG_GPS_LONGITUDE)
            if (modelTag != null)
                Log.d("MainActivity", "exif modelTag = " + modelTag)
            if (gpsLongTag != null)
                Log.d("MainActivity", "exif gpsLongTag = " + gpsLongTag)
            else
                Log.d("MainActivity", "gpsLongTag is NULL")

        } catch (e: Exception) {
            Toast.makeText(this, "exif fail", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        if (SDK_INT >= android.os.Build.VERSION_CODES.R)
        locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, this.mainExecutor, locationCallback)
    }

    override fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.textView)
        gpsLat = location.latitude.toBigDecimal().toString()
        gpsLong = location.longitude.toBigDecimal().toString()
        tvGpsLocation.text = "Latitude: " + gpsLat + " , Longitude: " + gpsLong
    }

    private val locationCallback = Consumer<Location> { location ->
        if (null != location) {
            tvGpsLocation = findViewById(R.id.textView)
            gpsLat = location.latitude.toBigDecimal().toString()
            gpsLong = location.longitude.toBigDecimal().toString()
            tvGpsLocation.text = "Latitude: " + gpsLat + " , Longitude: " + gpsLong

            var exif = ExifInterface(imageFilePath)
            exif.setLatLong (location.latitude, location.longitude)
            exif.saveAttributes()
            sendEmail()
        }
    }
}