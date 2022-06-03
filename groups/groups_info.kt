package com.example.database_part_3.groups

import android.app.ProgressDialog
import android.content.Context
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
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.model.universal_model
import com.google.android.material.bottomsheet.BottomSheetDialog


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
    var MUTE_SPECIFIC_PERSON = ArrayList<String>()
    private lateinit var GROUP_MEMBERS_SHOW : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.groups_info)
        context_ = this

        // for group info
        val _progress_bar =ProgressDialog(this)
        _progress_bar.setMessage("Accessing group info")
        _progress_bar.show()

        group_name_show = findViewById(R.id.group_name_show)

        val group_name = intent.getStringExtra("+group_name").toString()
        val group_number = intent.getStringExtra("+group_number").toString()

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
                MUTE_SPECIFIC_PERSON = _info.mute_notification_specific
            }

            handler.post{
                _progress_bar.dismiss()
            }
        })
        thread.start()

        save_to_gallery = findViewById(R.id.save_to_gallery_id_)
        // save to gallery buton
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
            group_member_show()
        }

    }


    fun attached_data(){
        // attaching members
    }

    private fun group_member_show(){
        val sheet: BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.group_member_bottom_sheet)
        GROUP_MEMBERS_SHOW = sheet.findViewById(R.id.group_members_)!!

        // adapter adapting the recycle View
        val layout_manager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        GROUP_MEMBERS_SHOW.layoutManager = layout_manager
        GROUP_MEMBERS_SHOW.setHasFixedSize(true)
        GROUP_MEMBERS_SHOW.adapter = group_info_adapter(this,GROUP_MEMBERS,this)

        sheet.show()
    }

    // when horizontal recycleView member is clicked
    override fun click_listener(position: Int) {
        Toast.makeText(this,"The position of clicked persons is :${position}",Toast.LENGTH_LONG).show()
    }
}