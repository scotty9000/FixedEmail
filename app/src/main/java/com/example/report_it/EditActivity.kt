package com.example.report_it

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // Get the Intent that started this activity and extract the string
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        Log.d("EditActivity", "message =" + message.toString())
        // Capture the layout's TextView and set the string as its text
        val textView = findViewById<TextView>(R.id.textView).apply {
            text = message

        // Shared Preference
        val etname = findViewById<EditText>(R.id.name)
        val etaddress = findViewById<EditText>(R.id.address)
        val save = findViewById<EditText>(R.id.saveButton)
        val get = findViewById<EditText>(R.id.getButton)
        val clear = findViewById<EditText>(R.id.clearButton)
        val delete = findViewById<EditText>(R.id.deleteButton)
        val sharedPref = getSharedPreferences("addName", Context.MODE_PRIVATE)
        var edit = sharedPref.edit()

        save.setOnClickListener{
            edit.putString("name",etname.text.toString())
            edit.putString("address",etaddress.text.toString())
            edit.commit()
            Toast.makeText(this@EditActivity,"data saved", Toast.LENGTH_SHORT).show()
        }

        get.setOnClickListener{
            val name = sharedPref.getString("name", "default value")
            val address = sharedPref.getString("address", "default value")
            Toast.makeText(this@EditActivity,name+"  address: "+address, Toast.LENGTH_SHORT).show()
        }

        clear.setOnClickListener{
            edit.remove("address")
            edit.commit()
        }
        delete.setOnClickListener{
            edit.clear()
            edit.commit()
        }

        }
    }
}