package com.example.database_part_3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.database_part_3.contact_access.custom_adapter
import com.example.database_part_3.message_holder.chat_activity
import com.example.database_part_3.model.ContactModel

class all_contacts : AppCompatActivity() {

    private var listView:ListView? = null
    private var customAdapter: custom_adapter? = null
    private var contactModelArrayList: ArrayList<ContactModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_contacts_show)

        listView = findViewById(R.id.list_view) as ListView
        contactModelArrayList = ArrayList()

        val hand = Handler()

        val thread : Thread = Thread({
        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contactModel = ContactModel()
            contactModel.setNames(name)
            contactModel.setNumbers(phoneNumber)
            contactModelArrayList!!.add(contactModel)
            Log.d("name>>", name + "  " + phoneNumber)
        }
           phones.close()
            hand.post({
                customAdapter = custom_adapter(this, contactModelArrayList!!)
                listView!!.adapter = customAdapter
            })
        })
        thread.start()

        listView!!.setOnItemClickListener { parent , _ , position , _ ->
            val name_ : String = contactModelArrayList!![position].name.toString()
            val number_ : String = contactModelArrayList!![position].number.toString()
            Toast.makeText(this,"You clicked : $name_",Toast.LENGTH_SHORT).show()

             val intent = Intent(this,chat_activity::class.java)
             intent.putExtra("+name",name_)
             intent.putExtra("+number",number_)
             startActivity(intent)
        }
    }
}