package com.example.database_part_3.groups

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.ArrayList
import java.util.function.DoubleBinaryOperator

class mute_person_group_adapter( private val context_ : Context ,
                                 private var adapterData : ArrayList<group_mute_person_model>,
                                 private val group_number : String )
     : RecyclerView.Adapter<mute_person_group_adapter.DataAdapterViewHolder>(){

     val mapper = jacksonObjectMapper()

     init {
        setHasStableIds(true)
     }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(R.layout.mute_person_group_items, parent,false)
        return DataAdapterViewHolder(view)
     }


     override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int){
        val supply_data = adapterData[position]
        holder.bind(supply_data,position)
     }

     override fun getItemCount() : Int = adapterData.size

     override fun getItemId(position: Int) : Long = position.toLong()


     inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        // for binding data
        fun bind(dataModel : group_mute_person_model , position: Int){
            val _name = itemView.findViewById<TextView>(R.id.mute_person_name_group)          // for showing the name of the person
            val mute_icon = itemView.findViewById<ImageButton>(R.id.mute_button_id)
            val unmute_icon = itemView.findViewById<ImageButton>(R.id.unmute_button_id)
            _name.setText(dataModel.name_)

            if(dataModel.mute==true)   mute_icon.visibility = View.VISIBLE      // this is mute
            if(dataModel.mute==false)  unmute_icon.visibility = View.VISIBLE   // this is unmute Button

            mute_icon.setOnClickListener{    // this means want to unmute the person
                val mBuilder = AlertDialog.Builder(context_)
                mBuilder.setTitle("Unmute ${adapterData[position].name_} messages in this group for me")
                mBuilder.setPositiveButton("yes"){ dialog , which ->
                    mute_icon.visibility = View.GONE
                    unmute_icon.visibility = View.VISIBLE
                    adapterData[position].mute = false       // this means unmuting this person
                    val thread = Thread({
                        update_data()
                    })
                    thread.start()
                    dialog.dismiss()
                }
                mBuilder.setNeutralButton("cancel"){ dialog_ , which->
                   dialog_.cancel()
                }
                mBuilder.show()
            }

            unmute_icon.setOnClickListener{    // this means want to mute the person
                val mBuilder = AlertDialog.Builder(context_)
                mBuilder.setTitle("Mute ${adapterData[position].name_} messages in this group for me")
                mBuilder.setPositiveButton("yes"){ dialog , which ->
                    mute_icon.visibility = View.VISIBLE
                    unmute_icon.visibility = View.GONE
                    adapterData[position].mute = true       // this means person is muted this user
                    val thread = Thread({
                        update_data()
                    })
                    thread.start()
                    dialog.dismiss()
                }
                mBuilder.setNeutralButton("cancel"){ dialog_ , which->
                    dialog_.cancel()
                }
                mBuilder.show()
            }
        }


        // updating the database
        fun update_data(){
            val DB = universal_chat_store(context_,null)
            val str = mapper.writeValueAsString(adapterData)
            DB.update_group_info("mute_member",str, group_number)
        }

    }
}