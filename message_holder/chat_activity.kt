package com.example.database_part_3.message_holder

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.database_part_3.model.universal_model.one_chat_property
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.bumptech.glide.Glide
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.SWIPE_TO_EDIT_MESSAGE.swipe_to_edit
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.forward.chips_formation
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.*
import com.example.database_part_3.groups_template_section.comment_section_group
import com.example.database_part_3.image_size_reducer.ImageResizer
import com.example.database_part_3.message_holder.*
import com.example.database_part_3.model.*
import com.example.database_part_3.multiple_image.image_select_model
import com.example.database_part_3.multiple_image.showing_multiple_photos
import com.example.database_part_3.vedio_player.play_vedio_large
import com.example.database_part_3.vedio_player.video_model
import com.example.database_part_3.video_compressor.video_compressor_
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
/*
text message : g_chat (same as group chat)
images messages : g_i (")
video messages : g_v
reaction_template : storing_reaction
voting_template : voting
sharing_contacts : "contact"


*/

class chat_activity : AppCompatActivity() ,
    OnContactClickListener ,
    ActionMode.Callback {

    private lateinit var adapter : DataAdapter
    private lateinit var context_ : Context
    private lateinit var message_list_show : RecyclerView
    private var first_click : Long = 0              // this is used for one click event
    private val DELTA_TIME_DOUBLE_CLICK = 360       //  this 1000 ms is the time between the two click of double click
    private var all_group_chats = ArrayList<one_chat_property>()
    private var tracker : SelectionTracker<Long> ? = null
    private var selectedPostItems : MutableList<Long> = mutableListOf()       // this contains the message position that is selected for activation
    private var actionMode : ActionMode? = null
    private val mapper = jacksonObjectMapper()
    private var TOTAL_MESSAGE = 0
    private lateinit var compression_1 : RelativeLayout
    private lateinit var compression_2 : RelativeLayout
    private lateinit var compression_3 : RelativeLayout
    private lateinit var send_image : Button
    private lateinit var NAME_ : String
    private var NUMBER_ : Long = 0
    private var SELECTED_REPLAY_MESSAGE_POSITION = -1        // if this is not -1 then sending is in replay mode
    private var SELECTED_REPLAY_TEXT = ""           // selected message to replay text is store here
    private lateinit var input_text : EditText
    private lateinit var reply_layout : RelativeLayout
    private lateinit var cancelButton : ImageButton
    private lateinit var txtQuotedMsg : TextView
    private lateinit var template_select : ImageButton
    private lateinit var edit_text_view :  TextView
    private lateinit var edit_text_view_layout : RelativeLayout
    private lateinit var replying_layout_id : RelativeLayout
    private lateinit var cancel_edit_text : ImageButton
    private lateinit var selected_image_show : ImageView
    private lateinit var button_1 : Button
    private lateinit var button_2 : Button
    val kk = ArrayList<Uri>()                 // for the video uri store , 0->original_video_uri , 1->360P uri compressed video , 2-> 240P uri of compressed video
    private lateinit var sheet_ : BottomSheetDialog
    private lateinit var layout_1 : RelativeLayout
    private lateinit var layout_2 : RelativeLayout    // for video compression layout
    var PROGRESS_POSITION_CONTAINER = 0                 // contains the position at which the image_progress is sended initially to the adapter
    private var EDIT_MESSAGE : Boolean = false
    private var POSITION_CONTAINER = 0                  // if the edit text is selected then this contains the position of edit
    var PICK_IMAGE : String = ""
    var BITMAP_CONTAINER : Bitmap ? = null
    private val IMAGE_CHOOSE = 1000
    private val PERMISSION_CODE = 1001
    private val SELECT_VIDEO = 2000
    private val SELECT_FILES = 1500
    private val REQUEST_CODE_ACCEPTING_DATA = 123
    private val REQUEST_CODE_VIDEO = 1234
    private val REQUEST_CODE_TEMPLATE_COMMENT = 2345
    private val CHOOSING_CONTACTS = 3030   // request code for choosing contacts
    private val CAMARA_ACCESS = 3900
    var click_photo_file : File ? = null
    var DURATION_OF_VIDEO_SELECTED = ""     // durations of video selected to send
    private lateinit var original_video_layout : RelativeLayout
    var PIXEL_SELECT_STATUS = ""
    var VIDEO_COMPRESS_ELIGIBILITY = false     // if size is less than 10MB then cannot compress
    var compressed_path_240 = ""               // contains the path of 240p compressed video
    var compressed_path_360 = ""               // contains the path of 360p compressed video
    var original_path = ""                    // contains original path of video uncompressed
    private var PRIVATE_CHAT_ = false
    val msg_number_position = HashMap<String,Int>()      // contains [message_number] = positions
    private lateinit var progress_bar : ProgressDialog
    private var PAIR_ = ""
    private lateinit var voice_record_ : ImageButton     // for voice record


    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_show_layout)

        val time_1 = System.currentTimeMillis()       // initial time before compilation of group_chat activity
        // now to setting for the initializations of toolbars
        val toolbar_ : Toolbar = findViewById(R.id.tool_bar_chat_screen)
        setSupportActionBar(toolbar_)
        toolbar_.showOverflowMenu()
        context_ = this

        // regestering the broadcast
        LocalBroadcastManager.getInstance(context_).registerReceiver( broad_cast_receiver_,IntentFilter("COMPRESSION_PROGRESS"))   // this is regestering the broadcast receive

        val send_button = findViewById<ImageButton>(R.id.btnsend)
        message_list_show = findViewById(R.id.messageList_recycle)
        reply_layout = findViewById(R.id.reply_layout)
        cancelButton = findViewById(R.id.cancelButton)
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg)
        template_select = findViewById(R.id.select_tamplate_id)
        edit_text_view = findViewById(R.id.live_text_view_show)
        edit_text_view_layout = findViewById(R.id.edit_message_layout_id)
        cancel_edit_text = findViewById(R.id.cancel_edit_text)
        replying_layout_id = findViewById(R.id.replying_layout_id)
        selected_image_show= findViewById(R.id.selected_image_show_)
        voice_record_ = findViewById(R.id.record_voice_id)


        NAME_ = intent.getStringExtra("+name").toString()
        NUMBER_ = intent.getStringExtra("+number")!!.toLong()          // if pair_chat== false then this variable contains group number
        PRIVATE_CHAT_ = intent.getStringExtra("private_chat").toBoolean()

        // calculating pair
        if(MY_NUMBER_LONG > NUMBER_) PAIR_ = "$MY_NUMBER_LONG|$NUMBER_"
        if(NUMBER_> MY_NUMBER_LONG) PAIR_="$NUMBER_|$MY_NUMBER_LONG"

        val finger_print = intent.getStringExtra("+finger_print").toBoolean()    // for the fingerprint lock setting before openning chat in group
        if(finger_print==true){
            val finger_print_icon = findViewById<ImageView>(R.id.finger_print_)
            finger_print_icon.visibility = View.VISIBLE
        }
        // initializiung the firebase app
        FirebaseApp.initializeApp(this)

        // settings for private chats
        val lock_show = findViewById<ImageView>(R.id.lock_pair_chat_id)

        if(PRIVATE_CHAT_== true){        // for restricting the screenshoot the screen
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            lock_show.setImageResource(R.drawable.lock_message)
        }
        if(PRIVATE_CHAT_==false){
            lock_show.setImageResource(R.drawable.unlock_white)
        }

        val time_2 = System.currentTimeMillis()       // final time after the compilation

        // make thread and bring data and send to the adapter
        val handler = Handler()
        val DB = universal_chat_store(this,null)

        // this thread contains all the setOnClick listener to give the better performance
        val thread = Thread({
            all_group_chats = DB.get_messages(PAIR_,"")
            val ss = all_group_chats.size
            var count=0

            for(i in all_group_chats){
                msg_number_position[i.msg_num]=count
                count++
            }
            if(ss>0){
                var ff = all_group_chats[ss-1].msg_num
                TOTAL_MESSAGE = ff.toInt()
            }       // copy the last message number of the group message

            handler.post{
                message_list_show.layoutManager = LinearLayoutManager(this)
                message_list_show.setHasFixedSize(true)
                adapter = DataAdapter(this,this,msg_number_position,PAIR_,PRIVATE_CHAT_)
                message_list_show.adapter = adapter


                // this is for long pressed detections in the group chat
                tracker = SelectionTracker.Builder<Long>(
                    "mySelection",
                    message_list_show,
                    StableIdKeyProvider(message_list_show),
                    Items_detail_look_up(message_list_show),
                    StorageStrategy.createLongStorage())
                    .withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

                adapter.tracker = tracker

                //  making the tracker for selections
                tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>(){
                    override fun onSelectionChanged(){
                        super.onSelectionChanged()

                        tracker.let {
                            selectedPostItems = it!!.selection.toMutableList()
                            Log.d("","@@@@@@@@selectedPosition:${selectedPostItems}")

                            if(selectedPostItems.isEmpty()){    // if nothing is selected
                                actionMode?.finish()
                            }
                            else{                             // if some items of messages are selected
                                if(actionMode==null) actionMode = startSupportActionMode(this@chat_activity)
                                actionMode?.title = "${selectedPostItems.size}"        // number of selected items to show in toolbar
                            }
                        }
                    }
                })


                // Replay layout right swipe of message
                // for message replay layout
                val message_swipe_controller = MessageSwipeController(this,object : SwipeControllerActions{

                    override fun showReplyUI(position : Int){             // this will give the recycle View item positions number
                        edit_text_view_layout.visibility = View.GONE
                        SELECTED_REPLAY_MESSAGE_POSITION = position
                        if(all_group_chats[position].category=="g_chat"){

                            if(all_group_chats[position].replied_msg=="yes"){
                                val text_str = all_group_chats[position].data
                                val store_ = mapper.readValue<replay_data_model>(text_str)
                                txtQuotedMsg.text = store_.reply_message
                                SELECTED_REPLAY_TEXT = store_.reply_message
                                showQuotedMessage()
                            }

                            if(all_group_chats[position].replied_msg=="none"){
                                txtQuotedMsg.text = all_group_chats[position].data
                                SELECTED_REPLAY_TEXT = all_group_chats[position].data
                                showQuotedMessage()
                            }
                        }

                        // if selecting chat is image
                        if(all_group_chats[position].category=="g_i"){
                            /* replay text */
                            /* replay sticker or vedios or images */
                            val selected_image_uri = mapper.readValue<image_data_model>(all_group_chats[position].data).local_url   // this will paste the uri of selected image
                            txtQuotedMsg.visibility = View.GONE
                            selected_image_show.visibility = View.VISIBLE
                            selected_image_show.setImageURI(selected_image_uri.toUri())    // set the image uri
                            showQuotedMessage()
                        }

                        if(all_group_chats[position].category=="voting"){
                            val vote_text = mapper.readValue<voting_template>(all_group_chats[position].data).topic    // this will selected
                            SELECTED_REPLAY_TEXT = vote_text
                            txtQuotedMsg.visibility = View.VISIBLE
                            selected_image_show.visibility = View.GONE
                            txtQuotedMsg.setText(vote_text)
                            showQuotedMessage()
                        }
                        if(all_group_chats[position].category=="sticker"){
                            /* normal image view */
                        }

                        if(all_group_chats[position].category=="storing_reaction"){   /* intent to comment sections */

                            val topic_of_reaction_template = mapper.readValue<reaction_store_group_model>(all_group_chats[position].data).topic
                            val intent = Intent(context_, comment_section_group::class.java)
                            intent.putExtra("+sender_name",all_group_chats[position].from)
                            intent.putExtra("+msg_number",all_group_chats[position].msg_num)
                            intent.putExtra("+group_number",group_number)
                            intent.putExtra("+group_name",  PAIR_)
                            intent.putExtra("+private_chat",PRIVATE_CHAT_)
                            intent.putExtra("+topic",topic_of_reaction_template)
                            Toast.makeText(context_,"Sender name is: ${all_group_chats[position].from}",Toast.LENGTH_LONG).show()    // this will show the sender name
                            startActivity(intent)
                        }

                        // now for document
                        if(all_group_chats[position].category=="g_doc" || all_group_chats[position].category=="PDF"){    // if selected message is file or something
                            // if some one select document then show only the name part of document
                            val selected_message : String = mapper.readValue<documents_model>(all_group_chats[position].data).file_name
                            SELECTED_REPLAY_TEXT = selected_message
                            txtQuotedMsg.visibility = View.VISIBLE
                            selected_image_show.visibility = View.GONE
                            txtQuotedMsg.setText(selected_message)       // selected message is document
                            showQuotedMessage()
                        }
                    }
                })
                val itemTouchHelper = ItemTouchHelper(message_swipe_controller)
                itemTouchHelper.attachToRecyclerView(message_list_show)


                // for the swipping functions for swiping message left side for reedit the message
                // only text messages are editable not other template types of messages or messages
                val edit_message = swipe_to_edit(this,object : SwipeControllerActions {
                    override fun showReplyUI(position: Int){
                        EDIT_MESSAGE = true
                        POSITION_CONTAINER = position
                        replying_layout_id.visibility = View.GONE
                        if(all_group_chats[position].category=="g_chat"){

                            if(all_group_chats[position].replied_msg=="yes"){
                                val text_str = all_group_chats[position].data
                                val store_ = mapper.readValue<replay_data_model>(text_str)
                                edit_text_view.text = store_.reply_message       // showing the message before editing it
                                edit_message_show()                              // calling the functions for edit live edit text
                                input_text.setText(store_.reply_message)         // displaying the message in the edit text so that user will know what text to edit from before
                            }

                            if(all_group_chats[position].replied_msg=="none"){
                                val text_ = all_group_chats[position].data        // just showing the text that the user is going to edit
                                edit_text_view.text = text_
                                edit_message_show()      // calling the functions for edit live edit text
                                input_text.setText(text_)     // displaying the message in the edit text so that user will know what text to edit from before
                            }
                        }
                        Toast.makeText(context_,"You are editing your message to resend it",Toast.LENGTH_LONG).show()
                    }
                })
                val editing_messages = ItemTouchHelper(edit_message)
                editing_messages.attachToRecyclerView(message_list_show)

                initiate_message_me(all_group_chats)

                val time_3 = System.currentTimeMillis()               // initial time of setclick listenner

                // for all chat store of group messages
                input_text = findViewById<EditText>(R.id.txtMessage)

                // for showing the all information of group
                toolbar_.setOnClickListener {
                    val intent = Intent(this , groups_info::class.java)
                    intent.putExtra("+group_name", NAME_)
                    intent.putExtra("+group_number", NUMBER_)
                    intent.putExtra("+last_msg_number","${TOTAL_MESSAGE}")
                    startActivity(intent)
                    Animatoo.animateSlideDown(this)
                }

                send_button.setOnClickListener{
                    var replay_status = "none"
                    val time_ = "${System.currentTimeMillis()}"
                    var msg_data = input_text.text.toString()


                    if (SELECTED_REPLAY_MESSAGE_POSITION != -1){
                        val selected_msg_number = all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].msg_num

                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_chat"){   // text_text
                            val replay_ = replay_data_model(SELECTED_REPLAY_TEXT, "$selected_msg_number", msg_data)
                            msg_data = mapper.writeValueAsString(replay_)
                            replay_status = "yes"
                        }

                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_i"){      // selected message is IMAGE and replied TEXT
                            val image_uri =mapper.readValue<image_data_model>(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data)
                            val selected_model = replay_data_model(image_uri.local_url,"$selected_msg_number",input_text.text.toString())
                            msg_data = mapper.writeValueAsString(selected_model)
                            replay_status = "IMAGE_TEXT"
                            Log.d("","ppppppppppppreparing for image selected text replay")
                        }

                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="voting"){   // already topic text is selected in tthis variable so this is ntrmal text_text
                            val replay_ = replay_data_model(SELECTED_REPLAY_TEXT, "$selected_msg_number", msg_data)
                            msg_data = mapper.writeValueAsString(replay_)
                            replay_status = "yes"
                            Log.d("","ppppppppppppppppppppppreparing for voting replay")
                        }

                        // this initiate if selected messag to replay is document
                        // this is text_to_text replay
                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_doc" || all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="PDF"){
                            val replay_ = replay_data_model(SELECTED_REPLAY_TEXT, "$selected_msg_number", msg_data)
                            msg_data = mapper.writeValueAsString(replay_)
                            replay_status = "yes"
                            Log.d("","ppppppppppppppppppreparing for voting replay")
                        }
                    }

                    if(EDIT_MESSAGE == false){
                        TOTAL_MESSAGE++
                        val send_data = one_chat_property(PAIR_,NAME_,"$TOTAL_MESSAGE",msg_data,"g_chat",false,"none",get_send_time(),false,false,
                                                          "none","none",false, MY_NUMBER_LONG,NUMBER_,replay_status,"none")
                        all_group_chats.add(send_data)
                        initiate_message_me(all_group_chats)
                        input_text.setText("")
                        val DB = universal_chat_store(this,null)
                        val thread = Thread({                // save message to table database
                            DB.save_message(PAIR_,"$TOTAL_MESSAGE",msg_data,"g_chat",false,"none",get_send_time(),"none",false,
                                MY_NUMBER_LONG,"$NUMBER_",replay_status,"none",false,false,"none",NAME_)   // saving to database
                        })
                        thread.start()
                        hideReplayLayout()
                    }

                    // when we edit anu text message
                    if (EDIT_MESSAGE == true){
                        val msg_number_ = all_group_chats[POSITION_CONTAINER].msg_num
                        val send_data = one_chat_property(PAIR_,NAME_,"$msg_number_",msg_data,"g_chat",false,"none",get_send_time(),
                                                    true,false,"none","none",false, MY_NUMBER_LONG,NUMBER_,replay_status,"none")

                        all_group_chats[POSITION_CONTAINER] = send_data

                        adapter.update_list(
                            "EDIT_REWRITE",
                            mapper.writeValueAsString(send_data),
                            POSITION_CONTAINER
                        )    // sending to the adapter

                        val DB = universal_chat_store(this, null)
                        val thread = Thread({
                            val msg_numbers = ArrayList<String>()
                            msg_numbers.add(msg_number_)
                            DB.update_one_chat_property("EDIT_REWRITE",PAIR_,msg_numbers,msg_data)     // this will update only the messaging part data min database
                        })
                        thread.start()
                        cancel_edit_text()
                        EDIT_MESSAGE = false
                    }
                }

                val attachment: ImageButton = findViewById(R.id.options_file_choose_id_)
                attachment.setOnClickListener {
                    show_bottom_dialog()
                }

                // cancelling to replay of message
                cancelButton.setOnClickListener {
                    hideReplayLayout()
                }

                // template selections voter and reactions store
                template_select.setOnClickListener {
                    selection_template()
                }

                //  cancelling the edit_text layout
                cancel_edit_text.setOnClickListener {
                    cancel_edit_text()
                }


                // for timer chat clickable
                // for timer chat
                val timer_msg : ImageButton = findViewById(R.id.timer_chat_icon_id)
                timer_msg.setOnClickListener{
                    if(input_text.text.isNotEmpty()){
                        val sheet: BottomSheetDialog = BottomSheetDialog(this)
                        sheet.setContentView(R.layout.timer_chat_layout)
                        val time : EditText = sheet.findViewById(R.id.timer_chat_set_time)!!

                        /* save this time to the timer sheet */

                        sheet.show()
                    }
                    else{
                        Toast.makeText(this,"Please enter the message",Toast.LENGTH_SHORT).show()
                    }
                }

                // for the listenning in text change in edit text to make cchange in template selections and docuemnt attachment logo visibility
                input_text.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?){}
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(current_text : CharSequence?, p1: Int, p2: Int, p3: Int) {

                        if(current_text!!.length!=0){
                            template_select.visibility = View.GONE
                            attachment.visibility = View.GONE
                            voice_record_.visibility = View.GONE
                        }
                        if(current_text.length==0){
                            template_select.visibility = View.VISIBLE
                            attachment.visibility = View.VISIBLE
                            voice_record_.visibility = View.VISIBLE
                        }
                    }
                })

                val time_4 = System.currentTimeMillis()         // final time of setoNCLICK listenner
                Log.d("","dddddddddelta time taken by click listenner: ${time_4 - time_3}")
            }
        })
        thread.start()
    }

    override fun onBackPressed() {
        if(selectedPostItems.size!=0)selectedPostItems.clear()          // this will disellect all the selected items
        super.onBackPressed()
        Animatoo.animateShrink(this)
    }

    // cancelling the editext
    private fun cancel_edit_text(){
        hideReplayLayout()
        input_text.setText("")
        EDIT_MESSAGE = false
        POSITION_CONTAINER = 0
    }

    // for replay layout
    private fun showQuotedMessage(){
        input_text.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(input_text , InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
        replying_layout_id.visibility = View.VISIBLE
    }

    // cancelling the replay layout
    private fun hideReplayLayout(){                      // this functions hide the replay layout for if not required
        reply_layout.visibility = View.GONE
        SELECTED_REPLAY_MESSAGE_POSITION = -1
        SELECTED_REPLAY_TEXT = ""
    }


    fun initiate_message_me(_data : ArrayList<one_chat_property>){
        adapter.setData(_data)                                // this positions is new inserted item in the arraList
        message_list_show.scrollToPosition(adapter.itemCount-1)    // for viewing the last message that is sended
    }

    //  setOnClickListener of message
    // this is responsible for double click listener of message for message info
    override fun onContactClickListener(position: Int){

        var _used = 0
        if(first_click!=0L){
            val now_ : Long = System.currentTimeMillis()
            if(_used==0) {
                if (now_ - first_click <= DELTA_TIME_DOUBLE_CLICK){
                    val sheet : BottomSheetDialog = BottomSheetDialog(this)
                    sheet.setContentView(R.layout.message_details)
                    val show_detail_text = sheet.findViewById<TextView>(R.id.show_detail_text)
                    val seen_time_id = sheet.findViewById<TextView>(R.id.seen_time_id)
                    show_detail_text!!.setText("${all_group_chats[position].data}")
                    seen_time_id!!.setText("${all_group_chats[position].read}")         /* this should be the seen time of this message of opposite one */
                    sheet.show()
                    first_click = 0
                    _used++
                }
            }

            if(_used==0){
                if(now_ - first_click >= DELTA_TIME_DOUBLE_CLICK){
                    first_click=0
                    Toast.makeText(this,"Outside double_click function",Toast.LENGTH_SHORT).show()
                }
            }
            _used++
        }
        if(_used==0){
            if(first_click==0L){
                first_click = System.currentTimeMillis()
            }
        }

        Toast.makeText(this,"You clicked the message position: ${position}",Toast.LENGTH_LONG).show()
    }

    //  #################################  presshold section ####################
    // allowing the menu items which one is to visible when
    override fun onCreateActionMode(mode : ActionMode?, menu : Menu?) : Boolean {
        mode?.let{
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.presshold_menu,menu)

            if(PRIVATE_CHAT_ ==true){                        // this controlls what menu should I show during presshold
                val copy_: MenuItem = menu!!.findItem(R.id.copy_msg_id)
                val forward: MenuItem = menu.findItem(R.id.forward_msg_id)
                copy_.setVisible(false)
                forward.setVisible(false)
            }
            return true
        }
        return false
    }

    override fun onActionItemClicked(mode : ActionMode?, item : MenuItem?) : Boolean {  // this functions controlled the clicked long pressed functions

        when(item?.itemId) {

            R.id.star_function_id -> {
                var stared_msg_number = 0
                val DB = universal_chat_store(this, null)

                for(i in selectedPostItems) if(all_group_chats[i.toInt()].stared == true) stared_msg_number++

                if (selectedPostItems.size == stared_msg_number){        // when you want to unstar all selected message
                    val list_items = "Unstar all ${selectedPostItems.size} messages for me"
                    val mBuilder = AlertDialog.Builder(this)
                    mBuilder.setTitle(list_items)
                    val msg_number: ArrayList<String> = ArrayList<String>()

                    for (i in selectedPostItems) {                        // Always be carefull when you selected the message number take directly from the list individual
                        val msg_num = all_group_chats[i.toInt()].msg_num
                        msg_number.add(msg_num)
                    }

                    mBuilder.setPositiveButton("ok"){ dialogInterface,i ->

                        for(k in selectedPostItems)adapter.update_list("STAR","false",k.toInt())  // updating the adapter for the template

                        Toast.makeText(this,"You unstar all ${selectedPostItems.size} messages",Toast.LENGTH_LONG).show()
                        val thread = Thread({
                            for(j in msg_number){
                                DB.update_group_message("STAR",group_number,j,"false")
                            }
                        })
                        thread.start()
                        dialogInterface.dismiss()
                    }
                    mBuilder.setNeutralButton("Cancel") { dialog, which ->
                        dialog.cancel()
                    }
                    val dd = mBuilder.create()
                    dd.show()
                }

                if(selectedPostItems.size != stared_msg_number){   // want to star all the selected messages
                    val list_items = "Star all ${selectedPostItems.size} messages for me"
                    val mBuilder = AlertDialog.Builder(this)
                    mBuilder.setTitle(list_items)
                    val msg_number : ArrayList<String> = ArrayList<String>()
                    for (i in selectedPostItems) {                        // Always be carefull when you selected the message number take directly from the list individual
                        val msg_num = all_group_chats[i.toInt()].msg_num
                        msg_number.add(msg_num)
                    }
                    mBuilder.setPositiveButton("ok"){dialogInterface, i ->
                        val thread  = Thread({          // updating the database
                            for(j in msg_number) {
                                DB.update_group_message("STAR",group_number,j,"true")
                            }
                        })
                        thread.start()
                        Toast.makeText(this,"Stared messages moved to star folder",Toast.LENGTH_LONG).show()
                        for(j in selectedPostItems){
                            Log.d("","nnnnnnnnnow the selected messages numbers are : ${j}")
                            all_group_chats[j.toInt()].stared = true   // updating the chat store array of Activity
                        }

//                      initiate_message_me(all_group_chats)
                        for(k in selectedPostItems){
                            adapter.update_list("STAR","true",k.toInt())
                        }
                        selectedPostItems.clear()
                        dialogInterface.dismiss()
                        adapter.tracker?.clearSelection()
                        actionMode = null
                    }
                    mBuilder.setNeutralButton("Cancel"){ dialog, which ->
                        dialog.cancel()
                        dialog.dismiss()
                    }
                    val dd = mBuilder.create()
                    dd.show()
                }
            }

        }
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) : Boolean {
        return true
    }

    override fun onDestroyActionMode(mode : ActionMode?){
        adapter.tracker?.clearSelection()
        actionMode = null
    }
    //  ######################################################


    //  ############################ Image section ###################
    // now for detecting the url of choosing the image after selections from gallery
    fun color_selection(layout_number : Int){

        if(PICK_IMAGE!="")send_image.setBackgroundResource(R.drawable.dark_color_rectangle)

        if(layout_number==1){
            compression_1.setBackgroundResource(R.color.gray)
            compression_2.setBackgroundResource(R.color.white)
            compression_3.setBackgroundResource(R.color.white)
        }
        if(layout_number==2){
            compression_1.setBackgroundResource(R.color.white)
            compression_2.setBackgroundResource(R.color.gray)
            compression_3.setBackgroundResource(R.color.white)
        }
        if(layout_number==3){
            compression_1.setBackgroundResource(R.color.white)
            compression_2.setBackgroundResource(R.color.white)
            compression_3.setBackgroundResource(R.color.gray)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data : Intent?){

        super.onActivityResult(requestCode, resultCode, data)

        // for catching the image url
        if(resultCode == Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
            //  val send_data = ArrayList<group_message_model>()
            Log.d("","ddddddddExtracted path is:${data!!.data}")

            if(data.clipData==null){     // if no photo has been selected from gallery
                Toast.makeText(this,"You didnot selected any media",Toast.LENGTH_SHORT).show()
            }

            if(data.data!=null){         // this is for selecting single photo from gallery
                val file_path: Uri = data.data!!
                val full_size_bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, file_path))
                } else {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, file_path)
                }

                // MAX_SIZE 240000
                // MAX_SIZE 300000
                // MAX_SIZE 360000
                val reduced_bitmap_2 : Bitmap = ImageResizer.reduceBitmapSize(full_size_bitmap,360000)
                val reduced_bitmap_3 : Bitmap = ImageResizer.reduceBitmapSize(full_size_bitmap,300000)

                val uri_2: String = saveImage(reduced_bitmap_2)
                val uri_3: String = saveImage(reduced_bitmap_3)

                // showing the option of pixel selections

                val sheet_ = BottomSheetDialog(this)
                sheet_.setContentView(R.layout.pixel_selection)

                compression_1 = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_1)!!
                compression_2 = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_2)!!
                compression_3 = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_3)!!

                val compress_size_1: TextView = sheet_.findViewById(R.id.compressed_size_id_1)!!
                val compress_size_2: TextView = sheet_.findViewById(R.id.compressed_size_id_2)!!
                val compress_size_3: TextView = sheet_.findViewById(R.id.compressed_size_id_3)!!

                send_image = sheet_.findViewById<Button>(R.id.send_image_id)!!

                val image_1: ImageView = sheet_.findViewById(R.id.compressed_image_1)!!
                val image_2: ImageView = sheet_.findViewById(R.id.compressed_image_2)!!
                val image_3: ImageView = sheet_.findViewById(R.id.compressed_image_3)!!

                val size1 = sizeOf(file_path.toString())                      // this is original size of the image
                compress_size_1.text = "Original : $size1 kb"
                image_1.setImageBitmap(full_size_bitmap)

                val size2 = sizeOf(uri_2)                     // 2nd compressor size of image
                compress_size_2.text = "Compressed : $size2 kb"
                image_2.setImageBitmap(reduced_bitmap_2)

                val size3 = sizeOf(uri_3)                     // 3rd  compressed size of Image
                compress_size_3.text = "Compressed : ${size3}kb"
                image_3.setImageBitmap(reduced_bitmap_3)


                // this are for color selections after selecting the pixels of Image
                compression_1.setOnClickListener {
                    PICK_IMAGE = file_path.toString()
                    BITMAP_CONTAINER = full_size_bitmap
                    color_selection(1)
                }
                compression_2.setOnClickListener {
                    PICK_IMAGE = uri_2
                    BITMAP_CONTAINER = reduced_bitmap_2
                    color_selection(2)
                }
                compression_3.setOnClickListener {
                    PICK_IMAGE = uri_3
                    BITMAP_CONTAINER = reduced_bitmap_3
                    color_selection(3)
                }

                // for viewing the full image pixels
                // when clicked in image to fullView
                image_1.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", file_path.toString())
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT_.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: original")
                }
                image_2.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_2)
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT_.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: compressed_2")
                }
                image_3.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_3)
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT_.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: compressed_3")
                }

                sheet_.show()

                // ready to send and save Image
                send_image.setOnClickListener {
                    if(PICK_IMAGE==""){
                        Toast.makeText(this,"Please select a pixel of your image",Toast.LENGTH_SHORT).show()
                    }
                    if(PICK_IMAGE!=""){
                        val time_ = "${System.currentTimeMillis()}"
                        TOTAL_MESSAGE++
                        var message_number = "${TOTAL_MESSAGE}"             // updating the total selected image
                        val data_ : image_data_model = image_data_model("","",PICK_IMAGE,"","")
                        var data_str = mapper.writeValueAsString(data_)
                        var read_store = HashMap<Int,String>()
                        var delivered_ = HashMap<Int,String>()
                        val read_ : String = mapper.writeValueAsString(read_store)
                        val delivered_str = mapper.writeValueAsString(delivered_)
                        sheet_.dismiss()
                        var replay_status = "g_i"
                        if(SELECTED_REPLAY_MESSAGE_POSITION!=-1){     // this means you have selected some thing to replay image

                            if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_chat"){    // this means selected message is text and replied image
                                val text_ = all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data       // selected text to replay
                                val reply_model = image_data_model("","","$PICK_IMAGE","","$text_")
                                data_str = mapper.writeValueAsString(reply_model)
                                replay_status = "TEXT_IMAGE"     // this means selected msg is text and replied message is image
                            }

                            if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_i"){
                                val image_model = mapper.readValue<image_data_model>(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data)   // parsing the data
                                val selected_uri : String = image_model.local_url
                                val replay_model = image_data_model("","",selected_uri,"",PICK_IMAGE)   // this last part contains replied image url
                                data_str = mapper.writeValueAsString(replay_model)    // Stringifying the replay  messmodelage
                                replay_status = "IMAGE_IMAGE"
                            }
                        }

