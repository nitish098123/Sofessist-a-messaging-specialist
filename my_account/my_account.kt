package com.example.database_part_3.my_account

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.model.universal_model
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.my_account.*

class my_account : AppCompatActivity(){
    private lateinit var change_name : ImageView
    private lateinit var change_about : ImageView
    private lateinit var change_dp : ImageView

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_account)

        // buttons
        change_name = findViewById(R.id.change_my_name_id)
        change_about = findViewById(R.id.change_my_about_id)
        change_dp = findViewById(R.id.change_my_dp)

        val sheet : BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.account_edittext)
        val value = sheet.findViewById<EditText>(R.id.changed_value_id)
        val show_guide = sheet.findViewById<TextView>(R.id.show_guide_id)
        val save_button = sheet.findViewById<TextView>(R.id.save_change_button_id)
     //        val text_ =  value?.text.toString()
        sheet.setCancelable(true)

         // for the database to save name and number and about
        val DB = universal_chat_store(this,null)

        val hand = Handler()

        val thread = Thread({
        var store : universal_model.get_my_account ? = DB.get_my_account()

            hand.post({
                if (store != null) {
                    my_name_id.setText(store._name)
                    about_show_id.setText(store.about)
                    Toast.makeText(this,"name from db:${store._name}",Toast.LENGTH_SHORT).show()
                }
                if(store==null){
                    Toast.makeText(this,"the my account table is null",Toast.LENGTH_SHORT).show()
                }

                name_layout.setOnClickListener {
                    show_guide!!.setText("change your name")

                    save_button!!.setOnClickListener {
                        val text_ = value?.text.toString()
                        my_name_id.setText(text_)            //  displaing updated name
                        if (store == null) {
                            DB.save_to_my_account(
                                "name",
                                text_
                            )   // saving account username for first time
                        }
                        if (store != null) {
                            DB.update_my_account("name", text_)   // updating the account name
                        }
                        sheet.cancel()
                    }
                    sheet.show()
                }

                about_section.setOnClickListener {
                    show_guide!!.setText("change your about")
                    save_button!!.setOnClickListener {
                        val text_ = value?.text.toString()
                        Toast.makeText(this, "the edited value :${text_}", Toast.LENGTH_SHORT)
                            .show()
                        about_show_id.setText(text_)
                        if (store == null) {
                            DB.save_to_my_account(
                                "about",
                                text_
                            )   // saving account username for first time
                        }
                        if (store != null) {
                            DB.update_my_account("about", text_)   // updating the account name
                        }
                        sheet.cancel()
                    }
                    sheet.show()
                }
            })
        })
        thread.start()

        change_dp.setOnClickListener {
            show_photos()
        }

    }

    fun show_photos(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission,1001)
            } else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent,1000)
            }
        }
        else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,1000)
        }
    }

}