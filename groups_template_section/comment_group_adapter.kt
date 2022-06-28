package com.example.database_part_3.groups_template_section

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.MY_NUMBER
import com.example.database_part_3.groups.PRIVATE_CHAT_
import com.example.database_part_3.groups.group_chat_adapter
import com.example.database_part_3.groups.group_message_model
import com.example.database_part_3.message_holder.PRIVATE_CHAT
import com.example.database_part_3.model.image_data_model
import com.example.database_part_3.model.reaction_store_group_model
import com.example.database_part_3.model.replay_data_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.picasso.Picasso
import java.util.ArrayList


/* This adapter is used as adapter for showing the threads of a storing reaction model */

class comment_group_adapter( private val context_ : Context)
    : RecyclerView.Adapter<comment_group_adapter.DataAdapterViewHolder>(){

    var adapterData = ArrayList<group_message_model>()
    var tracker : SelectionTracker<Long>? = null
    var DISPLAY_STATUS = 0                // this will decide which layout should be bind in binding holder
    private val mapper = jacksonObjectMapper()
    private var LOVED_REACTION_TEMPLATE : Boolean = false

    init {
        setHasStableIds(true)
    }

    companion object{
        val MESSAGE_FROM_ME = 0
        val MESSAGE_FROM_OTHERS = 1
        val IMAGE_FROM_ME = 2
        val IMAGE_FROM_OTHER = 3
        val TEMPLATE_ME_VOTE = 4
        val TEMPLATE_OTHER_VOTE = 5
        val TEMPLATE_ME_STORING_REACTION = 6
        val TEMPLATE_OTHER_STORING_REACTION = 7
        val REPLAY_TEMPLATE_ME = 8
        val REPLAY_TEMPLATE_OTHER = 9
        val STICKER_FROM_ME = 10
        val STICKER_FROM_OTHER = 11
    }

    // function that takes the input messages from the AppcompatActivity
    fun setData(data : ArrayList<group_message_model>){
        adapterData = data
        notifyDataSetChanged()
    }

    // functions for updating the new adapterData
    fun update_list(operator_ : String , data : String , position : Int){    // position is selected positions to update the list
        if(operator_=="STAR"){
            adapterData[position].stared = data.toBoolean()
        }
        if(operator_=="EDIT_REWRITE"){
            adapterData[position] = mapper.readValue<group_message_model>(data)
        }
        if(operator_=="UPLOAD_PROGRESS"){
            val parse_data = mapper.readValue<image_data_model>(adapterData[position].data)
            parse_data.upload_progress = data
            adapterData[position].data = mapper.writeValueAsString(parse_data)           // Updating the message getting error
        }
        notifyItemChanged(position)
    }

    // sending the view types for he each message
    override fun getItemViewType(position: Int): Int {
        var type_in_int : Int = 0

        if(adapterData[position].from== MY_NUMBER){
            if(adapterData[position].category=="g_chat")type_in_int = MESSAGE_FROM_ME       // this is for me message layout
            if(adapterData[position].category=="storing_reaction")type_in_int = TEMPLATE_ME_STORING_REACTION
            if(adapterData[position].category=="voting")type_in_int = TEMPLATE_ME_VOTE
            if(adapterData[position].category=="g_i")type_in_int = IMAGE_FROM_ME            // me image showing
            if(adapterData[position].replied_msg=="yes")type_in_int = REPLAY_TEMPLATE_ME
            if(adapterData[position].category=="sticker")type_in_int = STICKER_FROM_ME      // this means this layout is sticker
        }
        else{
            type_in_int = MESSAGE_FROM_OTHERS
        }
        DISPLAY_STATUS = type_in_int
        return type_in_int
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DataAdapterViewHolder{
        var _layout = 0

        if(viewType== MESSAGE_FROM_ME)_layout = R.layout.my_message
        if(viewType== IMAGE_FROM_ME)_layout = R.layout.image_show
        if(viewType== REPLAY_TEMPLATE_ME) _layout = R.layout.replay_showing_layout
        if(viewType== STICKER_FROM_ME) _layout = R.layout.sticker_layout

        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(_layout, parent,false)
        return DataAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int) {
        val supply_data : group_message_model = adapterData[position]
        tracker?.let {
            holder.BIND(supply_data)
        }
    }

    override fun getItemCount() : Int = adapterData.size

    override fun getItemId(position: Int) : Long = position.toLong()

    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        fun bind_msg_me(dataModel : group_message_model){
            val msg_view = itemView.findViewById<TextView>(R.id.txtMyMessage_me)
            msg_view.setText(dataModel.data)

            // controlling visibility
            val lock_ = itemView.findViewById<ImageView>(R.id.lock_showing_msg_id_)
            lock_.visibility = View.GONE

            if(dataModel.stared==true){
                val star_msg = itemView.findViewById<ImageView>(R.id.stared_msg_id_message)
                star_msg.visibility = View.VISIBLE
            }
            if(dataModel.edit_rewrite==true){
                val edit_view = itemView.findViewById<ImageView>(R.id.edit_msg_id_)
                edit_view.visibility = View.VISIBLE
            }
            if(dataModel.edit_rewrite == false){
                val edit_view = itemView.findViewById<ImageView>(R.id.edit_msg_id_)
                edit_view.visibility = View.GONE
            }
            if(dataModel.remainder!="none"){
                val remainder_ = itemView.findViewById<ImageView>(R.id.remainder_show_in_chat_)
                remainder_.visibility = View.VISIBLE
            }
        }

        fun bind_image_me(dataModel : group_message_model){
            val _data : image_data_model = mapper.readValue<image_data_model>(dataModel.data)
            val local_url = _data.local_url
            val download_url = _data.download_uri

            /* if local url doesnot exist please download this url from the server */

            val image_view = itemView.findViewById<AppCompatImageView>(R.id.image_show_id_)
            Picasso.with(context_).load(local_url).resize(600,600).into(image_view)

            // if you send text with the image then it will show in this textView part
            // after clicking the image show it in big image View
            image_view.setOnClickListener{
                val intent = Intent(context_, FullScreenImageActivity::class.java)
                intent.putExtra("+image",local_url.toString())
                intent.putExtra("+private_chat", PRIVATE_CHAT_.toString())
                context_.startActivity(intent)
            }
        }

        fun bind_replay_me(dataModel: group_message_model){
            Log.d("","rrrrrrrrreplay message template is binded!")
            val data_ = mapper.readValue<replay_data_model>(dataModel.data)    // parsing replay data

            val replied_msg = data_.reply_message
            itemView.findViewById<TextView>(R.id.replied_msg).text = replied_msg   // the replied message is attached

            val selected_msg = data_.text_
            itemView.findViewById<TextView>(R.id.replay_of_msg).text = selected_msg   // the message which is selected to reply
            itemView.findViewById<TextView>(R.id.sender_name).text = "You"            // you send this this message indicator
        }

        fun BIND(dataModel : group_message_model){
            if(DISPLAY_STATUS== MESSAGE_FROM_ME)bind_msg_me(dataModel)
            if(DISPLAY_STATUS== IMAGE_FROM_ME)bind_image_me(dataModel)
//            if(DISPLAY_STATUS== TEMPLATE_ME_VOTE) bind_vote_me(dataModel)
            if(DISPLAY_STATUS== REPLAY_TEMPLATE_ME)bind_replay_me(dataModel)
//            if(DISPLAY_STATUS== STICKER_FROM_ME) bind_sticker_me(dataModel)
        }

        // used for the long pressed function in group_chat_activity()
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>(){
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey():Long = itemId
        }

    }

}