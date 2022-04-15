package com.example.database_part_3.message_holder

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.MainActivity

class MyItemDetailsLookup(private val recyclerView: RecyclerView):
       ItemDetailsLookup<Long>() {

    override fun getItemDetails(event : MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)

        if (view != null) {
            return (recyclerView.getChildViewHolder(view) as DataAdapter.DataAdapterViewHolder).getItemDetails()
        }
        return null
    }
}