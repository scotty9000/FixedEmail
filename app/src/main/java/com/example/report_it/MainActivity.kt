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
import android.provider.AlarmClock.EXTRA_MESSAGE
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
    private val emailFields: EmailFields = EmailFields()
    private lateinit var emailAddress: String
    private lateinit var emailSubject: String
    private lateinit var emailBody: String

    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var gpsLong : String
    private lateinit var gpsLat : String

    lateinit var imageFilePath: String
    lateinit var uriForImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        getEmailFields() // from Shared Preferences File, displayed by xml binding

        // short button presses invoke intents
        binding.button1.setOnClickListener {
            emailAddress = emailFields.address1
            emailSubject = emailFields.subject1
            emailBody = emailFields.body1
            do_all()
        }

        binding.button2.setOnClickListener {
            emailAddress = emailFields.address2
            emailSubject = emailFields.subject2
            emailBody = emailFields.body2
            do_all()
        }

        binding.button3.setOnClickListener {
            emailAddress = emailFields.address3
            emailSubject = emailFields.subject3
            emailBody = emailFields.body3
            do_all()
        }

        binding.button4.setOnClickListener {
            emailAddress = emailFields.address4
            emailSubject = emailFields.subject4
            emailBody = emailFields.body4
            do_all()
        }

        binding.button1.setOnLongClickListener{
            edit(1)
            true
        }

        // long button presses invoke editing
        binding.button2.setOnLongClickListener{
            edit(2)
            true
        }

        binding.button3.setOnLongClickListener{
            edit(3)
            true
        }

        binding.button4.setOnLongClickListener{
            edit(4)
            true
        }
    }

    private fun edit(btn :Int) {
        val intent = Intent(this, EditActivity::class.java).apply{putExtra("btn", btn)}
        startActivity(intent)
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
            getLocation()
            } else {
            Toast.makeText(this, "Camera Intent RESULT NOT OK", Toast.LENGTH_LONG).show()
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
        return imageFile
    }

    private fun getImageUri() {
        try {
            val imageFile = createImageFile()
            val authorities = packageName + ".fileprovider"
            uriForImage = FileProvider.getUriForFile(this, authorities, imageFile)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not create file!", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendEmail() {
        Toast.makeText(this, "choose your email app  ...", Toast.LENGTH_LONG).show()
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        val to = arrayOf(emailAddress)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        emailIntent.putExtra(Intent.EXTRA_STREAM, uriForImage)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody  + "\n" + "Latitude = " + gpsLat + "\n" + "Longitude = " + gpsLong)
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } else {
            Toast.makeText(this, " Sorry, cannot find an email app on this device ", Toast.LENGTH_LONG).show()
        }
    }


    private fun getLocation() {
        Toast.makeText(this, "getting location ...", Toast.LENGTH_LONG).show()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }

        if (SDK_INT >= android.os.Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                LocationManager.GPS_PROVIDER,
                null,
                this.mainExecutor,
                locationCallback
            )
        } else {
            Toast.makeText(this, " location is NULL ", Toast.LENGTH_LONG).show()
        }
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
        emailFields.address1 = pref.getString("Address1", "").toString()
        emailFields.subject1 = pref.getString("Subject1", "").toString()
        emailFields.body1 = pref.getString("Body1", "").toString()

        emailFields.address2 = pref.getString("Address2", "").toString()
        emailFields.subject2 = pref.getString("Subject2", "").toString()
        emailFields.body2 = pref.getString("Body2", "").toString()

        emailFields.address3 = pref.getString("Address3", "").toString()
        emailFields.subject3 = pref.getString("Subject3", "").toString()
        emailFields.body3 = pref.getString("Body3", "").toString()

        emailFields.address4 = pref.getString("Address4", "").toString()
        emailFields.subject4 = pref.getString("Subject4", "").toString()
        emailFields.body4 = pref.getString("Body4", "").toString()

        binding.emailFields = emailFields
    }
}