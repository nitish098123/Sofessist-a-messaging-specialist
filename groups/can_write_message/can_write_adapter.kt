package com.example.database_part_3.groups.can_write_message

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import java.util.ArrayList
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_member_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class can_write_adapter(private val context_ : Context,
                         private val adapterData : ArrayList<group_member_model>,
                         private val group_number : String)

    : RecyclerView.Adapter<can_write_adapter.DataAdapterViewHolder>(){

    val mapper = jacksonObjectMapper()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(R.layout.can_write_message_items, parent,false)
        return DataAdapterViewHolder(view)
    }


    override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int){
        val supply_data : group_member_model = adapterData[position]
        holder.bind(supply_data,position)
    }

    override fun getItemCount() : Int = adapterData.size

    override fun getItemId(position: Int) : Long = position.toLong()


    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        // for binding data
        fun bind(dataModel : group_member_model,position : Int ){

            //for name
            val _name = itemView.findViewById<TextView>(R.id.name_write_status)
            _name.setText(dataModel.member_name)

            // can write icon
            val can_write = itemView.findViewById<ImageView>(R.id.can_write_icon)
            if(dataModel.can_write_chat==true){         // offcourse if admin then only he can edit this
                can_write.visibility = View.VISIBLE
            }

            // cannot write icon
            val cannot_write = itemView.findViewById<ImageView>(R.id.cannot_write_icon)
            if(dataModel.can_write_chat==false){        // check if user is admin or not
                cannot_write.visibility =View.VISIBLE
            }

            // allow click
            if(MY_ADMIN_STATUS==true){

                val allow_ = itemView.findViewById<TextView>(R.id.write_allow)
                allow_.visibility = View.VISIBLE
                allow_.setOnClickListener {
                    var variable_ = ""

                    val mBuilder = AlertDialog.Builder(context_)
                    if (adapterData[position].can_write_chat == true){
                        variable_ = "<b>" + "${adapterData[position].member_name}" + "</b>" + " is already allowed to write messages in this group"
                        mBuilder.setPositiveButton("ok"){dialog, which ->
                            dialog.dismiss()
                        }
                    }

                    if (adapterData[position].can_write_chat == false){
                        variable_ = "Allow "+"<b>" + "${adapterData[position].member_name}" + "</b>" + " to write messages in this group"
                        mBuilder.setPositiveButton("yes") { dialog, which ->
                            can_write.visibility = View.VISIBLE
                            cannot_write.visibility = View.GONE
                            adapterData[position].can_write_chat = true
                            update_to_db()
                            dialog.dismiss()
                        }
                        mBuilder.setNeutralButton("cancel") { dialog_, which ->
                            dialog_.cancel()
                        }
                    }

                    mBuilder.setTitle(Html.fromHtml(variable_))
                    mBuilder.show()
                }

                // disallow click
                val disallow_write = itemView.findViewById<TextView>(R.id.write_disallow)
                disallow_write.visibility = View.VISIBLE
                disallow_write.setOnClickListener {
                    var variable_ = ""
                    val mBuilder = AlertDialog.Builder(context_)
                    if (adapterData[position].can_write_chat == true) {
                        variable_ = "Are you sure you don't allow " + "<b>" + "${adapterData[position].member_name}" + "</b>" + " to write in this group "

                        mBuilder.setPositiveButton("yes") { dialog, which ->
                            can_write.visibility = View.GONE
                            cannot_write.visibility = View.VISIBLE
                            adapterData[position].can_write_chat = false
                            update_to_db()
                            dialog.dismiss()
                        }

                        mBuilder.setNeutralButton("cancel") { dialog_, which ->
                            dialog_.cancel()
                        }
                    }
                    if (adapterData[position].can_write_chat == false) {
                        variable_ = "<b>" + "${adapterData[position].member_name}" + "</b>" + " already cannot write messages in this group"
                        mBuilder.setPositiveButton("ok") { dialog, which ->
                            dialog.dismiss()
                        }
                    }
                    mBuilder.setTitle(Html.fromHtml(variable_))
                    mBuilder.show()
                }
            }

            if(MY_ADMIN_STATUS==false){
                // for my non admin status
            }
        }

        // for updating the data of sql data
        fun update_to_db(){
            val DB = universal_chat_store(context_,null)
            val new_data = mapper.writeValueAsString(adapterData)
            DB.update_group_info("can_write",new_data,group_number)  // updating the database data
        }
    }
}