//                        val send_data = group_message_model(group_name,group_number,message_number,data_str,"g_i",read_,delivered_str,"none",
//                            time_,false,false,"none","none", MY_NUMBER,"$replay_status","none")

                        val send_data = one_chat_property(PAIR_,NAME_,message_number,data_str,"g_i",false,"none",get_send_time(),false,false,
                                                        "none","none",false, MY_NUMBER_LONG,NUMBER_,"$replay_status","none")
                        all_group_chats.add(send_data)
                        msg_number_position["$TOTAL_MESSAGE"] = all_group_chats.size-1   // this will go to the adapter
                        initiate_message_me(all_group_chats)

                        val DB = universal_chat_store(this, null)
                        val handler = Handler()
                        val thread : Thread = Thread({
                            DB.save_message(PAIR_,message_number,data_str,"g_i",false,"none",get_send_time(),"none",false,
                                           MY_NUMBER_LONG,"$NUMBER_","$replay_status","none",false,false,"none",NAME_)

                            handler.post{
                                // showing the proccessing part in %
                                PROGRESS_POSITION_CONTAINER = adapter.itemCount - 1     // this contains the position of sended progress of uploading image to server

                                adapter.BITMAP_ = BITMAP_CONTAINER
                                adapter.update_list("UPLOAD_PROGRESS","0",PROGRESS_POSITION_CONTAINER)
                            }
                        })
                        thread.start()
                        PICK_IMAGE = ""
                    }
                }
            }

            // ############### for selecting multiple images at one time
            if(data.clipData!=null){
                val total_image = data.clipData!!.itemCount
                Toast.makeText(this,"Total Image you have selected is ${total_image}",Toast.LENGTH_LONG).show()
                Log.d("iiiiiiiiimage","data.clipData:${data.clipData} & total selected image is: ${total_image}")

                val read_ = HashMap<Int,String>()       // which persons have read this messages <Mobile_number,Time_of_read>
                val read_string = mapper.writeValueAsString(read_)     // ready to store in the database
                /* After clicking the send button  */

                val all_uri = ArrayList<image_select_model>()      // to store uri to send to the image show Activity

                for(i in 0..total_image-1){
                    val get_uri : ClipData.Item = data.clipData!!.getItemAt(i)
                    Log.d("LLLLLLLlllllink_uri","${get_uri.uri}")
                    all_uri.add(image_select_model("${get_uri.uri}",false))
                }

                val all_uri_str = mapper.writeValueAsString(all_uri)        // ready to send to multiple image showing layout
                val intent = Intent(this,showing_multiple_photos::class.java)
                intent.putExtra("all_uri",all_uri_str)
                startActivityForResult(intent,REQUEST_CODE_ACCEPTING_DATA)
            }
        }


        // for catching the vedio link after selecting  the vedio player
        if(resultCode == Activity.RESULT_OK && requestCode == SELECT_VIDEO){

            if(data!!.data==null && data.clipData==null){
                Toast.makeText(this,"you didnot selected any video from gallary",Toast.LENGTH_LONG).show()
            }

            // selection of single video url link
            if(data.data!=null && data.clipData==null){
                val mp = MediaPlayer.create(this, Uri.parse(data.data.toString()))
                val duration : Long = mp.duration.toLong()
                mp.release()

                /*convert millis to appropriate time*/

                val time : String = java.lang.String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                )
                DURATION_OF_VIDEO_SELECTED = time
                kk.add(data.data!!)             // this is teh first element in the uri store
                original_path = "${data.data}"   // copying the path of original video
                video_compressor_before_send("ORIGINAL","","${data.data}","",getBytes(data.data!!))
            }

            // for multiple vedio link detection
            if(data.clipData!=null){
                val total_videos = data.clipData!!.itemCount
                Toast.makeText(this,"Total video you have selected is ${total_videos}",Toast.LENGTH_LONG).show()

                val all_uri = ArrayList<String>()
                for(i in 0..total_videos-1){
                    val uri_at = data.clipData!!.getItemAt(i)      // this is present uri from clip uri data
                    all_uri.add("$uri_at")
                }

                val uri_string_bundle = mapper.writeValueAsString(all_uri)

//                val intent = Intent(this,showing_multiple_photos::class.java)        // this will go to the final all vedio selection template
//                intent.putExtra("all_uri",uri_string_bundle)
//                startActivityForResult(intent,REQUEST_CODE_VIDEO)
            }
        }


        // for catching the return data back from previous activity
        // For accepting multiple images from chooose Acrtivity
        if(resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_ACCEPTING_DATA){
            val array_list_str = data!!.getStringExtra("FINAL_URI")       // get the string data from the previous Activity
            val all_uri = mapper.readValue<ArrayList<image_select_model>>(array_list_str!!)

            var LAST_POSITION_MESSAGE = all_group_chats.size-1     // Last position of ArrayList of message
            var FIRST_POSITION_OF_IMAGE = LAST_POSITION_MESSAGE
            FIRST_POSITION_OF_IMAGE++

            val handler = Handler()
            val DB = universal_chat_store(context_,null)
            var total_array_size = all_group_chats.size-1
            Thread({
                for(i in all_uri){
                    TOTAL_MESSAGE++    // this is message number
                    total_array_size++
                    // send data to adapter
                    val read_ = HashMap<String,String>()
                    val read_str = mapper.writeValueAsString(read_)
                    val data_ = image_data_model("","",i.uri_str,"","")
                    val data_str = mapper.writeValueAsString(data_)
//                    all_group_chats.add(
//                        group_message_model(group_name,group_number,"$TOTAL_MESSAGE",data_str,"g_i",
//                        read_str,read_str,"none",get_send_time(),false,false,
//                        "none","none", MY_NUMBER_LONG,"none","none")
//                    )
                    all_group_chats.add(one_chat_property(PAIR_,NAME_,"$TOTAL_MESSAGE",data_str,"g_i",false,"none",get_send_time(),false,false,
                                                            "none","none",false, MY_NUMBER_LONG,NUMBER_,"none","none"))

                    DB.save_message(PAIR_,"$TOTAL_MESSAGE",data_str,"g_i",false,"none",get_send_time(),"none",false,
                                    MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"none",NAME_)
                    msg_number_position["$TOTAL_MESSAGE"] = total_array_size

                }

                handler.post{
                    // sending to the adapter
                    initiate_message_me(all_group_chats)                                    // after recieving the ata from previous activity this will compile
                }
            }).start()
        }


        // for sharing the multiple videos to person
        if(resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_VIDEO){
            val final_uri = intent.getStringExtra("").toString()     // this will give the final string of videos

        }


        // for receiving total comments of template section from different Activity
        if(resultCode == Activity.RESULT_OK && requestCode==REQUEST_CODE_TEMPLATE_COMMENT){

            val total_comment = data!!.getStringExtra("TOTAL_COMMENT")     // this will return total number of comments updated
            val message_number = data.getStringExtra("MESSAGE_NUMBER")     // the message number of
            Toast.makeText(this,"We are inside updating comments number of:total_comment${total_comment}!!!",Toast.LENGTH_LONG).show()
        }


        // for the selection of files or documents for sending
        if(resultCode==Activity.RESULT_OK && requestCode==SELECT_FILES){
            Toast.makeText(context_,"we are inside the file selections sections:",Toast.LENGTH_LONG).show()

            // for selection of single file document
            if(data!!.data!==null){
                val read_ = HashMap<String,String>()
                val str_read_deliver = mapper.writeValueAsString(read_)
                TOTAL_MESSAGE++
                val selected_uri = data!!.data
                val file_name = get_file_name(selected_uri!!)
                val file_type = get_type(file_name)    // this functions will bring the file type
                var pdf_type = ""
                var front_image_link = ""     // if the document is pdf then save the first page image in data model
                var total_pages = ""          // if pdf then save the total pages of pdf in data model

                //  saving file into the database
                val DB = universal_chat_store(this,null)
                val handler = Handler()

                Thread({

                    // if it is pdf then save the all properties in this datamodel
                    if(file_type=="pdf"){
                        // this is for only pdf files
                        pdf_type = "PDF"
                        val fd : ParcelFileDescriptor = context_.contentResolver.openFileDescriptor(selected_uri, "r")!!
                        total_pages = "pages:${PdfRenderer(fd).pageCount}"    // total images of pdf

                        val page_ : PdfRenderer.Page = PdfRenderer(fd).openPage(0)
                        val bitmap_ : Bitmap = Bitmap.createBitmap(page_.width,page_.height,Bitmap.Config.ARGB_8888)     // get the image of 1st page of pdf as thumbnail
                        page_.render(bitmap_,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        front_image_link = save_thumbnail_from_bitmap(bitmap_,"${file_name}_thumnail!")    // this will save the bitmap of front page image to gallery and return link
                        page_.close()
                    }

                    if(file_type!="pdf")pdf_type = "g_doc"           // this is for general document
                    val total_size_document = getBytes(selected_uri) // this return the size of pdf
                    val message_data = documents_model(file_name!!,
                        "$selected_uri","",
                        "$file_type","",getBytes(selected_uri),total_pages,front_image_link)
                    var string_data = mapper.writeValueAsString(message_data)
                    var replay_status = pdf_type    // contains the replay status

                    // check here the replay status , if any of the mesage is selected to replay
                    if(SELECTED_REPLAY_MESSAGE_POSITION!=-1){
                        val selected_msg_number = all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].msg_num    // msg number of selected message
                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_chat"){               // text_text
                            val _text = all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data                   // the normal text that we sended
                            val replay_ = replay_media_model(  _text,
                                "",
                                "$SELECTED_REPLAY_MESSAGE_POSITION",
                                "$selected_uri",
                                "",total_pages,front_image_link,  // this are for pdf if it is pdf
                                file_name,total_size_document)

                            string_data = mapper.writeValueAsString(replay_)
                            replay_status = "text_${pdf_type}"
                        }

                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_i"){      // selected message is IMAGE and replied TEXT
                            val image_uri = mapper.readValue<image_data_model>(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data)
                            val replay_ = replay_media_model( image_uri.download_uri,
                                image_uri.local_url,
                                "$selected_msg_number",
                                "$selected_uri",
                                "",total_pages,front_image_link,file_name,total_size_document)

                            string_data = mapper.writeValueAsString(replay_)
                            replay_status = "image_$pdf_type"                  // this means u are selecting images to replay documents
                            Log.d("","ppppppppppppreparing for image selected doc replay")
                        }
                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_v"){         // this means this is selecting the video to replay with document

                        }

                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="voting"){   // already topic text is selected in tthis variable so this is ntrmal text_text
                            val data_ = mapper.readValue<voting_template>(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data)
                            val replay_ = replay_media_model (   data_.topic ,
                                "",
                                "$selected_msg_number",
                                "$selected_uri",
                                "",total_pages,front_image_link,file_name,total_size_document )
                            string_data = mapper.writeValueAsString(replay_)
                            replay_status = "text_$pdf_type"
                            Log.d("","ppppppppppppppppppppppreparing for voting replay")
                        }

                        // this initiate if selected messag to replay is document
                        // this is text_to_text replay
                        if(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="g_doc" || all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].category=="PDF"){
                            val _data = mapper.readValue<documents_model>(all_group_chats[SELECTED_REPLAY_MESSAGE_POSITION].data)    // this is data of selected document to replay
                            val replay_ = replay_media_model(    _data.file_name,
                                _data.local_uri,
                                "$selected_msg_number",
                                "$selected_uri",
                                "",total_pages,front_image_link,file_name,total_size_document)

                            string_data = mapper.writeValueAsString(replay_)
                            replay_status = "text_$pdf_type"
                            Log.d("","ppppppppppppppppppreparing for voting replay")
                        }
                    }

                    // category of document is just g_doc the original  document is under file data parsing it
