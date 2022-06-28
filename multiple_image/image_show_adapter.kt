package com.example.database_part_3.multiple_image

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.front_page.OnContactClickListener
import com.squareup.picasso.Picasso


class image_show_adapter(private var context_ : Context,
                         private var all_uri : ArrayList<image_select_model>,
                         private val onContactClickListener: OnContactClickListener)
    : RecyclerView.Adapter<image_show_adapter.DataAdapterViewHolder>(){

    var all_uri_ = all_uri

    override fun getItemCount(): Int = all_uri_.size

    override fun onBindViewHolder(holder: image_show_adapter.DataAdapterViewHolder, position: Int){
        val supply_data = all_uri_[position]
        holder.itemView.setOnClickListener{
            onContactClickListener.onContactClickListener(position)
        }
        holder.bind(supply_data)
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): image_show_adapter.DataAdapterViewHolder{
        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(R.layout.image_show_item,parent,false)
        return DataAdapterViewHolder(view)
    }

    fun update_list(data_ : ArrayList<image_select_model>){
        all_uri_ = data_
        notifyDataSetChanged()
    }

    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
         fun bind(data_ : image_select_model){
             val image_view = itemView.findViewById<ImageView>(R.id.small_image_show)      // image showing layout
             val uri_ = data_.uri_str.toUri()
             Picasso.with(context_).load(uri_).resize(600,600).centerCrop().into(image_view)
         }
    }
}