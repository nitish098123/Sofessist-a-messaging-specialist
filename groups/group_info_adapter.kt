package com.example.database_part_3.groups

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import java.util.ArrayList
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.example.database_part_3.groups.group_member_click_listener


class group_info_adapter(private val context_ : Context ,
                             private val adapterData : ArrayList<group_member_model>,
                             private val click_listener : group_member_click_listener)

    : RecyclerView.Adapter<group_info_adapter.DataAdapterViewHolder>(){

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(R.layout.group_members_show_item, parent,false)
        return DataAdapterViewHolder(view)
    }


    override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int){
        val supply_data : group_member_model = adapterData[position]
        holder.bind(supply_data)
        holder.itemView.setOnClickListener{
            click_listener.click_listener(position)      // this will supply the position of group_member
        }
    }

    override fun getItemCount() : Int = adapterData.size

    override fun getItemId(position: Int) : Long = position.toLong()


    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        // for binding data
        fun bind(dataModel : group_member_model){
            val _name = itemView.findViewById<TextView>(R.id.members_name_show)          // for showing the name of the person
//            val _number = itemView.findViewById<TextView>(R.id.number_all_contact_)
            _name.setText(dataModel.member_name)
        }
    }
}