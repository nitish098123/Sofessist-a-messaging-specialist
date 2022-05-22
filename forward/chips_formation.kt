package com.example.database_part_3.forward

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.R
import com.example.database_part_3.alarm.MyAlarm
import com.example.database_part_3.contact_access.custom_adapter
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_creation
import com.example.database_part_3.groups.select_contact_list_model
import com.example.database_part_3.model.ContactModel
import com.example.database_part_3.model.universal_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

val MY_NUMBER = 6900529357
class chips_formation : AppCompatActivity(){
   private lateinit var chipGroup: ChipGroup
   private lateinit var list_view : ListView
   private var customAdapter : custom_adapter? = null
   private var contactModelArrayList = ArrayList<ContactModel>()
   private var send_forward_msg : FloatingActionButton ? = null
   private val selected_contacts = ArrayList<select_contact_list_model>()      // contains persons number that are selected
   private val selected_persons_numbers = ArrayList<String>()
   val mapper = jacksonObjectMapper()
   var item_with_dp = ArrayList<select_contact_list_model>()


    var list_messages: ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()

   override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chips_view)

        chipGroup = findViewById(R.id.chip_group_show_id)
        list_view = findViewById(R.id.list_of_contact_select_forward_or_groups)
        send_forward_msg = findViewById(R.id.send_forward_msg)

        //  if true then this layout is comming or using for creating group
        val group_creation_ : Boolean = intent.getStringExtra("group_create").toBoolean()
        val args : String = intent.getStringExtra("messages")!!

       if(args!="null"){
           list_messages = mapper.readValue<ArrayList<universal_model.one_chat_property>>(args)
       }

        val _process_dialog = ProgressDialog(this)
        _process_dialog.setMessage("Accessing Contacts")
        val hand = Handler()
        val thread : Thread = Thread({
        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

            while (phones!!.moveToNext()){
                val name = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                // modifying number
                var final_number: String = phoneNumber.toString()

                if (final_number.length < 14 && final_number.length > 9){
                    var i = 0
                    if (final_number.length == 13){
                        final_number = final_number.drop(3)                // removing the +91 part from the mobile number
                        i++
                    }
                    if(i == 0){
                        if(final_number.length == 12){
                            final_number = final_number.drop(2)             // temoving the 91 from the mobile number
                        }
                    }
                    val contactModel = ContactModel()
                    contactModel.setNames(name)
                    contactModel.setNumbers(final_number)
                    contactModel.setdp("")
                    contactModelArrayList.add(contactModel)
                }
            }
                phones.close()
                hand.post({
                    customAdapter = custom_adapter(this, contactModelArrayList)
                    list_view.adapter = customAdapter
                })
        })
        thread.start()
        _process_dialog.show()


        // Get all dp url of all person with number and name
        val DB = universal_chat_store(this,null)
        val handler_ = Handler()
        val thread_dp = Thread({
        val item_with_dp_ =  DB.get_person_dp_name_number()    // this hash_map_with (number and dp)
           // now we have to deliver this number , dp pair to adapter to update the dps
            var p = 0
            for(i in contactModelArrayList){
                val dp_uri = item_with_dp_[i.number]
                contactModelArrayList[p].setdp(dp_uri!!)
                p++
            }
            handler_.post{
                _process_dialog.dismiss()
            }
        })
       thread_dp.start()

        list_view.setOnItemClickListener{ parent, _ , position, _ ->
            val _name : String = contactModelArrayList[position].name.toString()
            val _number : String = contactModelArrayList[position].number.toString()
            val dp_ : String = contactModelArrayList[position]._dp.toString()

            //making original number removing the +91 part
            var final_number : String = _number.toString()
            final_number = final_number.takeLast(10)        // this will simply take the last 10 mobile number

            selected_contacts.add(select_contact_list_model(_name,final_number,dp_))
            selected_persons_numbers.add(final_number)

            add_chips(_name)   // this make the chips to stick at top
        }


    //  for the sending to save the message to database and server
       send_forward_msg!!.setOnClickListener{
           if(group_creation_==null){
            val db = universal_chat_store(this,null)

           val thread : Thread = Thread({
              db.forward_funciton(list_messages,selected_persons_numbers)    // sending to save the selected message and selected persons
              db.close()
           })
           thread.start()
           }

       if(group_creation_==true){                            // that means after this we have to go for group creation layout
           val intent : Intent = Intent(this,group_creation::class.java)
           val mapper = jacksonObjectMapper()
           val str : String = mapper.writeValueAsString(selected_contacts)
           intent.putExtra("+members",str)             // arraylist of the persons selected as group members
           startActivity(intent)
         }
       }
   }

    private fun add_chips(text_ : String){
            val chip = Chip(this)
            chip.text = text_
            chip.isCloseIconVisible = true
            chip.setChipIconResource(R.drawable.display_picture)       // this is where we give the display picture of particular person
            chip.setOnCloseIconClickListener{ it->
                Toast.makeText(this,"Closed chip is : ${chip.text}",Toast.LENGTH_SHORT).show()
                chipGroup.removeView(chip)

                // also removing from the selected persons array
                var u = 0
                for(i in selected_contacts){
                    if(i._name_==chip.text){
                        selected_contacts.removeAt(u)    // this will removes the closed contacts from array
                        Toast.makeText(this,"The positions of removed chip is :$u",Toast.LENGTH_LONG).show()
                        break
                   }
                   u++
                }
            }
            chipGroup.addView(chip)
    }

}
