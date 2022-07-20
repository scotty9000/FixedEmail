package com.example.report_it

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
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

        val btn = intent.getIntExtra("btn", 0)
        Toast.makeText(this, btn.toString(), Toast.LENGTH_LONG).show()

        // get shared data
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        var address = pref.getString("Address1","x")
        var subject = pref.getString("Subject1","x")
        var body = pref.getString("Body1","x")

        binding.saveButton.setOnClickListener {
            save(btn)
        }

        when (btn) {
            1-> {
                address = pref.getString("Address1","x1")
                subject = pref.getString("Subject1","x1")
                body = pref.getString("Body1","x1")
            }

            2-> {
                address = pref.getString("Address2","x2")
                subject = pref.getString("Subject2","x2")
                body = pref.getString("Body2","x2")
            }

            3-> {
                address = pref.getString("Address3","x3")
                subject = pref.getString("Subject3","x3")
                body = pref.getString("Body3","x3")
            }

            4-> {
                address = pref.getString("Address4","x4")
                subject = pref.getString("Subject4","x4")
                body = pref.getString("Body4","x4")
            }

            else -> {
                Toast.makeText(this, "InValid Btn !!??", Toast.LENGTH_LONG).show()
            }



        }
//        val address = pref.getString("Address","")
//        val subject = pref.getString("Subject","")
//        val body = pref.getString("Body","")
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
    fun save(btn: Int) {

        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val editor = pref.edit()

        when (btn) {
            1 -> {
                editor.putString("Address1", binding.editAddress.text.toString())
                editor.putString("Subject1", binding.editSubject.text.toString())
                editor.putString("Body1", binding.editBody.text.toString())
                editor.commit()
                Log.d("EditActivity", "save(1)")
            }

            2 -> {
                editor.putString("Address2", binding.editAddress.text.toString())
                editor.putString("Subject2", binding.editSubject.text.toString())
                editor.putString("Body2", binding.editBody.text.toString())
                editor.commit()
            }

            3 -> {
                editor.putString("Address3", binding.editAddress.text.toString())
                editor.putString("Subject3", binding.editSubject.text.toString())
                editor.putString("Body3", binding.editBody.text.toString())
                editor.commit()
            }

            4 -> {
                editor.putString("Address4", binding.editAddress.text.toString())
                editor.putString("Subject4", binding.editSubject.text.toString())
                editor.putString("Body4", binding.editBody.text.toString())
                editor.commit()
            }

            else -> {
                Toast.makeText(this, "InValid Btn !!??", Toast.LENGTH_LONG).show()
                Log.d("EditActivity", "Invalid Button in save()")
            }
        }



        val intent = Intent(this, MainActivity::class.java)
        Log.d("EditActivity", "SAVE pressed")
        startActivity(intent)
    }
}
