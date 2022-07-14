package com.example.report_it

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.report_it.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        //setContentView(R.layout.activity_edit)

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        Log.d("EditActivity", "message =" + message.toString())
        // Capture the layout's TextView and set the string as its text
        //val textView = findViewById<TextView>(R.id.textView).apply {
            //text = message

        // get shared data
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val address = pref.getString("Address","")
        val subject = pref.getString("Subject","")
        val body = pref.getString("Body","")
        binding.editAddress.setText(address)
        binding.editSubject.setText(subject)
        binding.editBody.setText(body)



       // }


    }

    fun onClear(view:View) {
        // clear email fields
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.commit()

        binding.editAddress.setText("")
        binding.editSubject.setText("")
        binding.editBody.setText("")
    }

    // create a shared preferences file
    // and save name and ID as values/pairs in file
    fun onSave(view: View) {
        // save email Fields
        val pref = getSharedPreferences("EmailPrefs",Context.MODE_PRIVATE)
        val editor = pref.edit()

        editor.putString("Address", binding.editAddress.text.toString())
        editor.putString("Subject", binding.editSubject.text.toString())
        editor.putString("Body", binding.editBody.text.toString())
        editor.commit()
        val toast = Toast.makeText(applicationContext, "saved", Toast.LENGTH_LONG)
        toast.show()

    }
}
