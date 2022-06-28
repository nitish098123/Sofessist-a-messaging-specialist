package com.example.database_part_3.groups.remove_person

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.groups.MY_NUMBER
import com.example.database_part_3.groups.group_member_model
import com.example.database_part_3.groups.mute_person_group_adapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val MY_NUMBER__ = 6900529357
class activity_part : AppCompatActivity() {

    private val mapper = jacksonObjectMapper()
    var MY_ADMIN_STATUS : Boolean = true

    private lateinit var context_ : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.removing_person)
        context_ = this

        // receiving the group number and the group number
        val group_number = intent.getStringExtra("+group_number").toString()
        val group_name = intent.getStringExtra("+group_name").toString()
        val str_ = intent.getStringExtra("+members").toString()
        val last_msg_number = intent.getStringExtra("+last_message_number").toString()
        val MEMBERS_ : ArrayList<group_member_model> = mapper.readValue<ArrayList<group_member_model>>(str_)

        for(i in MEMBERS_){                    // finding the admin status from of this user
            if(i.member_number== MY_NUMBER__.toString()){
                MY_ADMIN_STATUS = i._admin
                break
            }
        }

        // removing persons from the group
        val show_persons = findViewById<RecyclerView>(R.id.remove_person_from_group)

        val layout_manager = LinearLayoutManager(context_)
        show_persons!!.layoutManager = layout_manager
        show_persons.setHasFixedSize(true)
        show_persons.adapter = adapter_part(context_ , MEMBERS_, group_number , group_name,last_msg_number ,MY_ADMIN_STATUS)

    }
 }