//                    val group_message_model_ = group_message_model(
//                        group_name,group_number, "$TOTAL_MESSAGE",string_data,
//                        "$replay_status","$str_read_deliver","$str_read_deliver","none",
//                        "${System.currentTimeMillis()}",false,false,
//                        "none","none",MY_NUMBER_LONG,"none","none" )

                    val sending_data = one_chat_property(PAIR_,NAME_,"$TOTAL_MESSAGE",string_data,"$replay_status",false,"none",get_send_time(),false,false,
                                                        "none","none",false,MY_NUMBER_LONG,NUMBER_,"none","none")

//                    DB.save_group_message(group_name,group_number, "$TOTAL_MESSAGE",string_data,
//                        "$replay_status","$str_read_deliver","$str_read_deliver","none",
//                        "${System.currentTimeMillis()}",false,false,
//                        "none","none",MY_NUMBER_LONG,"none","none" )

                    DB.save_message(PAIR_,NAME_,"$TOTAL_MESSAGE","$replay_status",false,"none",get_send_time(),"none",false,
                        MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"none",NAME_)

                    handler.post {     // this is connecting to the UI thread to show the messages
                        all_group_chats.add(sending_data)
                        initiate_message_me(all_group_chats)
                        Toast.makeText(context_,"The link of thumbnail : ${front_image_link}",Toast.LENGTH_LONG).show()
                    }
                }).start()
            }

            if(data!!.clipData!==null){
                Toast.makeText(context_,"You selected multiple files to send!",Toast.LENGTH_LONG).show()
            }
        }


        // for choosing contacst to send back
        if(resultCode==Activity.RESULT_OK && requestCode==CHOOSING_CONTACTS){
            TOTAL_MESSAGE++
            var str_data = ""
            str_data = data!!.getStringExtra("choose_contact").toString()

            if(str_data!=""){
                val read_ = HashMap<String, String>()
                val str_read = mapper.writeValueAsString(read_)
                val handler = Handler()
                val DB = universal_chat_store(context_,null)
                Thread({
                    val recieved_data = mapper.readValue<ArrayList<choose_contacts_model>>(str_data)

                    for(i in recieved_data) {
                        val single_data = mapper.writeValueAsString(i)     // data of single contact
                        val send_data = one_chat_property(PAIR_,NAME_,"$TOTAL_MESSAGE",single_data,"contact",false,"none",get_send_time(),false,
                                                         false,"none","none",false,MY_NUMBER_LONG,NUMBER_,"none","none")
                        all_group_chats.add(send_data)
                        DB.save_message(PAIR_,"$TOTAL_MESSAGE",single_data,"contact",false,"none",get_send_time(),"none",false,
                                          MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"none",NAME_)
                    }

                    handler.post {
                        initiate_message_me(all_group_chats)    // displaying the data
                    }
                }).start()
            }

            if(str_data=="")Toast.makeText(this,"You did'nt selected any contacts",Toast.LENGTH_LONG).show()
        }

        // Accessing camera
        if(resultCode==Activity.RESULT_OK && requestCode==CAMARA_ACCESS && data!=null){
            val bitmap_ = BitmapFactory.decodeFile(click_photo_file!!.absolutePath)
            val uri_ = saveImage(bitmap_)
            val gg = ArrayList<image_select_model>()
            gg.add(image_select_model(uri_,true))
            val send_data = mapper.writeValueAsString(gg)
            val intent = Intent(this,showing_multiple_photos::class.java)
            intent.putExtra("all_uri",send_data)
            startActivityForResult(intent,REQUEST_CODE_ACCEPTING_DATA)    //  this will
        }

    }

    // save to images to the gallery
    private fun saveImage(bitmap_ : Bitmap) : String{
        // Save image to gallery
        val savedImageURL = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap_,
            "compressed_image",
            "Image_of_compressed"
        )
        return savedImageURL
    }

    // calculate the size of image
    fun sizeOf(path_ : String) : String {
        var file_size_txt = ""
        val input_stream : InputStream ? = this.contentResolver.openInputStream(path_.toUri())
        val file_size : Int = input_stream!!.available()     // calculating the size of the video
        input_stream.close()
        if(file_size%1000>=1){
            var kb = file_size/1000
            if(kb/1000>1){    // in MB
                file_size_txt = "${kb/1000} MB"
            }
            if(kb/1000<1){     // this is in KB
                file_size_txt ="${kb} KB"
            }
        }
        return file_size_txt
    }

    // showing the choosing images , videos template
    private fun show_bottom_dialog(){    // The options will come for choosing files and photos
        val sheet : BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.attachment_of_files_option)

        val photos : ImageButton = sheet.findViewById(R.id.choose_photo_linear_layout)!!
        val videos : ImageButton = sheet.findViewById(R.id.choose_videos_layout_id)!!
        val _files : ImageButton = sheet.findViewById(R.id.choose_files_linear_id)!!
        val _contacts : ImageButton = sheet.findViewById(R.id.Choose_contact_attachment)!!
        val camera : ImageButton = sheet.findViewById(R.id.choose_camera)!!


        // when user click to photos options
        photos.setOnClickListener{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permission,PERMISSION_CODE)
                }
                else{  choose_image_galary() }
            }
            else{
                choose_image_galary()
            }

            sheet.dismiss()
        }

        videos.setOnClickListener{   // when user click to videos options
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            intent.action = Intent.ACTION_OPEN_DOCUMENT                   //  this one allow to pick multiple image from gallery
            startActivityForResult(intent , SELECT_VIDEO)
            sheet.dismiss()
        }

        _files.setOnClickListener{    // when user click to file options
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
            startActivityForResult(Intent.createChooser(intent, "Select a file"),SELECT_FILES)    // this will select files
            sheet.dismiss()
        }

        _contacts.setOnClickListener{   // when user click to contacts options
            val intent= Intent(this,chips_formation::class.java)
            intent.putExtra("choose_contact","true")   // this is for allow to choose contacts
            startActivityForResult(intent,CHOOSING_CONTACTS)
            sheet.dismiss()
        }

        // clicking photo
        camera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            click_photo_file = get_file_path_camera()
