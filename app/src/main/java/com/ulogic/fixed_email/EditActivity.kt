package com.ulogic.fixed_email

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.ulogic.fixed_email.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        val btn = intent.getIntExtra("btn", 0)

        // get shared data
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        var subject = pref.getString("Subject1","")
        var body = pref.getString("Body1","")

        binding.saveButton.setOnClickListener {
            save(btn)
        }

        when (btn) {
            1-> {
                subject = pref.getString("Subject1","")
                body = pref.getString("Body1","")
            }

            else -> {
                Toast.makeText(this, "InValid Btn !!??", Toast.LENGTH_LONG).show()
            }
        }
        // display the text
        binding.editSubject.setText(subject)
        binding.editBody.setText(body)
    }


    // save email Fields to shared preferences file
    // and return to MainActivity
    fun save(btn: Int) {

        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val editor = pref.edit()

        when (btn) {
            1 -> {
                editor.putString("Subject1", binding.editSubject.text.toString())
                editor.putString("Body1", binding.editBody.text.toString())
                editor.commit()
            }

            else -> {
                Toast.makeText(this, "InValid Btn !!??", Toast.LENGTH_LONG).show()
            }
        }


        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
