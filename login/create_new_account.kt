package com.example.database_part_3.login

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.R


// for setting dp , username , about
class create_new_account : AppCompatActivity() {
    private lateinit var number_ : EditText
    private lateinit var password : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_new_account_dp)


    }
}