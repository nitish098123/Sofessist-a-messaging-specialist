package com.example.database_part_3.groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.forward.pair_to_model
import com.example.database_part_3.user_info.ChildRecyclerViewAdapter
import com.example.database_part_3.user_info.ParentRecyclerViewAdapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*

class group_creation_adapter(val context_ : Context) :RecyclerView.Adapter<group_creation_adapter.DataAdapterViewHolder>(){

        private val adapterData = mutableListOf<select_contact_list_model>()
        val mapper = jacksonObjectMapper()

        // make another setData for other users

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
            val view = LayoutInflater.from(context_).inflate(R.layout.group_member_item, parent, false)
            return DataAdapterViewHolder(view)
        }

        //    payload_ : MutableList<Any>
        override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int){
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context_, LinearLayoutManager.HORIZONTAL, false)
            val _data : select_contact_list_model = adapterData[position]
            holder.bind(_data)
        }

        override fun getItemCount() : Int = adapterData.size

        override fun getItemId(position: Int) : Long = position.toLong()

        fun setData(data : ArrayList<select_contact_list_model>){      // this take the message to be show in the screen
            adapterData.apply {
                addAll(data)
            }
        }

        inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

            fun bind(dataModel : select_contact_list_model){
               val _name = itemView.findViewById<TextView>(R.id.member_name_id)
               _name.text = dataModel._name_
                /* add all dp and name */
            }
        }
  }