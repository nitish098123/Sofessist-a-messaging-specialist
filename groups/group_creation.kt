package com.example.database_part_3.groups

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.forward.pair_to_model
import com.example.database_part_3.message_holder.DataAdapter
import com.example.database_part_3.model.dp_
import com.example.database_part_3.model.universal_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.junit.experimental.theories.internal.AllMembersSupplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


// the group creation layout
class group_creation : AppCompatActivity(){

    val mapper =  jacksonObjectMapper()
    private lateinit var adapter : group_creation_adapter
    private lateinit var chipgroup_write_msg : ChipGroup
    private lateinit var chipgroup_admin : ChipGroup
    private lateinit var members_show_id : RecyclerView
    private lateinit var done_id : Button
    private lateinit var add_group_dp_id : ImageView
    var get_members = ArrayList<select_contact_list_model>()
    var context_ : Context = this
    var OUTER_PERSON_CAN_TEXT : Boolean = false
    var PRIVATE_GROUP : Boolean = false
    var GROUP_NUMBER : String = ""
    private var group_dp = ""
    val mute_person = ArrayList<String>()
    var group_name = ""
    var descriptions_ = ""
    var FINAL_GROUP_MEMBERS = ArrayList<group_member_model>()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_creation)

        context_ = this

        // initializing chipGroup
        chipgroup_write_msg = findViewById(R.id.member_can_chat_in_group_id)
        chipgroup_admin = findViewById(R.id.select_group_admin_id)
        var descrip_ = findViewById<EditText>(R.id.group_description_id)
        var name_group = findViewById<EditText>(R.id.group_name_id)
        done_id = findViewById(R.id.done_id)
        add_group_dp_id = findViewById(R.id.add_group_dp_id)


        // receiving the group members from contact selections layout
        val get_members_str : String = intent.getStringExtra("+members").toString()
        get_members = mapper.readValue<ArrayList<select_contact_list_model>>(get_members_str)


        val handler_ = Handler()
        val thread_ = Thread({
            members_show_id = findViewById(R.id.members_show_id)
            val layout_manager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
            members_show_id.layoutManager = layout_manager
            members_show_id.setHasFixedSize(true)
            adapter = group_creation_adapter(this)
            members_show_id.adapter = adapter
            handler_.post {
                adapter.setData(get_members)

                for(i in get_members){
                    FINAL_GROUP_MEMBERS.add(group_member_model("",i._name_,i._number_,true,true))      // inital state all persons are allowed
                }

                for(i in FINAL_GROUP_MEMBERS){            // showing to chipGroups
                    add_chips_write_msg(i.member_name)
                    add_chips_select_admin(i.member_name)
                }

            }
        })
        thread_.start()

        // for radio group for outer person can write message or not
        val radio_outer = findViewById<RadioGroup>(R.id.radio_button_outer_person_id)
        radio_outer.setOnCheckedChangeListener{ radioGroup , ID ->
            when(ID){
                R.id.outer_person_allow_id ->{
                    OUTER_PERSON_CAN_TEXT = true
                    Toast.makeText(context_,"outer person is alllowed!",Toast.LENGTH_SHORT).show()
                }
                R.id.outer_person_disallow_id ->{
                    OUTER_PERSON_CAN_TEXT = false
                    Toast.makeText(context_,"outer person is dis-allowed!",Toast.LENGTH_SHORT).show()
                }
            } }

        // for radio button of private group
        val radio_private = findViewById<RadioGroup>(R.id.radio_group_private_id)
        radio_private.setOnCheckedChangeListener { radioGroup, ID ->
            when(ID) {
                R.id.private_allow_id -> {
                    PRIVATE_GROUP = true
                    Toast.makeText(context_,"Private group is true!",Toast.LENGTH_SHORT).show()
                }
                R.id.private_disallow_id -> {
                    PRIVATE_GROUP = false
                    Toast.makeText(context_,"Private group is false!",Toast.LENGTH_SHORT).show()
                }
            } }

        // final of group creations
        val DB = universal_chat_store(this,null)
        val thread = Thread({
            DB.save_group_info(GROUP_NUMBER,FINAL_GROUP_MEMBERS,PRIVATE_GROUP,descriptions_,group_name,group_dp,
                         false,mute_person,false,"","")
        })


        // finally after all the opotions are clicked this done button have to be clicked
        done_id.setOnClickListener{
            group_name = name_group.text.toString()          // always you have to put
            descriptions_ = descrip_.text.toString()         // then only that will compile and and bring the text
            Log.d("","ggggggggggggroup name is: ${group_name}")

            if(group_name!=""){
                GROUP_NUMBER = "${Date()}|${System.currentTimeMillis()}"    // this is the group number
                val save_to_local = group_local_model(GROUP_NUMBER,FINAL_GROUP_MEMBERS,PRIVATE_GROUP,descriptions_,group_name,group_dp,
                                                   false,mute_person,false,"")
                thread.start()
                  /*
                    send this data to the server
                    and also send to firebase
                  */
                Log.d("","ssnnnnnow saving to database of group data :$save_to_local")
                finish()            // this will make self distructions of this activity
            }
            if(group_name==""){
                Toast.makeText(this,"Please enter a group name",Toast.LENGTH_LONG).show()
            }
        }


        // for giving dp in group
        add_group_dp_id.setOnClickListener{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permission,1001)
                }
                else{  choose_image_galary() }
            }
            else{
                choose_image_galary()
            }
        }
    }

    // adding chips for selecting who can chats in group
    private fun add_chips_write_msg(text_ : String){
        val chip = Chip(this)
        chip.text = text_
        chip.isCloseIconVisible = true
        chip.setChipIconResource(R.drawable.display_picture)
        chip.setOnCloseIconClickListener { it ->
            Toast.makeText(this,"Closed chip is : ${chip.text}", Toast.LENGTH_SHORT).show()
            chipgroup_write_msg.removeView(chip)

            // also removing from the selected persons array
            var t=0
            for(i in FINAL_GROUP_MEMBERS){
                if(i.member_name==chip.text){
                    FINAL_GROUP_MEMBERS[t].can_write_chat = false    // this will disallow person to write message in group
                    break
                }
              t++
            }
        }
        chipgroup_write_msg.addView(chip)
    }

    // adding chips fro selecting admin
    private fun add_chips_select_admin(text_ : String){
        val chip = Chip(this)
        chip.text = text_
        chip.isCloseIconVisible = true
        chip.setChipIconResource(R.drawable.display_picture)
        chip.setOnCloseIconClickListener { it->
            Toast.makeText(this,"Closed chip is : ${chip.text}", Toast.LENGTH_SHORT).show()
            chipgroup_admin.removeView(chip)

            // also removing from the selected persons array
            var t=0
            for(i in FINAL_GROUP_MEMBERS){
                if(i.member_name==chip.text){
                    FINAL_GROUP_MEMBERS[t]._admin = false    // this will disallow member to become admin
                    break
                    Toast.makeText(this,"chip position:${t} is removed",Toast.LENGTH_LONG).show()
                }
              t++
            }
        }
        chipgroup_admin.addView(chip)
    }


    // for selecting picture from gallery
    private fun choose_image_galary(){    // this one will pick all the images
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action = Intent.ACTION_GET_CONTENT                   //  this one allow to pick multiple image from gallery
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),1000)
    }


    // for catching the uri of group_dp image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data : Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode==1000) {
              val image_uri : Uri ? = data!!.data
              val DP_ = dp_("","$image_uri")
              group_dp = mapper.writeValueAsString(DP_)           // strigifying the json
        }
    }
}