//            val file_provider =  FileProvider.getUriForFile(this,
//                                  "com.example.database_part_3.groups",
//                                   click_photo_file!! )

            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,click_photo_file)
            if(cameraIntent.resolveActivity(this.packageManager)!=null){
                startActivityForResult(cameraIntent, CAMARA_ACCESS)
            }
            else{
                Toast.makeText(this,"Sorry , unable to open camera",Toast.LENGTH_LONG).show()
            }
            sheet.dismiss()
        }
        sheet.show()
    }

    // Compile after choosing the image from template
    private fun choose_image_galary(){    // this one will pick all the images
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action = Intent.ACTION_OPEN_DOCUMENT                   //  this one allow to pick multiple image from gallery
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),IMAGE_CHOOSE)
    }
    //######################################################


    //############ template selection part function ############
    private fun selection_template(){
//        reaction_store_selection    ,  selection_vote_template
        val sheet : BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.select_template_option)           // for selections of template
        sheet.show()

        val _reaction = sheet.findViewById<RelativeLayout>(R.id.reaction_store_selection)
        val _vote = sheet.findViewById<RelativeLayout>(R.id.selection_vote_template)
        val _show_selection = sheet.findViewById<TextView>(R.id.show_template_selection)
        val topic = sheet.findViewById<EditText>(R.id.topic_edit_text)
        val _post = sheet.findViewById<Button>(R.id.template_post_id)
        val voting_text_view = sheet.findViewById<TextView>(R.id.topic_show_in_vote_bar)
        val reaction_store_text_view = sheet.findViewById<TextView>(R.id.show_topic_id)

        val _reaction_layout = sheet.findViewById<RelativeLayout>(R.id.reaction_store_selection)   // for changing the background color of layoyt after selecting template
        val _voting_layout = sheet.findViewById<RelativeLayout>(R.id.selection_vote_template)      // for changing the background color of layoyt after selecting template

        var TEMPLATE_NUMBER = ""

        _reaction!!.setOnClickListener{
            TEMPLATE_NUMBER = "storing_reaction"
            _show_selection!!.setText("Enter Topic For '${TEMPLATE_NUMBER}' template")
            _reaction_layout!!.setBackgroundResource(R.color.light_violet)     // changing the background color
            _voting_layout!!.setBackgroundResource(R.color.white)
        }
        _vote!!.setOnClickListener {
            TEMPLATE_NUMBER = "voting"
            _show_selection!!.setText("Enter Topic For '${TEMPLATE_NUMBER}' template")
            _reaction_layout!!.setBackgroundResource(R.color.white)        // changing the background color
            _voting_layout!!.setBackgroundResource(R.color.light_violet)
        }

        var read_ = HashMap<Int,String>()
        var delivered_ = HashMap<Int,String>()
        var read_str = mapper.writeValueAsString(read_)
        var delivered_str = mapper.writeValueAsString(delivered_)

        // 'store' arrange and save all the data that save to data base and send to the adapter

        var store = one_chat_property(PAIR_,NAME_,"","","",false,"none",get_send_time(),false,false,
                                       "yes","none",false,MY_NUMBER_LONG,NUMBER_,"none","none")
        var _message_: String = ""


        // live text change of the template
        topic!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(current_text : CharSequence?, p1: Int, p2: Int, p3: Int){
                if(TEMPLATE_NUMBER=="")Toast.makeText(context_,"PLease select any one template of above",Toast.LENGTH_LONG).show()

                if(TEMPLATE_NUMBER=="voting"){
                    voting_text_view!!.setText(current_text)
                    reaction_store_text_view!!.setText("")
                }

                if(TEMPLATE_NUMBER=="storing_reaction"){
                    voting_text_view!!.setText("")
                    reaction_store_text_view!!.setText(current_text)
                }
            }
        })

        val db = universal_chat_store(this,null)

        // after clicking the post button
        _post!!.setOnClickListener{
            TOTAL_MESSAGE++
            var _time : String = "${java.util.Calendar.getInstance().timeInMillis}"   // set this time when message is sended
            val text_ = topic.text.toString()

            if(text_.isEmpty())Toast.makeText(this,"please enter the Topic",Toast.LENGTH_SHORT).show()

            val handler = Handler()
            if(text_.isNotEmpty()){
                if(TEMPLATE_NUMBER == "storing_reaction"){
                    var comment_store = ArrayList<group_message_model>()
                    var reaction_store = reaction_store_group_model(text_,false,0,comment_store)
                    _message_ = mapper.writeValueAsString(reaction_store)

                    store.data = _message_           // saving all message properties to the store
                    store.time_ = _time
                    store.category = "storing_reaction"
                    store.msg_num = "$TOTAL_MESSAGE"
                    val thread : Thread = Thread({
                        db.save_message( PAIR_,"$TOTAL_MESSAGE",_message_,"storing_reaction",false,"none",get_send_time(),"none",
                                         false,MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"yes",NAME_)

                        handler.post {
                            all_group_chats.add(store)
                            initiate_message_me(all_group_chats)
                            sheet.cancel()
                        }
                    })
                    thread.start()
                    db.close()
                }
                if(TEMPLATE_NUMBER == "voting"){
                    var voting_temp : voting_template = voting_template(text_,0,0,0)
                    _message_ = mapper.writeValueAsString(voting_temp)

                    store.data = _message_           // saving all message properties to the store
                    store.time_ = _time
                    store.category = "voting"
                    store.msg_num = "$TOTAL_MESSAGE"
                    val thread: Thread = Thread({

                        db.save_message( PAIR_,"$TOTAL_MESSAGE",_message_,"voting",false,"none",get_send_time(),"none",
                            false,MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"yes",NAME_)

                        handler.post {
                            all_group_chats.add(store)
                            initiate_message_me(all_group_chats)
                            sheet.cancel()
                        }
                    })
                    thread.start()
                    db.close()
                }
//                all_group_chats.add(store)
//                initiate_message_me(all_group_chats)
//                sheet.cancel()
            }
        }
    }
    // ###############


    // #######  vedio compressor player before sending #############
    private fun video_compressor_before_send(quality_ : String , progress_ : String , uri_ : String , completed_uri : String, file_size : String) {
        // showing the option of pixel selections

        //setUp for original for original vidio details
        if(quality_=="ORIGINAL"){     // set all the video quality

            sheet_ = BottomSheetDialog(this)
            sheet_.setContentView(R.layout.video_pixel_selection)
            if(uri_!="")kk.add(uri_.toUri())

            layout_1 = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_1)!!       // compression for 240P
            layout_2 = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_2)!!       // compression for 360P

            button_1 = sheet_.findViewById<Button>(R.id.compress_240_button)!!
            button_2 = sheet_.findViewById<Button>(R.id.compress_360_button)!!
            original_video_layout = sheet_.findViewById<RelativeLayout>(R.id.compression_layout_3)!!
            send_image = sheet_.findViewById<Button>(R.id.send_image_id)!!

            val thumbnail_ = sheet_.findViewById<ImageView>(R.id.compressed_image_3)      // for thumbnail
            val text_size = sheet_.findViewById<TextView>(R.id.compressed_size_id_3)
            text_size!!.setText("ORIGINAL SIZE $file_size")
            Glide.with(context_).asBitmap().load(uri_).into(thumbnail_!!)       // image view of thumbnail
            val size_ = getBytes(uri_.toUri())
            val original_size_text = sheet_.findViewById<TextView>(R.id.compressed_size_id_3)
            original_size_text!!.setText("$size_")
        }


        // for commanding compressor
        button_2.setOnClickListener {                    // it converts 360P
            if(VIDEO_COMPRESS_ELIGIBILITY==true) video_compressor_(context_).compress_video(kk,"360p")      // now this  will broadcast the progress of compression
            if(VIDEO_COMPRESS_ELIGIBILITY==false)Toast.makeText(context_,"Opps Video is less than 10MB cannot compress , Sorry ",Toast.LENGTH_LONG).show()
        }


        button_1.setOnClickListener {                    // it converts 240P
            if(VIDEO_COMPRESS_ELIGIBILITY==true)video_compressor_(context_).compress_video(kk,"240p")
            if(VIDEO_COMPRESS_ELIGIBILITY==false)Toast.makeText(context_,"Opps Video is less than 10MB cannot compress , Sorry ",Toast.LENGTH_LONG).show()
        }


        // for selections color when layout is selected
        layout_1.setOnClickListener {         // for 240p
            original_video_layout.setBackgroundResource(R.color.white)
            layout_2.setBackgroundResource(R.color.white)
            layout_1.setBackgroundResource(R.color.light_blue)

            PICK_IMAGE = compressed_path_240
            PIXEL_SELECT_STATUS = "240p"
        }

        layout_2.setOnClickListener {      // for 360p
            original_video_layout.setBackgroundResource(R.color.white)
            layout_2.setBackgroundResource(R.color.light_blue)
            layout_1.setBackgroundResource(R.color.white)

            PICK_IMAGE = compressed_path_360    // prepare for database
            PIXEL_SELECT_STATUS = "360p"
        }

        original_video_layout.setOnClickListener {
            original_video_layout.setBackgroundResource(R.color.light_blue)
            layout_2.setBackgroundResource(R.color.white)
            layout_1.setBackgroundResource(R.color.white)

            PICK_IMAGE = original_path   // copying the uri of original video
            PIXEL_SELECT_STATUS = "ORIGINAL"
        }

        // now this is sending of the vedio
        send_image.setOnClickListener{
            val handler = Handler()
            if(PIXEL_SELECT_STATUS=="240p")delete_through_uri(compressed_path_360.toUri())
            if(PIXEL_SELECT_STATUS=="360p") delete_through_uri(compressed_path_240.toUri())     // after selections of video it will delete the uneccessary one

            var vedio_detail = video_model(DURATION_OF_VIDEO_SELECTED ,"$file_size","","",PICK_IMAGE,PIXEL_SELECT_STATUS)

            val DB = universal_chat_store(this,null)
            Thread({
                TOTAL_MESSAGE++
                val save_data = mapper.writeValueAsString(vedio_detail)                 // ready to save to data base

                val send_data = one_chat_property(PAIR_,NAME_,"$TOTAL_MESSAGE",save_data,"g_v",false,"none",get_send_time(),false,false,
                                                 "none","none",false, MY_NUMBER_LONG,NUMBER_,"none","none")


                DB.save_message(PAIR_,"$TOTAL_MESSAGE",save_data,"g_v",false,"none",get_send_time(),"none",false,
                                    MY_NUMBER_LONG,"$NUMBER_","none","none",false,false,"none",NAME_)

                handler.post {
                    all_group_chats.add(send_data)
                    initiate_message_me(all_group_chats)
                    kk.clear()      // clearing the index
                    PICK_IMAGE = ""
                }
            }).start()
        }

        // updating UI when compression is complete for the respective video
        if(progress_!=""){
            if(quality_=="240p"){     // means compression is done for 240 P
                button_1.setText("COMPRESSING...${progress_}")

                if(progress_=="100%"){      // means the compression is done and uri is arrived
                    compressed_path_240 = completed_uri         // uri of compressed video
                    button_1.visibility = View.GONE
                    layout_1.visibility = View.VISIBLE
                    val thumnail_of_240 = sheet_.findViewById<ImageView>(R.id.compressed_image_1)     // thumbnail view
                    Glide.with(context_).asBitmap().load(completed_uri).into(thumnail_of_240!!)       // image view of thumbnail
                    // attach the size and pixel
                    val size_show_in_text = sheet_.findViewById<TextView>(R.id.compressed_size_id_1)
                    size_show_in_text!!.setText(file_size)       // attaching the file size
                }
            }

            if(quality_=="360p"){     // means compression is done for 360 P
                button_2.setText("COMPRESSING...${progress_}")

                if(progress_=="100%"){
                    compressed_path_360 = completed_uri       // after compression the link of the video
                    button_2.visibility = View.GONE
                    layout_2.visibility = View.VISIBLE
                    val thumbnail_360 = sheet_.findViewById<ImageView>(R.id.compressed_image_2)        //  compression layout 360P
                    Glide.with(context_).asBitmap().load(completed_uri).into(thumbnail_360!!)       // image view of thumbnail
                    val size_text_show = sheet_.findViewById<TextView>(R.id.compressed_size_id_2)    // size text View
                    size_text_show!!.setText(file_size)    // this file size will for the specific after compressed video
                }
            }
        }

        if(quality_=="ORIGINAL"){
            sheet_.show()
        }

    }


    // color selection for videos
    fun color_selection_video(layout_number : Int){
        if(PICK_IMAGE!="")send_image.setBackgroundResource(R.drawable.dark_color_rectangle)

        if(layout_number==1){
            compression_1.setBackgroundResource(R.color.gray)
            compression_2.setBackgroundResource(R.color.white)
        }
        if(layout_number==2){
            compression_1.setBackgroundResource(R.color.white)
            compression_2.setBackgroundResource(R.color.gray)
        }
    }


    // conversion of uri to video thumbnail
    fun uri_to_thumbnail(uri_ : Uri , image_show : ImageView){     //  working fine
        Glide.with(this).asBitmap().load(uri_).into(image_show);   // setting the thmbnail of vedio
    }
    // ########################## finished of video sending ############


    //############# functions for editable text messages ##############
    private fun edit_message_show(){
        input_text.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(input_text,InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
        edit_text_view_layout.visibility = View.VISIBLE

        // when the edit text is change then change the textView part of message layout
        input_text.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?){}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(current_text : CharSequence?, p1: Int, p2: Int, p3: Int) {
                edit_text_view.setText(current_text)    // just changing the text this will update the textView
            }
        })
    }


    private fun get_send_time() : String{
        return "${Calendar.HOUR}:${Calendar.MINUTE}${Calendar.AM_PM}"
    }


    // get file extention from giving uri to this function
    // used only for file sharing part
    fun get_type(file_name : String ?) : String ? {
        var type  = ""
        type = "${File(file_name).extension}"           // this will give the extension of file
        return type
    }


    // get the file size from the uri
    fun getBytes(uri_ : Uri) : String{
        val inputStream = contentResolver.openInputStream(uri_)
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        val size_ = byteBuffer.toByteArray().size
        var size_str = ""
        val tt = size_/1000

        if(tt<1000){
            size_str = "$tt KB"
        }
        if(tt>1000){
            if((tt/1000)>=10)VIDEO_COMPRESS_ELIGIBILITY = true     // calculates here if the video compress eligibilitu
            size_str = "${tt/1000} MB"
        }

        return size_str
    }


    // get file name from uri  specially used by document
    fun get_file_name(uri: Uri) : String?{
//        resolver : ContentResolver,
        val returnCursor: Cursor =  context_.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }


    // save image get uri
    // this works perfect
    private fun get_file_path_camera(): File? {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "NAME_$timeStamp"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    // this works perfect
    fun save_thumbnail_from_bitmap(bitmap_ : Bitmap,file_name : String) : String{
        var file_uri = ""
        try {
            val path = Environment.getExternalStorageDirectory().toString()
            val file_ : File = File(path,"${file_name}.jpg")
            val fout = FileOutputStream(file_)
            bitmap_.compress(Bitmap.CompressFormat.JPEG,0,fout)    // this will save the image at 80% compression
            fout.close()

            file_uri = MediaStore.Images.Media.insertImage(contentResolver,file_.absolutePath,file_.name,file_.name)

        } catch (e:IOException) {
            e.printStackTrace()
        }
        return file_uri
    }

    //$$$$$$$$$$$$$$$$$ for camera $$$$$$$$$ finished


    // for broadcast recieving data
    private val broad_cast_receiver_ = object : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?){
            val progress_ = intent!!.getStringExtra("PROGRESS").toString()         // this will receive the number percent of compression
            val completed_uri = intent.getStringExtra("FINAL_URI")      // final uri of compression
            val quality_ = intent.getStringExtra("QUALITY")              // in which quakity the video is compressing
            val file_size_ = intent.getStringExtra("SIZE")

            Log.d("","ttttttttttthe progress listenner in % is:${progress_} & final_path :${completed_uri}")

            video_compressor_before_send(quality_!!,progress_,"",completed_uri!!,file_size_!!)  // whenever this broadcast will compile this functions will update UI
        }
    }


    // deleting video after compress
    private fun delete_through_uri(uri_ : Uri){
        val file_path = File(uri_.path)    // this is path of file
        if(file_path.delete())Toast.makeText(this,"file is deleted!",Toast.LENGTH_LONG).show()
    }


    //  this will give the current time format
    private fun current_time_format() : String{
        val date = Date()                        // this will give the current date with current time
        val time_format_ = SimpleDateFormat("hh.mm aa")     // this will automatically convert into time format
        val time_ = time_format_.format(date)    // this will arrange the time in the format
        return time_
    }

}
