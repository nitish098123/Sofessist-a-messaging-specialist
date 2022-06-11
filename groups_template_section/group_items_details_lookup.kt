package com.example.database_part_3.groups_template_section

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.groups.group_chat_adapter

class group_items_details_lookup(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {

    override fun getItemDetails(event : MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)

        if(view != null){
//            return (recyclerView.getChildViewHolder(view) as group_chat_adapter.DataAdapterViewHolder).getItemDetails()
          return (recyclerView.getChildViewHolder(view) as comment_group_adapter.DataAdapterViewHolder).getItemDetails()
        }
        return null
    }
}