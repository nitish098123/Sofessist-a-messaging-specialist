package com.example.database_part_3.login

import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.MainActivity
import com.example.database_part_3.R
import com.fasterxml.jackson.core.JsonStreamContext
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.create_account.*
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.Dispatcher
import okio.ForwardingTimeout
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import kotlin.coroutines.suspendCoroutine

class posting_data : AppCompatActivity() {

    private lateinit var number_ : EditText
    private lateinit var password : EditText
    private lateinit var section_: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        create_account_button.setOnClickListener {   // this will send a post request to server
            postMethod()
        }

        section_ = intent.getStringExtra("_section").toString()
        if(section_=="new_account"){
             section_id_text_view.setText("Create New Account")
            password_entry_id_text_view.setText("Create new Password")
        }

        if(section_=="already_account"){
            section_id_text_view.setText("Existing Account")
            password_entry_id_text_view.setText("Old Password")
        }

        number_ = findViewById(R.id.get_mobile_number)
        password = findViewById(R.id.get_password)
    }

    private fun postMethod() {

        var retrofit: Retrofit = Retrofit.Builder().baseUrl("http://137.184.72.247:80/submit/").build()

        if (section_ == "new_account") {
            retrofit = Retrofit.Builder().baseUrl("http://137.184.72.247:80/submit/").build()
            section_id_text_view.setText("Create New Account")
            password_entry_id_text_view.setText("Create new Password")
        }

        if (section_ == "already_account") {
            retrofit = Retrofit.Builder().baseUrl("http://137.184.72.247:80/reset/").build()
            section_id_text_view.setText("Existing Account")
            password_entry_id_text_view.setText("Old Password")
        }

        val service = retrofit.create(APIService::class.java)
        // creating JSON object
        val jsonObject = JSONObject()
        jsonObject.put("number_", "${number_.text.toString()}")
        jsonObject.put("passwd_", "${password.text.toString()}")


        // convert json to string
        val str = jsonObject.toString()
        val requestBody = str.toRequestBody("application/json".toMediaTypeOrNull())

        // here do the post request
            var prettyJson: String = ""
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.createEmployee(requestBody)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    prettyJson = gson.toJson(JsonParser.parseString(response.body().toString()))
                    Log.d("preety json(response.body)", prettyJson)

                    // checking allowance from server to continue or not
                    if (section_ == "new_account") {
                        if (prettyJson == "Created"){    // new account is created and allowed from the server
                            val intent = Intent(this@posting_data, create_new_account::class.java)
                            startActivity(intent)
                            finishActivity(0)    // this will show this layout only for first time
                        } else{
                            show_result.setText("*Account is not created May your number is already regestered")
                          }
                        Log.d("????? raw of response is","${response}")
                        Log.d("????? raw of response.body is","${response.body()}")
                        Log.d("????? raw of response.raw is","${response.raw()}")
                        Log.d("????? raw of response is","${response}")
                    }

                    if (section_ == "already_account") {
                        if("$prettyJson" == "OK") {
                            val intent = Intent(this@posting_data, MainActivity::class.java)
                            startActivity(intent)
                            finishActivity(0)
                            Log.d("????? now response","status is OK")
                        } else {
                            show_result.setText("*Please enter old password correctly")
                        }
                        Log.d("????? raw of response is","${response}")
                    }
                } else {
                    Log.e("Error in getting", "data from server")
                }
            }
        }
    }


}