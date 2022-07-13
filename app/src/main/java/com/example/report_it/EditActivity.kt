package com.example.report_it

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
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

        // create shared preferences file
        val pref = getPreferences(Context.MODE_PRIVATE)
        val name = pref.getString("NAME", "")
        val id = pref.getInt("ID", 0)
        binding.editName.setText(name)
        binding.editID.setText(id.toString())



       // }


    }

    fun onClear(view: View) {
        // clear name & ID
        val pref = getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.commit()

        binding.editID.setText("0")
        binding.editName.setText("")
    }

    // create a shared preferences file
    // and save name and ID as values/pairs in file
    fun onSave(view: View) {
        // save name & ID
        val pref = getPreferences(Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("NAME", binding.editName.text.toString())
        editor.putInt("ID", binding.editID.text.toString().toInt())
        editor.commit()
        val toast = Toast.makeText(applicationContext, "saved", Toast.LENGTH_LONG)
        toast.show()

    }
}
