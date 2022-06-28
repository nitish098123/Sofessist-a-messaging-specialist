package com.example.database_part_3.groups

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.forward.chips_formation
import com.example.database_part_3.groups.can_write_message.can_chat_activity
import com.example.database_part_3.groups.group_member_show.group_member_show
import com.example.database_part_3.groups.remove_person.activity_part
import com.example.database_part_3.groups.show_media_section.all_section_view
import com.example.database_part_3.groups.view_media_section.whole_activity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog


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
    private lateinit var make_private_icon : ImageButton
    var PRIVATE_GROUP : Boolean = false
    private val mapper = jacksonObjectMapper()
    var group_name : String = ""
    var group_number : String = ""
    var last_msg_number : String = ""
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
        make_private_icon = findViewById(R.id.make_group_private)          // make private group for all
        val private_text_id = findViewById<TextView>(R.id.private_id_text)
        val finger_print_text = findViewById<TextView>(R.id.private_chat_id_string)

        group_name = intent.getStringExtra("+group_name").toString()
        group_number = intent.getStringExtra("+group_number").toString()
        last_msg_number = intent.getStringExtra("+last_msg_number").toString()



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
                PRIVATE_GROUP = _info.group_private_chat         // if the group is private for all
            }

            // scanning for the group admin of myself user
            for(i in GROUP_MEMBERS){
                if(i.member_number==MY_NUMBER.toString()){
                    if(i._admin==true) MY_ADMIN_STATUS_ = true
                    break
                }
            }

            handler.post{
                 if(PRIVATE_GROUP==true){
                     make_private_icon.setImageResource(R.drawable.lock_logo)
                     private_text_id.setText("Private Group")
                 }
                 if(PRIVATE_GROUP==false){
                     make_private_icon.setImageResource(R.drawable.unlock_logo)
                     private_text_id.setText("Make Private")
                 }
                if(PRIVATE_GROUP ==true){     // disallow to take screenshoot
                    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
                }

                if(GROUP_INDIVIDUAL_RIVATE_CHAT==true){
                    finger_print_text.setText(Html.fromHtml("FingerPrint Lock <b>On</b>"))
                }
                if(GROUP_INDIVIDUAL_RIVATE_CHAT==false){
                    finger_print_text.setText(Html.fromHtml("FingerPrint Lock <b>Off</b>"))
                }
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


        //  for the viewing the media section
        val media_section = findViewById<Button>(R.id.view_below_sheeet_media)
        media_section.setOnClickListener{
            val intent = Intent(this,all_section_view::class.java)
            intent.putExtra("group_number",group_number)
            intent.putExtra("private_chat",PRIVATE_GROUP)
            startActivity(intent)
        }

        // removing persons from group
        val remove_person = findViewById<ImageButton>(R.id.remove_person_id)
        remove_person.setOnClickListener{
            val intent = Intent(this, activity_part::class.java)
            intent.putExtra("+group_number",group_number)
            intent.putExtra("+group_name",group_name)
            intent.putExtra("+members",mapper.writeValueAsString(GROUP_MEMBERS))
            intent.putExtra("+last_message_number",last_msg_number)
            startActivity(intent)
        }

        // make private group
        make_private_icon.setOnClickListener {

            if(MY_ADMIN_STATUS_==true){        // can make private group as group admin
                val DB = universal_chat_store(this,null)
                val dialog = AlertDialog.Builder(this)
                var status = true
                if (PRIVATE_GROUP == false){
                    status = true
                    dialog.setTitle("Are you sure , you want to make this group private")
                }
                if (PRIVATE_GROUP == true) {
                    status = false
                    dialog.setTitle("Are you sure , you want to make unprivate group")
                }
                val handler = Handler()
                dialog.setPositiveButton("Yes"){ d, _ ->
                val thread = Thread({
                    DB.update_group_info("GROUP_PRIVATE", "$status", group_number)
                    handler.post {
                        if (status == true) {
                            make_private_icon.setImageResource(R.drawable.lock_logo)
                            private_text_id.setText("Private Group")
                        }
                        if(status==false){
                            make_private_icon.setImageResource(R.drawable.unlock_logo)
                            private_text_id.setText("Make Private")
                        }
                    }
                })
                thread.start()
                d.dismiss()
            }
                dialog.show()
            }

            if(MY_ADMIN_STATUS_==false){      // cannot change this settings as not group admin
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("As you are not admin of this group , so you cannot change this setting")
                dialog.setPositiveButton("OK"){d,_->
                    d.dismiss()
                }
                dialog.show()
            }
        }

        // making the individual fingerprint lock
        val finger_print_lock = findViewById<ImageButton>(R.id.private_chats_id_)
        finger_print_lock.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            var status = false
            if(GROUP_INDIVIDUAL_RIVATE_CHAT==true){
                status = false
                builder.setTitle("Remove finger printlock to see messages of this group")
            }
            if(GROUP_INDIVIDUAL_RIVATE_CHAT==false){
                status = true
                builder.setTitle("Give finger printlock to see messages of this group")
            }

            builder.setPositiveButton("OK"){ d , _ ->
                finger_print_text.setText(Html.fromHtml("FingerPrint Lock <b>$status</b>"))      // updating to view the fingerprint lock status
                val thread = Thread({
                    DB.update_group_info("FINGER_PRINT_LOCK", "$status", group_number)
                })
                thread.start()
                d.dismiss()
            }
            builder.show()
        }

        // adding members in the group
        val add_members = findViewById<ImageView>(R.id.add_members_)
        add_members.setOnClickListener{
            val member_str = mapper.writeValueAsString(GROUP_MEMBERS)
            val intent = Intent(this, chips_formation::class.java)
            intent.putExtra("group_number",group_number)
            intent.putExtra("all_members",member_str)
            intent.putExtra("add_member","true")
            intent.putExtra("message_number",last_msg_number)
            startActivity(intent)
        }

    }


    fun attached_data(){
        // attaching members
    }


    // when horizontal recycleView member is clicked
    override fun click_listener(position: Int){
        Toast.makeText(this,"The position of clicked persons is :${position}",Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Animatoo.animateSlideUp(this)
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


    //############### functions for media sections view ####################
    fun show_images_section() : ArrayList<Int>{
        // for photos grid view
        val list = ArrayList<Int>()
        for(i in 0..12){
            list.add(R.drawable.photo_galary)
        }
        return list
    }


    // for vedio section
    fun show_vedio_section() : ArrayList<Int>{
        val list = ArrayList<Int>()
        for(i in 0..9){
            list.add(R.drawable.vedio_library)
        }
        return list
    }

    // #######################  view of media sections completed  #######################

}