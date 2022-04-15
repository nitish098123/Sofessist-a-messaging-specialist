package com.example.database_part_3.moments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.database_part_3.R
import com.example.database_part_3.model.universal_model

class moment_adapter(private var context: Context,
                     private var dataSource: ArrayList<String>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.moment_item, parent, false)
        val titleTextView = rowView.findViewById(R.id.other_moments_name) as TextView

        titleTextView.setText(dataSource[position])

        return rowView
    }
}