package com.example.database_part_3.groups.remove_person

import android.app.AlertDialog
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.my_number
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.groups.group_member_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.ArrayList



class adapter_part( private val context_ : Context,
                    private val adapterData : ArrayList<group_member_model>,
                    private val group_number : String ,
                    private val group_name : String,
                    private var msg_num : String,            // this is last_message_number of the particular group number
                    private val MY_ADMIN_STATUS : Boolean
                  )
    : RecyclerView.Adapter<adapter_part.DataAdapterViewHolder>(){

    val mapper = jacksonObjectMapper()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(R.layout.remove_person_item, parent,false)
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
        fun bind(dataModel : group_member_model, position : Int) {

            val remove_btn = itemView.findViewById<TextView>(R.id.remove_person)
            itemView.findViewById<TextView>(R.id.members_name_show).text = dataModel.member_name
            val removed_person = itemView.findViewById<TextView>(R.id.removed_person)

            /* also add the person dp here */

            remove_btn.setOnClickListener {
            if(MY_ADMIN_STATUS == true){             // this means user is admin so give the access to him
                val builder = AlertDialog.Builder(context_)
                builder.setTitle(Html.fromHtml("Are you sure you want to remove <b>${adapterData[position].member_name}</b> from this group"))
                builder.setPositiveButton("YES"){ d, _ ->
                    d.dismiss()
                    // update the database part for removing the person from group
                    removed_person.visibility = View.VISIBLE      // viewing the removed button
                    val thread = Thread({
                        var t=0
                        for(i in adapterData){
                            if(i.member_number==dataModel.member_number){
                                adapterData.removeAt(t)
                                break
                            }
                           t++
                        }
                        val data_ = remove_person_model("$MY_NUMBER__",dataModel.member_name)     // this data has to be save in the data base and also send to the server
                        val str_data = mapper.writeValueAsString(data_)
                        update_to_db(str_data)
                    })
                    thread.start()
                    Toast.makeText(context_,"The removed status will be shown in chat after you re-open this app",Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("Cancel"){ d,_ ->
                    d.dismiss()
                }
                builder.show()
            }
            if (MY_ADMIN_STATUS == false){           // this means user is not admin so don't give access to him
                alert_function()
            }
        }
        }

        // for updating the data of sql data
        fun update_to_db(data : String){
            val num_ = msg_num.toInt() + 1          // the message number arrived from the intent part just gives the last message number so you have to increment it
            msg_num = num_.toString()
            val DB = universal_chat_store(context_,null)
            val new_data = mapper.writeValueAsString(adapterData)
            DB.update_group_info("remove_member",new_data,group_number)  // updating the group_members column database data
            DB.update_group_info("remove_from_mute",adapterData[position].member_number,group_number)   // this will remove from the mute column in sql database
            DB.save_group_message(group_name,group_number,msg_num,data,"removed_person","","","","${System.currentTimeMillis()}",
                false,false,"none","none", MY_NUMBER__,"none","none")
            DB.close()
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