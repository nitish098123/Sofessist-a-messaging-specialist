package com.example.database_part_3.groups

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class Items_detail_look_up(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {

    override fun getItemDetails(event : MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)

        if(view != null){
//        return (recyclerView.getChildViewHolder(view) as DataAdapter.DataAdapterViewHolder).getItemDetails()
            return (recyclerView.getChildViewHolder(view) as group_chat_adapter.DataAdapterViewHolder).getItemDetails()
        }
        return null
    }
}