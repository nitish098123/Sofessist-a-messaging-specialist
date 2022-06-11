package com.example.database_part_3.groups

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import java.util.ArrayList
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.widget.TextView
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_member_click_listener
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


/* For Viewing the group members  */
class group_member_view_adapter(private val context_ : Context ,
                                private val adapterData : ArrayList<group_member_model>,
                                private  val group_number : String
                                )
    : RecyclerView.Adapter<group_member_view_adapter.DataAdapterViewHolder>(){

    val mapper = jacksonObjectMapper()

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
        holder.bind(supply_data,position)
    }

    override fun getItemCount() : Int = adapterData.size

    override fun getItemId(position: Int) : Long = position.toLong()


    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        // for binding data
        fun bind(dataModel : group_member_model,position : Int){
            val _name = itemView.findViewById<TextView>(R.id.members_name_show)          // for showing the name of the person
            _name.setText(dataModel.member_name)

            val admin_text = itemView.findViewById<TextView>(R.id.admin_id)
            val not_admin_text = itemView.findViewById<TextView>(R.id.not_admin_show_id)

            if(dataModel._admin==true)admin_text.visibility = View.VISIBLE
            if(dataModel._admin==false)not_admin_text.visibility = View.VISIBLE

            if(MY_ADMIN_STATUS_==true){
                // want to make admin
                not_admin_text.setOnClickListener {
                    val mBuilder = AlertDialog.Builder(context_)
                    val variable_ = "Make "+"<b>" + "${adapterData[position].member_name}" + "</b>" + " admin of this group"
                    mBuilder.setPositiveButton("ok") { dialog, which ->
                        not_admin_text.visibility = View.GONE
                        admin_text.visibility = View.VISIBLE
                        adapterData[position]._admin = true
                        val thread = Thread({
                            update_to_db()
                        })
                        thread.start()
                        dialog.dismiss()
                    }
                    mBuilder.setNeutralButton("cancel") { dialog_, which ->
                        dialog_.cancel()
                    }
                    mBuilder.setTitle(variable_)
                    mBuilder.show()
                }

                // want to remove from admin
                admin_text.setOnClickListener {
                    val mBuilder = AlertDialog.Builder(context_)
                    val variable_ = "Remove "+"<b>" + "${adapterData[position].member_name}" + "</b>" + "from group admin of this group"
                    mBuilder.setPositiveButton("ok") { dialog, which ->
                        not_admin_text.visibility = View.VISIBLE
                        admin_text.visibility = View.GONE
                        adapterData[position]._admin = false
                        val thread = Thread({
                            update_to_db()
                        })
                        thread.start()
                        dialog.dismiss()
                    }
                    mBuilder.setNeutralButton("cancel") { dialog_, which ->
                        dialog_.cancel()
                    }
                    mBuilder.setTitle(Html.fromHtml(variable_))
                    mBuilder.show()
                }
            }
            if(MY_ADMIN_STATUS_ == false){
                admin_text.setOnClickListener {
                    alert_function()
                }
                not_admin_text.setOnClickListener {
                    alert_function()
                }
            }
        }

       // for updating the data of sql data
        fun update_to_db(){
            val DB = universal_chat_store(context_,null)
            val new_data = mapper.writeValueAsString(adapterData)
            DB.update_group_info("change_admin",new_data,group_number)  // updating the database data
        }

        fun alert_function(){
            val builder = AlertDialog.Builder(context_)
            builder.setTitle("As you are not group admin , you cannot change this setting")
            builder.setPositiveButton("OK"){ d ,_->
                d.dismiss()
            }
            builder.show()
        }

    }
}