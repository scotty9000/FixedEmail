package com.example.report_it

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.report_it.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)

        // get shared data
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val address = pref.getString("Address","")
        val subject = pref.getString("Subject","")
        val body = pref.getString("Body","")
        binding.editAddress.setText(address)
        binding.editSubject.setText(subject)
        binding.editBody.setText(body)

        emailFocusListener()
    }

    private fun emailFocusListener() {
        binding.editAddress.setOnFocusChangeListener{_, focused ->
            if(!focused){
                binding.addressContainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailAddress = binding.editAddress.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            return "Invalid Email Address"
        }
        return null
    }

    // save email Fields to shared preferences file
    // and return to MainActivity
    fun onSave(view: View) {

        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString("Address", binding.editAddress.text.toString())
        editor.putString("Subject", binding.editSubject.text.toString())
        editor.putString("Body", binding.editBody.text.toString())
        editor.commit()

        val intent = Intent(this, MainActivity::class.java)
        Log.d("EditActivity", "SAVE pressed")
        startActivity(intent)
    }
}
