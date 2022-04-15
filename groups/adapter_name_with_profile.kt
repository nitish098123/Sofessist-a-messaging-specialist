package com.example.database_part_3.groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.database_part_3.R
import com.example.database_part_3.model.universal_model

//class adapter_name_with_profile (private var context: Context, var dataSource: ArrayList<String>) : BaseAdapter() {
//    var inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//
//    override fun getCount(): Int {
//        return dataSource.size
//    }
//
//    //2
//    override fun getItem(position: Int): Any {
//        return dataSource[position]
//    }
//
//    //3
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    //4
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        // Get view for row item
//        val rowView = inflater.inflate(R.layout.home_page, parent, false)
//        val titleTextView = rowView.findViewById(R.id.name) as TextView
//        val last_message = rowView.findViewById(R.id.number) as TextView
//        val time_ = rowView.findViewById(R.id._time) as TextView
//
//        titleTextView.setText(dataSource[position]._name_)
//        last_message.setText(dataSource[position].last_msg)
//        time_.setText(dataSource[position].time)
//
//        return rowView
//    }
//}