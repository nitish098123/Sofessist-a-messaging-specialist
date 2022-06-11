package com.example.database_part_3.groups

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.DropBoxManager
import android.os.Handler
import android.util.Log
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.can_write_message.can_chat_activity
import com.example.database_part_3.groups.group_member_show.group_member_show
import com.example.database_part_3.model.universal_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.internal.notifyAll

var MY_ADMIN_STATUS_ = false
var group_number : String = ""
class groups_info : AppCompatActivity() , group_member_click_listener {
    private lateinit var context_ : Context
    private lateinit var save_to_gallery : ImageButton
    var GROUP_ADMIN_STATUS : Boolean = false
    var GROUP_PRIVATE_ : Boolean = false
    private lateinit var group_name_show : TextView
    var GROUP_MEMBERS = ArrayList<group_member_model>()
    var GROUP_DP = ""
    var GROUP_NOTIFICATION = true
    var GROUP_WALLPAPER = ""
    var GROUP_INDIVIDUAL_RIVATE_CHAT = false
    var GROUP_AUTO_DELETE = ""
    var MUTE_SPECIFIC_PERSON = ArrayList<group_mute_person_model>()         // this string is json form of mute_list_model
    private lateinit var GROUP_MEMBERS_SHOW : RecyclerView
    private var list_of_mute_person = ArrayList<group_mute_person_model>()
    private val mapper = jacksonObjectMapper()
    var group_name : String = ""
    var group_number : String = ""
    var MY_NUMBER = 6900529357

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.groups_info)
        context_ = this

        // for group info
        val _progress_bar =ProgressDialog(this)
        _progress_bar.setMessage("Accessing group info")
        _progress_bar.show()

        group_name_show = findViewById(R.id.group_name_show)

        group_name = intent.getStringExtra("+group_name").toString()
        group_number = intent.getStringExtra("+group_number").toString()

        group_name_show.setText(group_name)        // attaching group name

        // digging group data from database table
        val handler = Handler()
        val DB = universal_chat_store(this,null)
        val thread = Thread({
            val _info = DB.get_group_info(group_number)
            if(_info.group_member.size==0){
                Log.d("","nnnnnnnnnnobody is in the group")
            }
            if(_info.group_member.size>0){
                GROUP_MEMBERS = _info.group_member
                GROUP_DP = _info.group_dp
                GROUP_NOTIFICATION = _info.group_notification
                GROUP_WALLPAPER = _info.group_wallpaper
                GROUP_INDIVIDUAL_RIVATE_CHAT = _info.individual_private_chat
                GROUP_AUTO_DELETE = _info.group_auto_delete
            }

            // scanning for the group admin of myself user
            for(i in GROUP_MEMBERS){
                if(i.member_number==MY_NUMBER.toString()){
                    if(i._admin==true) MY_ADMIN_STATUS_ = true
                    break
                }
            }

            handler.post{
                _progress_bar.dismiss()
            }
        })
        thread.start()

        save_to_gallery = findViewById(R.id.save_to_gallery_id_)
        // save to gallery button
        /*
           1. if this user is group_admin( then only he can change PRIVATE_CHAT to non_PRIVATE_CHAT for all)
           2. if this user is not group admin then ( if(GROUP_IS_PRIVATE : Then there is nothing he can do) , if(group is not private he can do anything for himself only)  )
        */

        save_to_gallery.setOnClickListener {
            if(GROUP_ADMIN_STATUS==true){

            }
            if(GROUP_ADMIN_STATUS==false){

            }
        }

        // view group members id
        val members = findViewById<ImageButton>(R.id.view_group_member_id)
        members.setOnClickListener {
            val intent = Intent(this,group_member_show::class.java)
            intent.putExtra("+group_number",group_number)
            startActivity(intent)
        }

        // mute specific person
        val mute_individual_id = findViewById<ImageButton>(R.id.mute_individual_id)
        mute_individual_id.setOnClickListener {
            mute_specific_person()
        }

        // who can write message in group
        val  write_message = findViewById<ImageButton>(R.id.who_can_write_msg_id)
        write_message.setOnClickListener {
            write_message()
        }

    }


    fun attached_data(){
        // attaching members
    }

    // when horizontal recycleView member is clicked
    override fun click_listener(position: Int){
        Toast.makeText(this,"The position of clicked persons is :${position}",Toast.LENGTH_LONG).show()
    }


    // showing the muted person in the specific group
    fun mute_specific_person(){
        val sheet = BottomSheetDialog(context_)
        sheet.setContentView(R.layout.mute_person_group)
        val mute_person_list = sheet.findViewById<RecyclerView>(R.id.person_mute_show)

        val handler = Handler()
        val thread = Thread({
        var t=0

            if(MUTE_SPECIFIC_PERSON.size==0){
                 var store_ = ArrayList<group_mute_person_model>()
                 val DB = universal_chat_store(this,null)
                 store_ = DB.get_specific_mute_person(group_number)

                 for(k in GROUP_MEMBERS){
                     var status = 0
                     for(j in store_){
                         if(j.number_==k.member_number){
                             MUTE_SPECIFIC_PERSON.add(group_mute_person_model(k.dp_,true,k.member_name,k.member_number))
                             status++
                             break
                         }
                     }

                     if(status==0)MUTE_SPECIFIC_PERSON.add(group_mute_person_model(k.dp_,false,k.member_name,k.member_number))
                 }
                Log.d("","mmmmmmmmute specific persons: ${MUTE_SPECIFIC_PERSON}")
            }

        handler.post{
                // adapter adapting the recycle View
                val layout_manager = LinearLayoutManager(context_)
                mute_person_list!!.layoutManager = layout_manager
                mute_person_list.setHasFixedSize(true)
                mute_person_list.adapter = mute_person_group_adapter(context_ , MUTE_SPECIFIC_PERSON , group_number)
          }
        })
        thread.start()
        sheet.show()
    }

    fun write_message(){
        val intent = Intent(this,can_chat_activity::class.java)
        intent.putExtra("+group_number",group_number)
        startActivity(intent)
    }

}
