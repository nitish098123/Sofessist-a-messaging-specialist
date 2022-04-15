package com.example.database_part_3.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.R
import com.example.database_part_3.message_holder.chat_activity

class welcome : AppCompatActivity() {
    private lateinit var  new_ : Button
    private lateinit var existing_ : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_welcome)

         new_ = findViewById(R.id.create_account_section)
        existing_ = findViewById(R.id.login_id)

        new_.setOnClickListener {
            val text_ = "new_account"
            val intent = Intent(this,posting_data::class.java)
            intent.putExtra("_section",text_)
            startActivity(intent)
        }

        existing_.setOnClickListener {
            val text_ = "already_account"
            val intent = Intent(this,posting_data::class.java)
            intent.putExtra("_section",text_)
            startActivity(intent)
        }

    }
}