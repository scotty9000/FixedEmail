package com.ulogic.fixed_email

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.ulogic.fixed_email.databinding.ActivityMainBinding
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

        // long button presses invoke editing
        binding.button1.setOnLongClickListener{
            edit(1)
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
        cameraIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1048576L) // does not appear to have any effect
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
            Toast.makeText(this, " SDK error ", Toast.LENGTH_LONG).show()
        }
    }

    private val locationCallback = Consumer<Location> { location ->
        if (null != location) {
            gpsLat = location.latitude.toBigDecimal().toString()
            gpsLong = location.longitude.toBigDecimal().toString()

            var exif = ExifInterface(imageFilePath)
            exif.setLatLong (location.latitude, location.longitude)
            exif.saveAttributes()
            //compressImage()
            sendEmail()
        } else {
            Toast.makeText(this, " location is NULL, have you enabled GPS? ", Toast.LENGTH_LONG).show()
        }
    }

    private fun getEmailFields() {
        val pref = getSharedPreferences("EmailPrefs", Context.MODE_PRIVATE)
        emailFields.address1 = pref.getString("Address1", "").toString()
        emailFields.subject1 = pref.getString("Subject1", "").toString()
        emailFields.body1 = pref.getString("Body1", "").toString()

        binding.emailFields = emailFields
    }

//    private fun setPic() {
//        // Get the dimensions of the View
//        //val targetW: Int = imageView.width
//        //val targetH: Int = imageView.height
//
//        val targetW: Int = 800
//        val targetH: Int = 640
//
//        val bmOptions = BitmapFactory.Options().apply {
//            // Get the dimensions of the bitmap
//            inJustDecodeBounds = true
//
//            BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
//
//            val photoW: Int = outWidth
//            val photoH: Int = outHeight
//
//            // Determine how much to scale down the image
//            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))
//
//            // Decode the image file into a Bitmap sized to fill the View
//            inJustDecodeBounds = false
//            inSampleSize = scaleFactor
//            inPurgeable = true
//        }
//        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
//            imageView.setImageBitmap(bitmap)
//        }
//    }
//
//    private fun compressImage() {
//
//    }

}