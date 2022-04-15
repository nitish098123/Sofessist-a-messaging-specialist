package com.example.database_part_3

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_creation
import com.example.database_part_3.message_holder.chat_activity
import com.example.database_part_3.model.universal_model
import com.example.database_part_3.moments.moment_adapter
import com.example.database_part_3.my_account.my_account
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.drawer_main.*
import kotlinx.android.synthetic.main.nav_header.view.*

/*
  this is for Home page created on 3/3/2022
  By Nitish Kr Boro
*/

class MainActivity : AppCompatActivity()
//    NavigationView.OnNavigationItemSelectedListener
{

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
//    var _name : universal_model.get_my_account ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.drawer_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, 0, 0)
        drawerLayout.addDrawerListener(actionBarToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        actionBarToggle.syncState()
        navView = findViewById(R.id.navView)

        // calling name from database
        val list_view: ListView = findViewById(R.id.listView)
        // bring all the past talks contacts from database
        val DB = universal_chat_store(this, null)
        var recipeList: ArrayList<universal_model.front_contact_msg>? = null

        val hand = Handler()
        val thread: Thread = Thread({
            recipeList = DB.get_past_used_contact()
           val _name_ : universal_model.get_my_account? = DB.get_my_account()
            hand.post({
                val view : View =  navView.getHeaderView(0)
                val name = view.findViewById<TextView>(R.id.header_my_name_id)
                name.setText(_name_?._name)
                val adapter = front_contact_adapter(this, recipeList!!)
                list_view.adapter = adapter
            })
        })
        thread.start()

        val list_name = ArrayList<String>()
        for (i in 0..5) {
            list_name.add("name_of_moments")
        }

        // for moments showung from below
        val moments_id: ImageView = findViewById(R.id.moments_id)

        moments_id.setOnClickListener {
            val sheet: BottomSheetDialog = BottomSheetDialog(this)
            sheet.setContentView(R.layout.moments_layout)
//            val view = layoutInflater.inflate(R.layout.moments_layout,null)
            val list_other_moment: ListView = sheet.findViewById(R.id.list_view_other_moment_id)!!
            val adapter = moment_adapter(this, list_name)
            list_other_moment.adapter = adapter
            sheet.setCancelable(true)
//            sheet.setContentView(view)
            sheet.show()
        }

        // for floating button for new contacts
        val button_: FloatingActionButton = findViewById(R.id.new_chat_id)
        button_.setOnClickListener {
            val intent = Intent(
                this,
                all_contacts::class.java
            )              //   going to the all contacts section
            startActivity(intent)
        }


        // screen for chat
        list_view.setOnItemClickListener { parent, _, position, _ ->
            val name_: String = recipeList!![position]._name_
            val number_: String = recipeList!![position].number_
            Toast.makeText(this, "You clicked : $name_", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, chat_activity::class.java)
            intent.putExtra("+name", name_)
            intent.putExtra("+number", number_)
            startActivity(intent)
        }


        //  head of drawer parts
        val view : View =  navView.getHeaderView(0)
//        val name = view.findViewById<TextView>(R.id.header_my_name_id)
//        name.setText(_name!!._name)                                      // set name in the name section of drawer head
        /*  show the profile view  */
        val head = view.findViewById<RelativeLayout>(R.id.drawer_head)

        head.setOnClickListener{
            val intent = Intent(this,my_account::class.java)
            startActivity(intent)
        }

        navView.setNavigationItemSelectedListener { menuItem->

            when (menuItem.itemId) {
            R.id.my_account -> {
                val intent = Intent(this,my_account::class.java)
                startActivity(intent)
                Toast.makeText(this, "My Profile", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.star_folder_id -> {
                Toast.makeText(this, "stared messages", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.themes_id -> {
                Toast.makeText(this, "themes", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.create_new_group_id -> {
                Toast.makeText(this, "Create new group", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,group_creation::class.java)
                startActivity(intent)
                true
            }
            R.id.list_timer_chat_id -> {
                Toast.makeText(this, "List of timer chat", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.setting_id -> {
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.archive_id -> {
                Toast.makeText(this, "archived contacts", Toast.LENGTH_SHORT).show()
                true
            }
        }
             true
    }
    }


    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.openDrawer(navView)
        return true
    }

    // override the onBackPressed() function to close the Drawer when the back button is clicked
    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}