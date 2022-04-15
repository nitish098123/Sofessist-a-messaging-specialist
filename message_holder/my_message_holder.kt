package com.example.database_part_3.message_holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R

class my_message_holder(v : View):RecyclerView.ViewHolder(v) {
    val label1 : TextView
    init {        // Taking the id of myMessage Layout
       label1 = v.findViewById(R.id.txtMyMessage)
    }
}