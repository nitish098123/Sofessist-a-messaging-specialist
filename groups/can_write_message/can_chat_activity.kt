package com.example.database_part_3.groups.can_write_message

import android.content.Context
import android.os.Bundle
import android.os.DropBoxManager
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_member_click_listener
import com.example.database_part_3.groups.group_member_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

// for allowing to write the message in the group
var MY_ADMIN_STATUS = false
class can_chat_activity : AppCompatActivity(){
     private lateinit var context_ : Context
     private lateinit var can_write_recycle : RecyclerView
     val mapper = jacksonObjectMapper()
     val _MY_NUMBER_ = 6900529357

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.can_write_message)
        context_ = this
        can_write_recycle = findViewById(R.id.allow_chat_id_show)
        // arriving the group number from the previous activity
        val group_number = intent.getStringExtra("+group_number").toString()

        // data must be come from the database not from intent
        val handler = Handler()
        val thread = Thread({

            val DB= universal_chat_store(this,null)
            val members_ : ArrayList<group_member_model> = DB.get_group_members(group_number)

            // checking the admin status
            for(i in members_){
                if(i.member_number == _MY_NUMBER_.toString()){
                    if(i._admin==true)MY_ADMIN_STATUS = true
                    break
                }
            }

            handler.post{
                val text_show = findViewById<TextView>(R.id.changable_text_id)
                if(MY_ADMIN_STATUS==true){
                    text_show.setText("You are GROUP ADMIN so you can change settings")
                }
                if(MY_ADMIN_STATUS==false){
                    text_show.setText("You are not GROUP ADMIN so you cannot change settings")
                }

                // for adapter of the recycleView
                val layout_manager = LinearLayoutManager(context_)
                can_write_recycle.layoutManager = layout_manager
                can_write_recycle.setHasFixedSize(true)
                can_write_recycle.adapter = can_write_adapter(this,members_,group_number)
            }
        })
        thread.start()
    }
 }