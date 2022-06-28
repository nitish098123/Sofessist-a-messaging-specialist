package com.example.database_part_3.groups.group_member_show

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_member_view_adapter
import com.example.database_part_3.groups.group_member_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class group_member_show : AppCompatActivity(){

    private lateinit var GROUP_MEMBERS_SHOW : RecyclerView
    private lateinit var context_: Context
    var GROUP_MEMBERS = ArrayList<group_member_model>()
    val mapper = jacksonObjectMapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_member_bottom_sheet)
        context_ = this
        GROUP_MEMBERS_SHOW = findViewById(R.id.group_members_)

        // intent data
        val handler = Handler()
        val group_number = intent.getStringExtra("+group_number").toString()
        val thread = Thread({
            val DB = universal_chat_store(context_,null)
            GROUP_MEMBERS = DB.get_group_members(group_number)  // RETRIVE DATA OF GROUP MEMBERS ONLY FROM DATA BASE NOT FROM INYTENT
            handler.post {
                // fiting the adapter of recyucke view
                val layout_manager = LinearLayoutManager(this) // this will be the vertical scrool view recycleView
                GROUP_MEMBERS_SHOW.layoutManager = layout_manager
                GROUP_MEMBERS_SHOW.setHasFixedSize(true)
                GROUP_MEMBERS_SHOW.adapter = group_member_view_adapter(this,GROUP_MEMBERS,group_number)

            }
        })

        thread.start()
    }


}