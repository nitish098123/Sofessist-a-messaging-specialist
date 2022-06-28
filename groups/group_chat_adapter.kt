package com.example.database_part_3.groups

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.ContactsContract
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.add_member.add_member_model
import com.example.database_part_3.groups.remove_person.remove_person_model
import com.example.database_part_3.groups_template_section.comment_section_group
import com.example.database_part_3.image_upload.upload_to_firebase
import com.example.database_part_3.image_upload.upload_to_firebase_1
import com.example.database_part_3.media_path.URIPathHelper
import com.example.database_part_3.message_holder.PRIVATE_CHAT
import com.example.database_part_3.message_holder.chat_activity
import com.example.database_part_3.model.*
import com.example.database_part_3.vedio_player.play_vedio_large
import com.example.database_part_3.vedio_player.video_model
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.like.LikeButton
import com.squareup.picasso.Picasso
import java.io.File


val MY_NUMBER : Long = 6900529357
class group_chat_adapter ( private val context_ : Context,
                           private val onContactClickListener: OnContactClickListener,
                           private var msg_number_position : HashMap<String,Int>,         // [message_number]= arrayList_positions
                           private  val GROUP_NUMBER  : String
                         )
    : RecyclerView.Adapter<group_chat_adapter.DataAdapterViewHolder>(){

    var renderer : PdfRenderer? = null
    var kk=0
    private lateinit var upload : upload_to_firebase
    var tracker : SelectionTracker<Long> ? = null
    var DISPLAY_STATUS = 0                // this will decide which layout should be bind in binding holder
    private val mapper = jacksonObjectMapper()
    private var LOVED_REACTION_TEMPLATE : Boolean = false
    private var adapterData = ArrayList<group_message_model>()
    var BITMAP_ : Bitmap? = null
    var ON_UPLOADING_PROCESS : Boolean = false                 // if the image is uploading then it will true
    var FIRST_POSITION = 0
    var upload_ : upload_to_firebase_1 ? = null

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
        val REMOVED_PERSON = 12
        val MEMBER_ADDED = 13
        val IMAGE_PROGRESS_SHOW_UPLOAD = 14
        val VIDEO_PLAY = 15
        val TEXT_IMAGE = 16
        val IMAGE_TEXT = 17
        val IMAGE_IMAGE = 18     // to reply image with replied message as image
        val DOCUMENT = 19        // any kind of document WAV , doc etc the layout will be the same
        val PDF = 20             // this is only for PDF type of file
        val TEXT_PDF = 21
        val TEXT_DOC = 22
        val IMAGE_PDF = 23
        val IMAGE_DOC = 24
        val SHARE_CONTACT = 25
        val SHARE_GROUP = 26
    }

    // function that takes the input messages from the AppcompatActivity
    fun setData(data : ArrayList<group_message_model>){      //  we should always give the List in parameter
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
            adapterData[position].edit_rewrite = true
        }

        if(operator_=="UPLOAD_PROGRESS"){
            if(adapterData[position].replied_msg=="none" || adapterData[position].replied_msg=="TEXT_IMAGE" || adapterData[position].replied_msg=="IMAGE_TEXT"){
                val parse_data = mapper.readValue<image_data_model>(adapterData[position].data)
                parse_data.upload_progress = data
                adapterData[position].data = mapper.writeValueAsString(parse_data)           // Updating the message getting error
            }

            // selected message: image and replied message: image
            if(adapterData[position].replied_msg=="IMAGE_IMAGE"){

            }
        }

        notifyItemChanged(position)
    }


    // broadcast reciever of the database query changed
    private val broad_cast_receiver_ = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?){
            val progress_ = intent!!.getStringExtra("PROGRESS")
            val get_group_number_ = intent.getStringExtra("GROUP_NUMBER")
            val get_message_number = intent.getStringExtra("MESSAGE_NUMBER")

            if(get_group_number_ == GROUP_NUMBER){           // even if this is runnable for the different and group number will not match and will not showed
                update_list("UPLOAD_PROGRESS" , progress_!! , msg_number_position[get_message_number]!!     //  this gives the positions in ArrayList positions from the message number
                )
            }
        }
    }

    // sending the view types for he each message
    override fun getItemViewType(position: Int) : Int {
        var type_in_int : Int = 0

        if(adapterData[position].from== MY_NUMBER){
            if(adapterData[position].category=="g_chat")type_in_int = MESSAGE_FROM_ME               // this is for me message layout
            if(adapterData[position].category=="storing_reaction")type_in_int = TEMPLATE_ME_STORING_REACTION
            if(adapterData[position].category=="voting")type_in_int = TEMPLATE_ME_VOTE
            if(adapterData[position].category=="g_i")type_in_int = IMAGE_FROM_ME                    // me image showing
            if(adapterData[position].replied_msg=="yes")type_in_int = REPLAY_TEMPLATE_ME
            if(adapterData[position].category=="sticker")type_in_int = STICKER_FROM_ME              // this means this layout is sticker
            if(adapterData[position].category=="removed_person")type_in_int = REMOVED_PERSON        // this means someone removed a person from group
            if(adapterData[position].category=="member_added") type_in_int = MEMBER_ADDED
            if(adapterData[position].category=="image_process_upload") type_in_int = IMAGE_PROGRESS_SHOW_UPLOAD    // showing the progress of image
            if(adapterData[position].category=="g_v") type_in_int = VIDEO_PLAY
            if(adapterData[position].replied_msg=="IMAGE_TEXT") type_in_int = IMAGE_TEXT            // if selected message is image and replied message is text
            if(adapterData[position].replied_msg=="TEXT_IMAGE") type_in_int = TEXT_IMAGE            // if selected message is text and replied one is image
            if(adapterData[position].replied_msg=="VIDEO_IMAGE") type_in_int = TEXT_IMAGE           // just attach the video thumbnail in the place of ImageView
            if(adapterData[position].replied_msg=="IMAGE_VIDEO") type_in_int = TEXT_IMAGE
            if(adapterData[position].replied_msg=="VIDEO_TEXT") type_in_int = IMAGE_TEXT            // just use the thumbnail in plac eof imageView and attach the text in text View
            if(adapterData[position].replied_msg=="TEXT_VIDEO") type_in_int = TEXT_IMAGE
            if(adapterData[position].replied_msg=="IMAGE_IMAGE") type_in_int = IMAGE_IMAGE
            if(adapterData[position].category=="g_doc") type_in_int = DOCUMENT
            if(adapterData[position].category=="PDF")  type_in_int = PDF                            // this is just only for pdf openner
            if(adapterData[position].category=="text_PDF") type_in_int = TEXT_PDF                   // selected message is text and replay is pdf
            if(adapterData[position].category=="image_PDF") type_in_int = IMAGE_PDF                 // selected msg is image and replay is pdf
            if(adapterData[position].category=="text_g_doc")  type_in_int = TEXT_DOC                  // selected msg is text and replay is doc
            if(adapterData[position].category=="image_g_doc") type_in_int = IMAGE_DOC                 // selected msg is image replay is doc
            if(adapterData[position].category=="contact") type_in_int = SHARE_CONTACT              // if share contacts
            if(adapterData[position].category=="group_share") type_in_int = SHARE_GROUP             // when some one share group to join that

        }

        else{
             type_in_int = MESSAGE_FROM_OTHERS
        }
        Log.d("","vvvvvvvvvvview type of the category of message is:${adapterData[position].category}")
        DISPLAY_STATUS = type_in_int
        return type_in_int
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataAdapterViewHolder{
            var _layout : Int = 0

            if(viewType== MESSAGE_FROM_ME)_layout = R.layout.my_message
            if(viewType== IMAGE_FROM_ME)_layout = R.layout.image_show
            if(viewType== IMAGE_PROGRESS_SHOW_UPLOAD )_layout = R.layout.image_show
            if(viewType== TEMPLATE_ME_STORING_REACTION)_layout = R.layout.reaction_template
            if(viewType== TEMPLATE_ME_VOTE)_layout = R.layout.vote_template
            if(viewType== REPLAY_TEMPLATE_ME || viewType== IMAGE_TEXT) _layout = R.layout.replay_showing_layout
            if(viewType== STICKER_FROM_ME) _layout = R.layout.sticker_layout
            if(viewType== REMOVED_PERSON) _layout = R.layout.removed_person
            if(viewType== MEMBER_ADDED) _layout = R.layout.member_added
            if(viewType== VIDEO_PLAY) _layout = R.layout.vedios_layout
            if(viewType== TEXT_IMAGE || viewType==IMAGE_IMAGE ) _layout = R.layout.image_replay_layout        // you have to put all image and video thumbnail
            if(viewType== DOCUMENT) _layout = R.layout.general_document_send          // for different kind of extension file to show
            if(viewType== PDF) _layout =  R.layout.pdf_view_item                      // for pdf view type only
            if(viewType== TEXT_PDF || viewType == IMAGE_PDF)_layout = R.layout.replay_pdf              // if the replied of is pdf only
            if(viewType== TEXT_DOC || viewType == IMAGE_DOC ) _layout = R.layout.replay_document       // if the replied message is some different document
            if(viewType == SHARE_CONTACT) _layout = R.layout.share_contacts

            val inflater = LayoutInflater.from(context_)
            val view = inflater.inflate(_layout, parent,false)
            return DataAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder : DataAdapterViewHolder , position: Int){
        val supply_data : group_message_model = adapterData[position]
        holder.itemView.setOnClickListener{
            onContactClickListener.onContactClickListener(position)
        }
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
            if(dataModel.stared==false){
                val star_msg = itemView.findViewById<ImageView>(R.id.stared_msg_id_message)
                star_msg.visibility = View.GONE
            }

            // For the visibility of the any icon you have to fit both the visible and gone part
            if(dataModel.edit_rewrite==true){
                Log.d("","EEEEEEEEEEEEdit and rewrite of msg_num :${dataModel.msg_num}")
                val edit_view = itemView.findViewById<ImageView>(R.id.edit_msg_id_)
                edit_view.visibility = View.VISIBLE
            }
            if(dataModel.edit_rewrite==false){
                val edit_view = itemView.findViewById<ImageView>(R.id.edit_msg_id_)
                edit_view.visibility = View.GONE
            }

            if(dataModel.remainder!="none"){
                val remainder_ = itemView.findViewById<ImageView>(R.id.remainder_show_in_chat_)
                remainder_.visibility = View.VISIBLE
            }
            if(dataModel.remainder=="none"){
                val remainder_ = itemView.findViewById<ImageView>(R.id.remainder_show_in_chat_)
                remainder_.visibility = View.GONE
            }

        }

        fun bind_image_me(dataModel : group_message_model){

                val _data: image_data_model = mapper.readValue<image_data_model>(dataModel.data)
                val local_url = _data.local_url
                Log.d("","uuuuuuuuuri of image is:${local_url}")
                val image_view = itemView.findViewById<AppCompatImageView>(R.id.image_show_id_)
                Picasso.with(context_).load(local_url).resize(800,800).centerCrop().into(image_view)

                val resume_upload = itemView.findViewById<ImageView>(R.id.retry_button_id)
                val progress_icon = itemView.findViewById<ProgressBar>(R.id.uploading_process_id)     // processing bar
                val download_background = itemView.findViewById<ImageView>(R.id.download_background_)      // background white color at download button
                val progress_text = itemView.findViewById<TextView>(R.id.uploaded_status_text)
                val cancel_upload = itemView.findViewById<ImageView>(R.id.cancel_upload_id)

                progress_text.visibility = View.VISIBLE
                download_background.visibility = View.GONE

                // if the uploading is not completed
                if(_data.upload_progress==""){
                    cancel_upload.visibility = View.VISIBLE
                    download_background.visibility = View.VISIBLE
                    progress_icon.visibility = View.VISIBLE
                    progress_icon.isIndeterminate = true
                    Thread({

                    upload_ =  upload_to_firebase_1(context_ , local_url , false , group_number , dataModel.msg_num ,"$MY_NUMBER")
                    upload_!!.upload_to_firebase("START")

                    Log.d("","ccccccccccccurent thread is:${Thread.currentThread()}")

                    }).start()
                    progress_icon.progress = 0
                    progress_text.setText("-1")   // this means just beginning
                }

//               in the middle of upload progress
                if(_data.upload_progress!="" && _data.upload_progress!="completed" && _data.upload_progress!="pause"){
                    download_background.visibility = View.VISIBLE
                    resume_upload.visibility = View.GONE
                    progress_icon.isIndeterminate = false
                    cancel_upload.visibility = View.VISIBLE
                    progress_icon.progress = _data.upload_progress.toInt()
                    progress_text.setText("${_data.upload_progress}%")
                }

                if(_data.upload_progress=="pause"){
                  cancel_upload.visibility = View.GONE
                  resume_upload.visibility = View.VISIBLE
                  progress_icon.visibility = View.GONE
                  progress_icon.isIndeterminate = false
                  download_background.visibility = View.VISIBLE
                  progress_text.setText("paused")
                }

                if(_data.upload_progress=="completed" || _data.upload_progress=="100%" || _data.upload_progress=="100"){    // when the process is completed
                  progress_icon.isIndeterminate = false
                  download_background.visibility = View.GONE
                  resume_upload.visibility = View.GONE
                  progress_icon.visibility = View.GONE
                  cancel_upload.visibility = View.GONE
                  progress_text.setText("completed")      // this is visible
               }

                // after clicking the image show it in big image View
              image_view.setOnClickListener{
                    val intent = Intent(context_, FullScreenImageActivity::class.java)
                    intent.putExtra("+image", local_url.toString())
                    intent.putExtra("+private_chat", PRIVATE_CHAT_.toString())
                    context_.startActivity(intent)
              }

              // canceling the upload progress  this means pause
              cancel_upload.setOnClickListener{
                  // the outer parameter is for initiating the uploading the image to server
//                  upload.image_uploading(local_url,"PAUSE",dataModel.msg_num,GROUP_NUMBER,"$MY_NUMBER",false,dataModel,"OUTER")

                  upload_!!.upload_to_firebase("PAUSE")
                  download_background.visibility = View.VISIBLE
                  resume_upload.visibility = View.VISIBLE
                  progress_icon.visibility = View.GONE
                  cancel_upload.visibility = View.GONE
              }

              // after clicking the resume button
              resume_upload.setOnClickListener{
                  resume_upload.visibility = View.GONE
                  progress_icon.visibility = View.VISIBLE
                  progress_icon.isIndeterminate = true
                  cancel_upload.visibility = View.VISIBLE
                  upload.image_uploading(local_url,"RESUME",dataModel.msg_num,GROUP_NUMBER,"$MY_NUMBER",false,dataModel,"OUTER")
              }
        }

        fun bind_replay_me(dataModel: group_message_model){

            Log.d("","ccccccccccchosssssing right function :")

            if(dataModel.replied_msg=="IMAGE_TEXT"){
                itemView.findViewById<TextView>(R.id.sender_name).text = "You"            // you send this this message indicator
                val data_ = mapper.readValue<replay_data_model>(dataModel.data)
                val image_view = itemView.findViewById<ImageView>(R.id.selected_image_show)     // this is selected image to be attached by uri
                val replied_text =  itemView.findViewById<TextView>(R.id.replied_msg)
                image_view.visibility = View.VISIBLE
                replied_text.visibility = View.VISIBLE
                replied_text.setText(data_.reply_message)                                     // this is text to replay of image message
                Picasso.with(context_).load(data_.text_.toUri()).resize(800,800).centerCrop().into(image_view)     // this is selected image to replay
            }

            if(dataModel.replied_msg=="yes"){     // this means text_text :- selected msg is text and replied messafe is also text
                val data_ = mapper.readValue<replay_data_model>(dataModel.data)    // parsing replay data
                val replied_msg = data_.reply_message
                val replied_message = itemView.findViewById<TextView>(R.id.replied_msg)   // the replied message is attached
                replied_message.visibility = View.VISIBLE
                replied_message.setText(replied_msg)
                val selected_msg = data_.text_
                val replay_text_ = itemView.findViewById<TextView>(R.id.replay_of_msg)
                replay_text_.visibility = View.VISIBLE
                replay_text_.setText(selected_msg)                                       // the message which is selected to reply
                itemView.findViewById<TextView>(R.id.sender_name).text = "You"            // you send this this message indicator
            }
        }

        fun bind_reaction_me(item : group_message_model){
            var data_ = mapper.readValue<reaction_store_group_model>(item.data)
            itemView.findViewById<AppCompatTextView>(R.id.show_topic_id_)?.text = data_.topic
            val like_number = itemView.findViewById<AppCompatTextView>(R.id.total_liked_number_)
            like_number.text = data_.total_like.toString()
            val total_comment_number = itemView.findViewById<TextView>(R.id.total_number_comment_)
            total_comment_number.text = data_.total_comment.size.toString()

            val like_icon = itemView.findViewById<LikeButton>(R.id.like_icon_id_)
            val comment_icon = itemView.findViewById<ImageView>(R.id.comment_icon_id)
            like_icon.isEnabled = true
            like_icon.setOnClickListener{         //when clicked to the love icon
                var t=0
                if(data_.you_liked==false){    // clicking to like the thread
//                    like_icon.setImageResource(R.drawable.love_with_color)
                    like_icon.isLiked = true     // this set the like button true
                    var total_love_num = data_.total_like
                    total_love_num++
                    LOVED_REACTION_TEMPLATE = true
                    like_number.text = "$total_love_num"
                    data_.you_liked = true
                    data_.total_like = total_love_num    // new updated data for saving to db
                    t++

                    // Now updating original array(adapterdata)
                    var old_data = mapper.readValue<reaction_store_group_model>(adapterData[position].data)
                    old_data.you_liked = true
                    old_data.total_like = total_love_num
                    val new_data_str = mapper.writeValueAsString(old_data)
                    adapterData[position].data = new_data_str

                    // updating  the reaction template in database
                    Log.d("","iiiiiiiiiiiinitial msg_number is:${item.msg_num}")
                    update_reaction_store(true,GROUP_NUMBER,item.msg_num)
                }


                if(t==0){
                    if(data_.you_liked==true){    // clicking to remove love sign
//                        like_icon.setImageResource(R.drawable.love_with_out_color)
                        like_icon.isLiked = false     // this will set the like button false
                        var total_lik_num = data_.total_like
                        total_lik_num--
                        like_number.text = "$total_lik_num"
                        data_.total_like = total_lik_num
                        LOVED_REACTION_TEMPLATE = false
                        data_.you_liked = false

                        // Now updating original array(adapterdata)
                        var old_data = mapper.readValue<reaction_store_group_model>(adapterData[position].data)
                        old_data.you_liked = false
                        old_data.total_like = total_lik_num
                        val new_data_str = mapper.writeValueAsString(old_data)
                        adapterData[position].data = new_data_str

                        // update new data to reaction_store_template
                        update_reaction_store(false,GROUP_NUMBER,item.msg_num)
                    }
                }
            }

            // for clicking effect of comments icon in the template
            val comment_str : String = mapper.writeValueAsString(data_)
            comment_icon.setOnClickListener{
                val intent  = Intent(context_, comment_section_group::class.java)
                intent.putExtra("+comments",comment_str)             // passing data of comments
                intent.putExtra("+sender_name","${item.from}")       // passing the sender name
                intent.putExtra("+msg_number",item.msg_num)         // sending the pure message nmber of the thread
                intent.putExtra("+private_chat","$PRIVATE_CHAT")    // if private chat then user cannot take screen shoot of chats
                intent.putExtra("+group_number","${item.group_number}")
                intent.putExtra("+topic",data_.topic)
                context_.startActivity(intent)
            }

            if(data_.you_liked==true){
                LOVED_REACTION_TEMPLATE = true
//                like_icon.setImageResource(R.drawable.love_with_color)
                 like_icon.isLiked = true
            }
            if(data_.you_liked==false){
                LOVED_REACTION_TEMPLATE = false
//                like_icon.setImageResource(R.drawable.love_with_out_color)
                like_icon.isLiked = false
            }
            if(item.stared==true){
                val _image: ImageView = itemView.findViewById<AppCompatImageView>(R.id.stared_msg_id)
                _image.visibility = View.VISIBLE
            }
            if(item.remainder!="none"){
                val _remainder: ImageView = itemView.findViewById<AppCompatImageView>(R.id.remainder_show_in_chat)
                _remainder.visibility = View.VISIBLE
            }
        }

        fun bind_removed_display(dataModel : group_message_model){    // when some one removed from group
            // this data will come directly from the local database

            val text_id = itemView.findViewById<TextView>(R.id.show_text_)
            val data = dataModel.data       // to get the persons name we have to parse it first
            val data_ = mapper.readValue<remove_person_model>(data)
            val from_ = data_.from        // person_phone_number who wants to remove another person
            val to_ = data_.to             // person_phone_number who has been removed
            text_id.setText(Html.fromHtml("<b>${from_}</b> removed <b>${to_}</b> from this group"))    // setting this text
        }

        fun bind_add_member(dataModel: group_message_model){
            val data_ = mapper.readValue<add_member_model>(dataModel.data)     //  who has added to whom this will parse data
            val show_in_text = itemView.findViewById<TextView>(R.id.member_added)
            show_in_text.setText(Html.fromHtml("<b>${data_.who_did}</b> added ${data_.added_person}"))
        }

        // for showing the vedio play
        fun bind_vedio_show(dataModel : group_message_model){
            val play_vedio = itemView.findViewById<ImageView>(R.id.play_button)
            val total_size = itemView.findViewById<TextView>(R.id.show_vedio_size)
            val time_of_send = itemView.findViewById<TextView>(R.id.show_time_id)
            val duration_time = itemView.findViewById<TextView>(R.id.video_duration_id)

            val data_ = mapper.readValue<video_model>(dataModel.data)       // all the information of specific video is parsed here
            val local_link = data_.local_link

            // thumbnail from uri
            val thumbnail_view = itemView.findViewById<ImageView>(R.id.thumbnail_view_id)
            Glide.with(context_).asBitmap().load(local_link).into(thumbnail_view);   // setting the thmbnail of vedio

            total_size.setText(data_.total_size)
            time_of_send.setText(dataModel.time_)       // seting the time of sending of vedio
            duration_time.setText(data_.total_time)

            play_vedio.setOnClickListener{
                val intent = Intent(context_,play_vedio_large::class.java)
                intent.putExtra("video_uri",data_.local_link)         // giving the local link for playing the vedio
                context_.startActivity(intent)
            }
            thumbnail_view.setOnClickListener{
                val intent = Intent(context_,play_vedio_large::class.java)
                intent.putExtra("video_uri",data_.local_link)         // giving the local link for playing the vedio
                context_.startActivity(intent)
            }

        }

        // this is binding the vote template
        fun bind_vote_me(dataModel : group_message_model){
            Log.d("","vvvvvvvvvvvvoting data for adapter is:${dataModel.data}")
            var item : voting_template = mapper.readValue<voting_template>(dataModel.data)
            itemView.findViewById<TextView>(R.id.show_time_)?.text = "${System.currentTimeMillis()}"
            var up_vote = itemView.findViewById<TextView>(R.id.up_vote_number_)
            up_vote.text = "${item.total_up_vote}"
            var down_vote = itemView.findViewById<AppCompatTextView>(R.id.down_vote_number_)
            down_vote.text = "${item.total_down_vote}"
            itemView.findViewById<AppCompatTextView>(R.id.topic_show_in_vote_bar)?.text = item.topic

            val show_up : ImageView = itemView.findViewById<AppCompatImageView>(R.id.up_vote_icon)
            val show_down: ImageView = itemView.findViewById<AppCompatImageView>(R.id.down_vote_icon_bar_)

            show_up.setOnClickListener{
                var tt = 0
                if(item.your_vote==1){       // for removing up vote in template
                    show_up.setImageResource(R.drawable.up_vote)       // showing the color of upvote
                    var total_ = item.total_up_vote
                    total_--
                    up_vote.text = "$total_"
                    tt++
                    item.total_up_vote = total_
                    item.your_vote = 0

                    // now updating the adapterData
                    var old_data = mapper.readValue<voting_template>(adapterData[position].data)
                    old_data.total_up_vote = total_
                    old_data.your_vote = 0
                    val new_data_str = mapper.writeValueAsString(old_data)
                    adapterData[position].data = new_data_str
                }
                if(tt==0){
                    if(item.your_vote != 1){          // for up vote in template
                        show_up.setImageResource(R.drawable.up_vote_color)       // showing the color of upvote
                        var total_ = item.total_up_vote
                        total_++
                        up_vote.text = total_.toString()
                        item.total_up_vote = total_
                        var down_vote_num = item.total_down_vote

                        // now updating the adapterData
                        var old_data = mapper.readValue<voting_template>(adapterData[position].data)
                        old_data.total_up_vote = total_
                        old_data.your_vote = 1

                        if(item.your_vote==2){        // user had already given the downvote
                            show_down.setImageResource(R.drawable.down_vote)
                            down_vote_num--
                            down_vote.text = "$down_vote_num"

                            old_data.total_down_vote = down_vote_num    // this update the adapter data
                        }
                        item.your_vote = 1
                        item.total_down_vote = down_vote_num

                        val new_data_str = mapper.writeValueAsString(old_data)
                        adapterData[position].data = new_data_str
                    } }
                update_db_vote(item,dataModel.msg_num)
            }

            show_down.setOnClickListener{
                var tt = 0
                if(item.your_vote==2){       // for removing up vote in template
                    show_down.setImageResource(R.drawable.down_vote)       // showing the color of upvote
                    var total_ = item.total_down_vote
                    total_--
                    down_vote.text = total_.toString()
                    tt++
                    item.your_vote = 0
                    item.total_down_vote = total_
                    Log.d("","^^^^^^^down voting & total down vote: $total_")

                    // now updating the adapterData
                    var old_data = mapper.readValue<voting_template>(adapterData[position].data)
                    old_data.total_down_vote = total_
                    old_data.your_vote = 0
                    val new_data_str = mapper.writeValueAsString(old_data)
                    adapterData[position].data = new_data_str
                }
                if(tt==0){
                    if(item.your_vote!=2){          // for up vote in template
                        show_down.setImageResource(R.drawable.down_vote_with_color)       // showing the color of upvote
                        var total_ = item.total_down_vote
                        total_++
                        down_vote.text = "$total_"
                        item.total_down_vote= total_
                        var up_vote_number = item.total_up_vote

                        // now updating the adapterData part
                        var old_data = mapper.readValue<voting_template>(adapterData[position].data)
                        old_data.total_down_vote = total_

                        if(item.your_vote==1){        // user had already given the downvote
                            up_vote_number--
                            show_up.setImageResource(R.drawable.up_vote)
                            up_vote.text = "$up_vote_number"
                            item.total_up_vote = up_vote_number

                            old_data.total_up_vote = up_vote_number   // for adapter data
                        }
                        old_data.your_vote = 2
                        item.your_vote = 2
                        Log.d("","ddddddddown vote fpr updating the group vote template:$total_")
                        val new_data_str = mapper.writeValueAsString(old_data)
                        adapterData[position].data = new_data_str
                    }
                }
                update_db_vote(item,dataModel.msg_num)
            }

            // this is for when you donot respond in the any of the likes
            if(item.your_vote==0){
                show_up.setImageResource(R.drawable.up_vote)
                show_down.setImageResource(R.drawable.down_vote)
            }
            if(item.your_vote==1){
                show_up.setImageResource(R.drawable.up_vote_color)
                show_down.setImageResource(R.drawable.down_vote)
            }
            if(item.your_vote==2){
                show_down.setImageResource(R.drawable.down_vote_with_color)
                show_up.setImageResource(R.drawable.up_vote)
            }
        }

        // for binding the text_image_video
        fun bind_replay_image_video_me(dataModel : group_message_model){
            val sender_name = itemView.findViewById<TextView>(R.id.sender_name)
            val selected_image = itemView.findViewById<ImageView>(R.id.selected_image_show)
            val selected_text = itemView.findViewById<TextView>(R.id.replay_of_msg)
            val replied_image = itemView.findViewById<ImageView>(R.id.image_show_id_)

            val item_ = dataModel.data

            sender_name.setText("You")
            if(dataModel.replied_msg=="TEXT_IMAGE"){        // this means selecting text and replied image
                selected_text.visibility = View.VISIBLE
                replied_image.visibility = View.VISIBLE

                val data_ = mapper.readValue<replay_data_model>(item_)
                val selected_text_ = data_.text_
                val replied_image_uri : Uri = data_.reply_message.toUri()   // this is replied image uri
                selected_text.setText(selected_text_)
                Picasso.with(context_).load(replied_image_uri).resize(800,800).centerCrop().into(replied_image)
            }

            if(dataModel.replied_msg=="IMAGE_IMAGE"){
                val data_ = mapper.readValue<image_data_model>(dataModel.data)
                selected_image.visibility = View.VISIBLE
                replied_image.visibility = View.VISIBLE
                val selected_uri = data_.local_url
                val replied_msg_uri = data_.text_     // the image that is uploaded

                Picasso.with(context_).load(selected_uri).resize(800,800).centerCrop().into(selected_image)
                Picasso.with(context_).load(replied_msg_uri).resize(800,800).centerCrop().into(replied_image)

//                upload.image_uploading()

//              selected_image.setImageURI(data_.text_.toUri())          // setting the image uri in selection of message
//              replied_image.setImageURI(data_.reply_message.toUri())   // set replied image to in replay layout
            }

            if(dataModel.category=="IMAGE_VIDEO"){

            }
            if(dataModel.category=="TEXT_STICKER"){

            }
            if(dataModel.category=="IMAGE_STICKER"){

            }
            if(dataModel.category=="VIDEO_STICKER"){

            }
        }


        // binding document
        fun bind_document(dataModel : group_message_model){
            val _data = mapper.readValue<documents_model>(dataModel.data)
            val _uri = _data.local_uri          // uri of file in the local disk
            val data_type = _data.file_type
            val file_size = _data.total_size
            val file_name = _data.file_name

            val whole_area = itemView.findViewById<RelativeLayout>(R.id.open_document_layout)
            val thumnail_show = itemView.findViewById<ImageView>(R.id.pdf_thumbnail_id)
            val file_name_show = itemView.findViewById<TextView>(R.id.pdf_name_id)
            val total_size_show = itemView.findViewById<TextView>(R.id.file_size_id)
            val file_type = itemView.findViewById<TextView>(R.id.file_type_id)         // show the file type in the layout

            file_name_show.setText(file_name)
            total_size_show.setText(file_size)
            file_type.setText(data_type)
            thumnail_show.setImageResource(R.drawable.files_logo)

            whole_area.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
//                val file_path = URIPathHelper().getPath(context_,_uri.toUri())
                intent.setDataAndType( _uri.toUri(), "application/${data_type}")
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                startActivity(context_,intent,null)
            }
        }


        // binding only pdf
        fun bind_pdf_document(dataModel : group_message_model){
            val _data = mapper.readValue<documents_model>(dataModel.data)      // this contains all the information of pdf document
            val _uri = _data.local_uri
            val file_size = _data.total_size
            val file_name = _data.file_name
            val total_pages_from_data = _data.total_pages       // this contains total_page number of pdf
            val front_image_of_pdf = _data.front_page_image     // this contains the link of front page image

            val thumnail_show = itemView.findViewById<ImageView>(R.id.pdf_thumbnail_id)
            val file_name_show = itemView.findViewById<TextView>(R.id.pdf_name_id)
            val total_size_show = itemView.findViewById<TextView>(R.id.file_size_id)
            val file_type = itemView.findViewById<TextView>(R.id.file_type_id)
            val total_page_pdf_view =  itemView.findViewById<TextView>(R.id.pages_pdf_id)

            file_name_show.setText(file_name)
            total_size_show.setText(file_size)

            val pdf_layout = itemView.findViewById<RelativeLayout>(R.id.pdf_layout_to_open)    // touch to open pdf
            pdf_layout.setOnClickListener{
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType( _uri.toUri(), "application/pdf")
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                startActivity(context_,intent,null)
            }

                file_type.setText("PDF")
                total_page_pdf_view.visibility = View.VISIBLE
                total_page_pdf_view.setText(total_pages_from_data)    // attaching total data
                Picasso.with(context_).load(front_image_of_pdf.toUri()).resize(800,800).centerCrop().into(thumnail_show)   // attching the thumbnail of pdf
        }


        // for replying the selected message with pdf not any other documents
        fun bind_replay_with_pdf(dataModel: group_message_model) {
            val _data = mapper.readValue<replay_media_model>(dataModel.data)      // this contains all the information of pdf document

            val selected_message_text = _data.selected_text_or_download_uri     // this will gibe text now
            val _uri = _data.replay_media_local_uri
            val file_size = _data.total_size_
            val file_name = _data.name_of_file
            val front_page_image = _data.front_page_image   // this link of bitmap
            val total_pages_ = _data.total_pages_           // total pages of pdf

            val thumnail_show = itemView.findViewById<ImageView>(R.id.pdf_thumbnail_id)
            val file_name_show = itemView.findViewById<TextView>(R.id.pdf_name_id)
            val total_size_show = itemView.findViewById<TextView>(R.id.file_size_id)
            val total_page_pdf_view = itemView.findViewById<TextView>(R.id.pages_pdf_id)
            val selected_text_msg = itemView.findViewById<TextView>(R.id.replay_of_msg)   // selected message to replay

            file_name_show.setText(file_name)
            total_size_show.setText(file_size)
            Picasso.with(context_).load(front_page_image.toUri()).resize(800, 800).centerCrop().into(thumnail_show)   // attching the thumbnail of pdf
            total_page_pdf_view.setText(total_pages_)

          if(dataModel.category=="text_PDF"){
            selected_text_msg.visibility = View.VISIBLE
            selected_text_msg.setText(selected_message_text)
          }

            if(dataModel.category=="image_PDF"){      // this attach image as replay of pdf
                val selected_image = itemView.findViewById<ImageView>(R.id.selected_image_show)
                selected_image.visibility = View.VISIBLE
                Picasso.with(context_).load(selected_message_text.toUri()).resize(800, 800).centerCrop().into(selected_image)   // attching the thumbnail of pdf
            }

                val pdf_layout = itemView.findViewById<RelativeLayout>(R.id.pdf_layout_to_open)    // touch to open pdf
                pdf_layout.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(_uri.toUri(), "application/pdf")
                    intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    startActivity(context_, intent, null)
                }

        }


        // bind_document of selected image or text
        fun bind_replay_with_doc(dataModel: group_message_model){
            val _data = mapper.readValue<replay_media_model>(dataModel.data)      // this contains all the information of pdf document

            val selected_message_text = _data.selected_text_or_download_uri     // this will gibe text now
            val _uri = _data.replay_media_local_uri                            // link of doc replied
            val file_size = _data.total_size_
            val file_name = _data.name_of_file
            val type_name = File(file_name).extension      // this will give the file type

            val file_name_show = itemView.findViewById<TextView>(R.id.pdf_name_id)
            val total_size_show = itemView.findViewById<TextView>(R.id.file_size_id)
            val selected_text_msg = itemView.findViewById<TextView>(R.id.replay_of_msg)   // selected message to replay
            val file_type = itemView.findViewById<TextView>(R.id.file_type_id)           // attach the file type id

            file_name_show.setText(file_name)
            total_size_show.setText(file_size)
            file_type.setText(type_name)

            if(dataModel.category=="text_g_doc"){
                selected_text_msg.visibility = View.VISIBLE
                selected_text_msg.setText(selected_message_text)
            }

            if(dataModel.category=="image_g_doc"){      // this attach image as replay of pdf
                val selected_image = itemView.findViewById<ImageView>(R.id.selected_image_show)
                selected_image.visibility = View.VISIBLE
                Picasso.with(context_).load(selected_message_text.toUri()).resize(800, 800).centerCrop().into(selected_image)   // attching the thumbnail of pdf
            }

            val pdf_layout = itemView.findViewById<RelativeLayout>(R.id.pdf_layout_to_open)    // touch to open pdf
            pdf_layout.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(_uri.toUri(), "application/$type_name")
                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                startActivity(context_, intent, null)
            }

        }

        // bind contacts
        fun bind_contacts(dataModel : group_message_model){
            val _data = mapper.readValue<choose_contacts_model>(dataModel.data)    // parsing the data model of contact

                var dp_uri = ""
                if (_data.display_picture != "") {
                    dp_uri = _data.display_picture    // link of display picture
                }
                val person_name = _data.name_

                /* download task of dp and save to database */

                val dp_show = itemView.findViewById<ImageView>(R.id.display_picture_id)
                val person_name_show = itemView.findViewById<TextView>(R.id.person_name_)
                person_name_show.setText(person_name)

               if(dp_uri!="")Glide.with(context_).load(dp_uri).apply { RequestOptions.circleCropTransform() }.into(dp_show)    // this will make the photo in the circle form
               val message_person = itemView.findViewById<TextView>(R.id.message_layout_id)

               message_person.setOnClickListener {
                   val intent = Intent(context_, chat_activity::class.java)
                   intent.putExtra("+name", person_name)
                   intent.putExtra("+number", _data.number_)
                   intent.putExtra("+private_chat", "false")
                   context_.startActivity(intent)
               }

              // for adding contact
              val add_contact = itemView.findViewById<TextView>(R.id.add_to_contact_id)
              add_contact.setOnClickListener {
                  val intent = Intent(Intent.ACTION_INSERT)
                  intent.setType(ContactsContract.Contacts.CONTENT_TYPE)
                  intent.putExtra(ContactsContract.Intents.Insert.NAME, person_name)
                  intent.putExtra(ContactsContract.Intents.Insert.PHONE, _data.number_)
                  context_.startActivity(intent)
              }

        }

        fun BIND(dataModel : group_message_model){
            kk++
            if(kk==1){
                LocalBroadcastManager.getInstance(context_).registerReceiver( broad_cast_receiver_,IntentFilter("UPLOAD_PROGRESS"))   // this is regestering the broadcast receive
            }
            // for uploading function
            upload = upload_to_firebase(context_)

            if(DISPLAY_STATUS== MESSAGE_FROM_ME) bind_msg_me(dataModel)
            if(DISPLAY_STATUS== IMAGE_FROM_ME || DISPLAY_STATUS== IMAGE_PROGRESS_SHOW_UPLOAD) bind_image_me(dataModel)
            if(DISPLAY_STATUS== TEMPLATE_ME_STORING_REACTION) bind_reaction_me(dataModel)
            if(DISPLAY_STATUS== TEMPLATE_ME_VOTE) bind_vote_me(dataModel)
            if(DISPLAY_STATUS== REPLAY_TEMPLATE_ME || DISPLAY_STATUS == IMAGE_TEXT ) bind_replay_me(dataModel)
            if(DISPLAY_STATUS== REMOVED_PERSON) bind_removed_display(dataModel)
            if(DISPLAY_STATUS== MEMBER_ADDED) bind_add_member(dataModel)
            if(DISPLAY_STATUS == VIDEO_PLAY) bind_vedio_show(dataModel)
            if(DISPLAY_STATUS== TEXT_IMAGE  || DISPLAY_STATUS == IMAGE_IMAGE) bind_replay_image_video_me(dataModel)
    //      if(DISPLAY_STATUS== STICKER_FROM_ME) bind_sticker_me(dataModel)
            if(DISPLAY_STATUS == DOCUMENT)bind_document(dataModel)           // fits the document
            if(DISPLAY_STATUS == PDF)bind_pdf_document(dataModel)            // this will fit only the pdf files
            if(DISPLAY_STATUS == TEXT_PDF || DISPLAY_STATUS== IMAGE_PDF) bind_replay_with_pdf(dataModel)
            if(DISPLAY_STATUS == TEXT_DOC || DISPLAY_STATUS == IMAGE_DOC) bind_replay_with_doc(dataModel)
            if(DISPLAY_STATUS == SHARE_CONTACT) bind_contacts(dataModel)          // binding contacts to to share
        }

        // used for the long pressed function in group_chat_activity()
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>(){
            override fun getPosition(): Int = adapterPosition
            override fun getSelectionKey():Long = itemId
        }

        //  updating the database of reaction template
        fun update_reaction_store( data_ : Boolean,
                                   group_number : String,
                                   message_number : String
                                 )
        {
            val db = universal_chat_store(context_,null)

            val thread = Thread({
                val str = "$data_"
                db.update_group_reaction_template("you_liked",group_number,message_number,str)
            })
            thread.start()
            db.close()
        }

        // for the updating te vote template data base
        private fun update_db_vote(new_value : voting_template , msg_number: String){
            val DB = universal_chat_store(context_,null)
            val new_data : String = mapper.writeValueAsString(new_value)
            val thread = Thread({
                DB.group_update_vote_template(GROUP_NUMBER,msg_number, new_data)
            })
            thread.start()
        }

    }


}