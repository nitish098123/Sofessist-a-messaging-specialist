package com.example.database_part_3.message_holder
/*
  created on 3/3/2022 by Nitish Kr Boro
  this is brain of chat screen for pairs
*/

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.model.universal_model
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.message_details.*
import java.util.*


private const val MY_NUMBER : Long = 6900529357
class DataAdapter(val context : Context) :
    RecyclerView.Adapter<DataAdapter.DataAdapterViewHolder>(){

    private val adapterData = mutableListOf<universal_model.one_chat_property>()
    private var first_click : Long = 0       // this is used for one click event
    private val DELTA_TIME_DOUBLE_CLICK = 400       //  this 1000 ms is the time between the two click of double click
    private var image_store : ArrayList<Uri> = ArrayList<Uri>()
    private var image_count = 0

    var tracker : SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    //--------onCreateViewHolder: inflate layout with view holder-------
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val layout = when (viewType) {
            TYPE_ME  -> R.layout.my_message
            TYPE_OTHER -> R.layout.other_message
            TOOL_BAR -> R.layout.presshold_selection_me
            IMAGE_ME ->R.layout.image_show
            REPLAY_MESSAGE ->R.layout.replay_showing_layout
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return DataAdapterViewHolder(view)
    }

    //-----------onBindViewHolder: bind view with data model---------
   override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int, payload_ : MutableList<Any>) {

        val _number: universal_model.one_chat_property = adapterData[position]
        tracker?.let {
            holder.bind(_number, it.isSelected(position.toLong()), position)
        }
        if(payload_.isNotEmpty()){                                                         // when specific item is updated with payload e.g. edit_write,remiander,stared etc

        } else{
            super.onBindViewHolder(holder,position,payload_)
        }
   }

   fun position_to_type(index : Long) : String{         // this will give the selected datatypes of selected positions
        val item_data = adapterData[index.toInt()]
        var data_type : String = ""
        if(adapterData[index.toInt()].from==MY_NUMBER){
            if(adapterData[index.toInt()].template=="storing_reaction"){
                // give the storing_reactions layout
            }
            if(adapterData[index.toInt()].template=="voting"){
                // show the voting template
            }
            else{
                data_type = "TYPE_ME"
            }
        }
        else{
            data_type = "TYPE_OTHER"
        }
        return  data_type
    }

   override fun getItemCount() : Int = adapterData.size

   override fun getItemId(position: Int) : Long = position.toLong()

   override fun getItemViewType(position: Int): Int {          // this is classify which layout should be present in the chat_screen
       var type_in_int : Int = 0
       if(adapterData[position].from==MY_NUMBER){

           if(adapterData[position].template=="storing_reaction"){
               // give the storing_reactions layout
           }
           if(adapterData[position].template=="voting"){
               // show the voting template
           }
           if(adapterData[position].category=="i"){       // me image showing
               type_in_int = IMAGE_ME
           }
            if(adapterData[position].replied_msg!="no"){
                type_in_int = REPLAY_MESSAGE
            }
           else{   // this is for me message layoyt
               type_in_int = TYPE_ME
           }
       }
       else{
           type_in_int = TYPE_OTHER
       }
       return type_in_int
}

   fun setData(data : ArrayList<universal_model.one_chat_property>){      // this take the message to be show in the screen
        adapterData.apply {
            addAll(data)
        }
       notifyDataSetChanged()
   }

   // make another setData for other users
   companion object {
        private const val TYPE_ME = 0
        private const val TYPE_OTHER = 1
        private const val TYPE_HEADER = 3
        private const val TOOL_BAR = 4
        private const val VOTING_TAMPLATE = 5
        private const val REACTION_TAMPLATE = 6
        private const val IMAGE_ME = 7
        private const val REPLAY_MESSAGE = 8
   }

inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        private fun bind_me(item : universal_model.one_chat_property) {                         //Do your view assignment here from the data model
        itemView.findViewById<AppCompatTextView>(R.id.txtMyMessage)?.text = item.data           // id should must be for fragment of recycler view
        itemView.findViewById<AppCompatTextView>(R.id.my_message_time)?.text = item.time_       // Ataching time at when the message is sended
        }

        private fun bind_other(item: universal_model.one_chat_property) {                          //Do your view assignment here from the data model
        itemView.findViewById<AppCompatTextView>(R.id.txtOtherUser)?.text = item.data
        }
        private fun bind_image_me(item : universal_model.one_chat_property) {
  //      itemView.findViewById<AppCompatImageView>(R.id.image_view)?.setImageURI(item.data.toUri())

        image_count++
        image_store.add(item.data.toUri())
        if (image_count == TOTAL_SELECT_IMAGE){
           val fliping_image = itemView.findViewById<ViewFlipper>(R.id.fliper_id)
           for (i in image_store){
             if (fliping_image!= null) {
             var imageView = ImageView(context)
             imageView.setImageURI(i)
             fliping_image.addView(imageView)
           } }
                val _anim: Animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
                fliping_image.setInAnimation(_anim)         // setAnimations for flipin
                fliping_image.flipInterval = 1000          // 1sec interval between fliping
                fliping_image.isAutoStart = true          // autoatically start fliping
           }
        }
        private fun bind_replied_msg(item : universal_model.one_chat_property){
            itemView.findViewById<AppCompatTextView>(R.id.replied_msg)?.text = item.data                     // you replied message of previous message
            if(item.replied_msg!="no"){
                Log.d("rrrrrrrrrr index","${item.replied_msg!!.toInt()}")
                var reply_of_message = adapterData[item.replied_msg.toInt()]
                itemView.findViewById<AppCompatTextView>(R.id.replay_of_msg)?.text = reply_of_message.data   // the text that you want to replay
            }
        }

        fun bind(dataModel : universal_model.one_chat_property , isActivated : Boolean = false , position : Int){
        itemView.isActivated = isActivated                                  // here if the items is selected then it is true & if not selected then false

            if(dataModel.from==MY_NUMBER){
                if(dataModel.template=="reaction_store"){
                    // make template for reactions store
                }
                if(dataModel.template=="voting"){
                    // make template for voting
                }
                if(dataModel.category=="i"){     // this means image showing tamplate should use
                   bind_image_me(dataModel)
                }
                if(dataModel.replied_msg!="no"){
                    bind_replied_msg(dataModel)
                }
                else{
                    bind_me(dataModel)
                }
            }
            if(dataModel.from!=MY_NUMBER){
                bind_other(dataModel)
            }

            // below is for double click features
            itemView.setOnClickListener {                                                   // for the double click event

                var _used = 0
                if(first_click!=0L){
                    val now_ : Long = System.currentTimeMillis()
                    if(_used==0) {
                        if (now_ - first_click <= DELTA_TIME_DOUBLE_CLICK) {
                            val sheet: BottomSheetDialog = BottomSheetDialog(context)
                            sheet.setContentView(R.layout.message_details)
                            sheet.show_detail_text.setText("${dataModel.data}")
                            sheet.seen_time_id.setText("${dataModel.read}")         /* this should be the seen time of this message of opposite one */
                            sheet.show()
                            first_click = 0
                            Toast.makeText(context, "inside the double_click function",Toast.LENGTH_SHORT).show()
                            _used++
                        }
                    }

                    if(_used==0){
                        if (now_ - first_click >= DELTA_TIME_DOUBLE_CLICK) {
                            first_click=0
                            Toast.makeText(context,"Outside double_click function",Toast.LENGTH_SHORT).show()
                        }
                    }
                  _used++
                }
                if(_used==0){
                    if(first_click==0L){
                        first_click = System.currentTimeMillis()
                    }
                }
            }
       }

    fun update_bing(dataModel : universal_model.one_chat_property){
        if(dataModel.remainder!="none"){     // i.e the remainder button is updated

        }
    }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>(){
          override fun getPosition(): Int = adapterPosition
          override fun getSelectionKey():Long? = itemId
        }
    }
}