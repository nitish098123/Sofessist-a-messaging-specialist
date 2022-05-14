package com.example.database_part_3.message_holder
/*
  created on 3/3/2022 by Nitish Kr Boro
  this is brain of chat screen for pairs
*/

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.comments_view.comment_view_section
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.model.comment
import com.example.database_part_3.model.reaction_store_model
import com.example.database_part_3.model.universal_model
import com.example.database_part_3.model.voting_template
import com.example.database_part_3.user_info.screen_activity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_details.*
import java.util.*

private const val MY_NUMBER : Long = 6900529357
class DataAdapter(val context : Context) : RecyclerView.Adapter<DataAdapter.DataAdapterViewHolder>(){
    private var LOVED_REACTION_TEMPLATE : Boolean = false
    private val adapterData = mutableListOf<universal_model.one_chat_property>()
    private var first_click : Long = 0       // this is used for one click event
    private val DELTA_TIME_DOUBLE_CLICK = 400       //  this 1000 ms is the time between the two click of double click
    var status : Boolean = true                  // true or false storage of love or not of reactions template
    val mapper = jacksonObjectMapper()
    var tracker : SelectionTracker<Long>? = null
    private var UP_VOTE : Boolean = false
    private var DOWN_VOTE : Boolean = false

    init {
        setHasStableIds(true)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
        val layout = when (viewType) {
            TYPE_ME -> R.layout.my_message
            TYPE_OTHER -> R.layout.other_message
            TOOL_BAR -> R.layout.presshold_selection_me
            IMAGE_ME ->R.layout.image_show
            REPLAY_MESSAGE ->R.layout.replay_showing_layout
            REACTION_TAMPLATE -> R.layout.reaction_template
            VOTING_TAMPLATE -> R.layout.vote_template
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return DataAdapterViewHolder(view)
    }

    //    payload_ : MutableList<Any>
    override fun onBindViewHolder(holder : DataAdapterViewHolder,position: Int){
        val _number: universal_model.one_chat_property = adapterData[position]
        tracker?.let {
            holder.bind(_number, it.isSelected(position.toLong()), position)
        }
    //        super.onBindViewHolder(holder,position,payload_)
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
                type_in_int = REACTION_TAMPLATE
            }
            if(adapterData[position].template=="voting"){
                type_in_int = VOTING_TAMPLATE
            }
            if(adapterData[position].category=="i"){       // me image showing
                type_in_int = IMAGE_ME
            }
            if(adapterData[position].replied_msg!="no"){
                type_in_int = REPLAY_MESSAGE
            }
            if(adapterData[position].category=="m"){   // this is for me message layoyt
                type_in_int = TYPE_ME
            }
        }
        else{
            type_in_int = TYPE_OTHER
        }
        Log.d("","tttttttttemplate of present is:${type_in_int}")
        return type_in_int
    }

    fun setData(data : ArrayList<universal_model.one_chat_property>){      // this take the message to be show in the screen
        adapterData.apply {
            addAll(data)
        }
        notifyDataSetChanged()
    }

    fun delete_message(position: Int){    // delete messages from recycle view list
        adapterData.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position,adapterData.size)
    }

    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        private fun bind_me(item : universal_model.one_chat_property){                         //Do your view assignment here from the data model
            itemView.findViewById<AppCompatTextView>(R.id.txtMyMessage_me)?.text = item.data           // id should must be for fragment of recycler view
            itemView.findViewById<AppCompatTextView>(R.id.my_message_time)?.text = item.time_       // Ataching time at when the message is sended

            // making features invisible
            if(item.lock==false){
                val _image: ImageView = itemView.findViewById<AppCompatImageView>(R.id.lock_showing_msg_id)
                _image.visibility = View.GONE
            }
            if(item.stared==false){
                val _image: ImageView = itemView.findViewById<AppCompatImageView>(R.id.stared_msg_id_message)
                _image.visibility = View.GONE
            }
            if(item.edit_rewrite == false){
                val _icon: ImageView = itemView.findViewById<AppCompatImageView>(R.id.edit_msg_id)
                _icon.visibility = View.GONE
            }
            if(item.remainder=="none"){
                val _remainder: ImageView = itemView.findViewById<AppCompatImageView>(R.id.remainder_show_in_chat)
                _remainder.visibility = View.GONE
            }
        }

        private fun bind_other(item: universal_model.one_chat_property) {                          //Do your view assignment here from the data model
            itemView.findViewById<AppCompatTextView>(R.id.txtOtherUser)?.text = item.data
        }

        private fun bind_image_me(item : universal_model.one_chat_property){
            val uri_: Uri = Uri.parse(item.data)
            val image_view = itemView.findViewById<AppCompatImageView>(R.id.image_show_id)
            Picasso.with(context).load(uri_).resize(300,400).into(image_view)

            // after clicking the image show it in big image View
            image_view.setOnClickListener{
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.setData(uri_)
                context.startActivity(intent)
            }
        }

        private fun bind_replied_msg(item : universal_model.one_chat_property){
            itemView.findViewById<AppCompatTextView>(R.id.replied_msg)?.text = item.data                     // you replied message of previous message
            if(item.replied_msg!="no"){
                Log.d("rrrrrrrrrr index","${item.replied_msg.toInt()}")
                var reply_of_message = adapterData[item.replied_msg.toInt()]
                itemView.findViewById<AppCompatTextView>(R.id.replay_of_msg)?.text = reply_of_message.data   // the text that you want to replay
            }
        }

        private fun bind_voting_template(dataModel: universal_model.one_chat_property){
            var item : voting_template = mapper.readValue<voting_template>(dataModel.data)
            itemView.findViewById<AppCompatTextView>(R.id.show_time_)?.text = "${System.currentTimeMillis()}"
            var up_vote = itemView.findViewById<AppCompatTextView>(R.id.up_vote_number_)
            up_vote.text = "${item.total_up_vote}"
            var down_vote = itemView.findViewById<AppCompatTextView>(R.id.down_vote_number_)
            down_vote.text = "${item.total_down_vote}"
            itemView.findViewById<AppCompatTextView>(R.id.topic_show_in_vote_bar)?.text = item.topic

            val show_up: ImageView = itemView.findViewById<AppCompatImageView>(R.id.up_vote_icon)
            val show_down: ImageView = itemView.findViewById<AppCompatImageView>(R.id.down_vote_icon_bar_)

            show_up.setOnClickListener{
                 var tt = 0
                 if(UP_VOTE==true){       // for removing up vote in template
                    show_up.setImageResource(R.drawable.up_vote)       // showing the color of upvote
                    var total_ = item.total_up_vote
                    total_--
                    up_vote.text = "$total_"
                    UP_VOTE=false
                    tt++
                    item.total_up_vote = total_
                    item.your_vote = 0
//                    update_db_vote(item,dataModel.pair,dataModel.msg_num)
                 }
                 if(tt==0){
                    if(UP_VOTE == false){          // for up vote in template
                       show_up.setImageResource(R.drawable.up_vote_color)       // showing the color of upvote
                       var total_ = item.total_up_vote
                       total_++
                       up_vote.text = total_.toString()
                       UP_VOTE = true
                       item.your_vote = 1
                       item.total_up_vote = total_
                       var down_vote_num = item.total_down_vote

                       if(DOWN_VOTE==true){        // user had already given the downvote
                           show_down.setImageResource(R.drawable.down_vote)
                           down_vote_num--
                           down_vote.text = "$down_vote_num"
                           DOWN_VOTE = false
                       }
                       item.total_down_vote = down_vote_num
//                       update_db_vote(item,dataModel.pair,dataModel.msg_num)
                    }
                 }
            }

            show_down.setOnClickListener{
                var tt = 0
                if(DOWN_VOTE==true){       // for removing up vote in template
                    show_down.setImageResource(R.drawable.down_vote)       // showing the color of upvote
                    var total_ = item.total_down_vote
                    total_--
                    up_vote.text = total_.toString()
                    DOWN_VOTE=false
                    tt++
                    item.your_vote = 0
                    item.total_down_vote = total_
                    Log.d("","^^^^^^^down voting & total down vote: $total_")
//                    update_db_vote(item,dataModel.pair,dataModel.msg_num)
                }
                if(tt==0){
                    if(DOWN_VOTE==false){          // for up vote in template
                        show_down.setImageResource(R.drawable.down_vote_with_color)       // showing the color of upvote
                        var total_ = item.total_down_vote
                        total_++
                        DOWN_VOTE=true
                        down_vote.text = "$total_"
                        item.total_down_vote= total_
                        var up_vote_number = item.total_up_vote
                        item.your_vote = 2

                        if(UP_VOTE==true){        // user had already given the downvote
                             up_vote_number--
                             show_up.setImageResource(R.drawable.up_vote)
                             up_vote.text = "$up_vote_number"
                             item.total_up_vote = up_vote_number
                             UP_VOTE = false
                        }
                        Log.d("","********down vote adding & total down vote:$total_")
//                  update_db_vote(item,dataModel.pair,dataModel.msg_num)
                    }
                }
            }

            // this is for when you donot respond in the any of the likes
            if(item.your_vote==0){
               show_up.setImageResource(R.drawable.up_vote)
               show_down.setImageResource(R.drawable.down_vote)
            }
            if(item.your_vote==1){
                show_up.setImageResource(R.drawable.up_vote_color)
            }
            if(item.your_vote==2){
                show_down.setImageResource(R.drawable.down_vote_with_color)
            }
        }

        private fun bind_reaction_template(item : universal_model.one_chat_property){

            var data_: reaction_store_model = mapper.readValue<reaction_store_model>(item.data)

            itemView.findViewById<AppCompatTextView>(R.id.show_topic_id_)?.text = data_.topic
            val like_number = itemView.findViewById<AppCompatTextView>(R.id.total_liked_number_)
            like_number.text = data_.total_like.toString()
            val total_comment_number = itemView.findViewById<AppCompatTextView>(R.id.total_number_comment_)
            total_comment_number.text = data_.total_comment.size.toString()

            val like_icon : ImageView = itemView.findViewById<ImageView>(R.id.like_icon_id_)
            val comment_icon : ImageView = itemView.findViewById<AppCompatImageView>(R.id.comment_icon_id)

            like_icon.setOnClickListener{         //when clicked to the love icon
                var t=0
                if(LOVED_REACTION_TEMPLATE==false){    // clicking to like the thread
                    like_icon.setImageResource(R.drawable.love_with_color)
                    var total_love_num = data_.total_like
                    total_love_num++
                    LOVED_REACTION_TEMPLATE = true
                    like_number.text = "$total_love_num"
                    data_.total_like = total_love_num    // new updated data for saving to db
                    t++
                }
                if(t==0){
                    if(LOVED_REACTION_TEMPLATE==true){    // clicking to remove love sign
                        like_icon.setImageResource(R.drawable.love_with_out_color)
                        var total_lik_num = data_.total_like
                        total_lik_num--
                        like_number.text = "$total_lik_num"
                        data_.total_like = total_lik_num
                        LOVED_REACTION_TEMPLATE = false
                    }
                }

                val db = universal_chat_store(context,null)

                val thread : Thread = Thread({
                    val str = mapper.writeValueAsString(data_)
//                    db.update_reaction_template("you_liked",str,item.pair,item.msg_num)     // for updating the like of template
                })
                thread.start()
                db.close()
            }

            // for clicking effect of comments icon in the template
            val comment_str : String = mapper.writeValueAsString(data_)
            comment_icon.setOnClickListener{
                val intent : Intent = Intent(context , comment_view_section::class.java)
                intent.putExtra("+comments",comment_str)        // passing data of comments
                intent.putExtra("+sender_name",item.from)       // passing the sender name
                intent.putExtra("+pair_comment","${item.pair}|${item.msg_num}")    // passing the pair of one comments in reactions store
                intent.putExtra("+PAIR",item.pair)              // sending pair of this thread
                intent.putExtra("+msg_number",item.msg_num)    // sending the pure message nmber of the thread
                context.startActivity(intent)
            }

            if(data_.you_liked==true){
                like_icon.setImageResource(R.drawable.love_with_color)
            }
            if(data_.you_liked==false){
                like_icon.setImageResource(R.drawable.love_with_out_color)
            }

            if(item.lock==false){
                val _image: ImageView = itemView.findViewById<AppCompatImageView>(R.id.lock_showing_msg_id)
                _image.visibility = View.GONE
            }
            if(item.stared==false){
                val _image: ImageView = itemView.findViewById<AppCompatImageView>(R.id.stared_msg_id)
                _image.visibility = View.GONE
            }
            if(item.remainder=="none"){
                val _remainder: ImageView = itemView.findViewById<AppCompatImageView>(R.id.remainder_show_in_chat)
                _remainder.visibility = View.GONE
            }
        }

        fun bind(dataModel : universal_model.one_chat_property , isActivated : Boolean = false , position : Int){
            itemView.isActivated = isActivated                                  // here if the items is selected then it is true & if not selected then false
            if (dataModel.from == MY_NUMBER){
                var t = 0
                if (dataModel.template != "none"){
                    ++t
                    if (dataModel.template == "storing_reaction") {  // for the reactions storing template
                        bind_reaction_template(dataModel)          // this is the json data
                    }

                    if (dataModel.template == "voting") {          // for the voting store template
//                        val data_: voting_template = mapper.readValue<voting_template>(dataModel.data)    // all set data for voting data
                        bind_voting_template(dataModel)            // all the required data for voting data
                    }
                }

                if (t == 0) if (dataModel.category == "i") {     // this means image showing tamplate should use
                    t++
                    bind_image_me(dataModel)
                }

                if(t == 0)if(dataModel.category == "m") {
                    t++
                    if(dataModel.replied_msg != "no"){                   // for replay of message
                        bind_replied_msg(dataModel)
                    } else {                                // for text message show
                        bind_me(dataModel)
                    }
                }
            }
            if (dataModel.from != MY_NUMBER){
                bind_other(dataModel)
            }

            // below is for double click features
            itemView.setOnClickListener{                                                   // for the double click event

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
                            _used++
                        }
                    }

                    if(_used==0){
                        if(now_ - first_click >= DELTA_TIME_DOUBLE_CLICK){
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

        private fun update_db_vote( new_value : voting_template , pair_ : String , msg_number: String){
            val DB = universal_chat_store(context,null)
            val new_data : String = mapper.writeValueAsString(new_value)
            DB.update_voting_template(new_data,pair_,msg_number)
        }

        fun update_bing(dataModel : universal_model.one_chat_property){
            if(dataModel.remainder!="none"){     // i.e the remainder button is updated
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>(){
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey():Long = itemId
        }
    }
}
