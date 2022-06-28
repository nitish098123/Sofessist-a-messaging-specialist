package com.example.database_part_3.groups.show_media_section

import com.example.database_part_3.groups.group_chat_adapter
import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class media_details_look_up(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>(){

    override fun getItemDetails(event : MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)

        if(view != null){
//        return (recyclerView.getChildViewHolder(view) as DataAdapter.DataAdapterViewHolder).getItemDetails()
            return (recyclerView.getChildViewHolder(view) as adapter_recycle_view.DataAdapterViewHolder).getItemDetails()
        }
        return null
    }
}