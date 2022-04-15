package com.example.database_part_3.message_holder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R

class others_message_holder(v : View):RecyclerView.ViewHolder(v) {
    var other_text_view : TextView = v.findViewById(R.id.txtOtherUser)
}