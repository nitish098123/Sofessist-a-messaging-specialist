package com.example.database_part_3.groups_template_section

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.SWIPE_TO_EDIT_MESSAGE.swipe_to_edit
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.Items_detail_look_up
import com.example.database_part_3.groups.MY_NUMBER
import com.example.database_part_3.groups.group_chat_adapter
import com.example.database_part_3.groups.group_message_model
import com.example.database_part_3.image_size_reducer.ImageResizer
import com.example.database_part_3.message_holder.MessageSwipeController
import com.example.database_part_3.message_holder.SwipeControllerActions
import com.example.database_part_3.message_holder.TOTAL_SELECT_IMAGE
import com.example.database_part_3.model.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class comment_section_group : AppCompatActivity() , ActionMode.Callback{
    private lateinit var adapter : comment_group_adapter
    val mapper = jacksonObjectMapper()
    var TOTAL_MESSAGE = 0
    var PRIVATE_CHAT = false
    private lateinit var comment_recycle_view : RecyclerView
    private lateinit var group_number : String
    private lateinit var group_name : String
    private var actionMode : ActionMode? = null
    private var all_comments : ArrayList<group_message_model>  = ArrayList<group_message_model>()        // all the updating message is stored here to send to the adapter
    private var thread_store : reaction_store_group_model = reaction_store_group_model("",false,0,ArrayList<group_message_model>())
    private var SELECTED_REPLAY_MESSAGE = -1
    private var SELECTED_REPLAY_TEXT = ""
    private lateinit var compression_1 : RelativeLayout
    private lateinit var compression_2 : RelativeLayout
    private lateinit var compression_3 : RelativeLayout
    private lateinit var send_image : Button
    private lateinit var data_input : EditText
    private var selectedPostItems : MutableList<Long> = mutableListOf()       // this contains the message position that is selected for activation
    private lateinit var reply_layout : RelativeLayout
    private lateinit var cancelButton : ImageButton
    private lateinit var txtQuotedMsg : TextView
    private var tracker : SelectionTracker<Long> ? = null
    private lateinit var msg_number : String
    private var first_click : Long = 0              // this is used for one click event
    private val DELTA_TIME_DOUBLE_CLICK = 360       //  this 1000 ms is the time between the two click of double click
    private var EDIT_MESSAGE : Boolean = false
    private var POSITION_CONTAINER = 0                  // if the edit text is selected then this contains the position of edit
    private lateinit var replying_layout_id : RelativeLayout
    private lateinit var edit_text_view : TextView
    private lateinit var  context_ : Context
    private lateinit var edit_text_view_layout : RelativeLayout
    private lateinit var cancel_edit_text_button : ImageView

    var PICK_IMAGE : String = ""
    private val IMAGE_CHOOSE = 1000
    private val PERMISSION_CODE = 1001
    private val SELECT_VIDEO = 1000
    private val SELECT_FILES = 1000

    override fun onCreate(savedInstanceState: Bundle?){          // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reaction_template_comment_view)

        // bringing the ids from the xml part
        val discuss_topic = findViewById<TextView>(R.id.discussion_topic_id)
        reply_layout = findViewById(R.id.reply_layout)
        cancelButton = findViewById(R.id.cancelButton)
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg)
        data_input = findViewById(R.id.text_comment_id)
        replying_layout_id = findViewById(R.id.replying_layout_id)
        edit_text_view = findViewById(R.id.live_text_view_show)
        edit_text_view_layout = findViewById(R.id.edit_message_layout_id)
        cancel_edit_text_button = findViewById<ImageView>(R.id.cancel_edit_text)
        context_ = this

        // receives data from intent that is given
        val reaction_store_string : String = intent.getStringExtra("+comments").toString()
        val sender_name : String = intent.getStringExtra("+sender_name").toString()
        msg_number = intent.getStringExtra("+msg_number").toString()
        group_number = intent.getStringExtra("+group_number").toString()
        group_name = intent.getStringExtra("+group_name").toString()
        PRIVATE_CHAT = intent.getStringExtra("+private_chat").toBoolean()
        val topic = intent.getStringExtra("+topic").toString()
        discuss_topic.setText(topic)    // setting the topic of  reactions store template

        // for restricting screenshot of chats
        if(PRIVATE_CHAT==true)window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        if(PRIVATE_CHAT==false){
            val private_chat_ = findViewById<ImageView>(R.id.private_chat_thread_)
            private_chat_.visibility = View.GONE                                    // if it is not private chat then make it invisible
        }

        // data must be bring from the database
        val handler = Handler()
        val DB = universal_chat_store(this ,null)
        val thread = Thread({
           val data_ : reaction_store_group_model? =  DB.get_group_message_one_template(group_number,msg_number)   // bringing data from the data base

            if(data_!=null)thread_store = data_
            //  discuss_topic.text = thread_store!!.topic
            if(thread_store!=null){
                all_comments = thread_store.total_comment

                if (all_comments.size > 0){
                    val tt = all_comments.size
                    TOTAL_MESSAGE = all_comments[tt - 1].msg_num.toInt()    // this the last message number of one thread
                }
            }

            handler.post{
                comment_recycle_view = findViewById(R.id.comment_recycle_view)
                comment_recycle_view.layoutManager = LinearLayoutManager(this)
                adapter = comment_group_adapter(this)
                comment_recycle_view.adapter = adapter

                // for message selection
                tracker = SelectionTracker.Builder<Long>(
                    "mySelection",
                    comment_recycle_view,
                    StableIdKeyProvider(comment_recycle_view),
                    group_items_details_lookup(comment_recycle_view),
                    StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
                adapter.tracker = tracker

                //    making the tracker for selections
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
                                if(actionMode==null) actionMode = startSupportActionMode(this@comment_section_group)
                                actionMode?.title = "${selectedPostItems.size}"        // number of selected items to show in toolbar
                            }
                        }
                    }
                })


                // this is for replaying the message text swipe right
                val message_swipe_controller = MessageSwipeController(this,object : SwipeControllerActions {
                    override fun showReplyUI(position : Int){             // this will give the recycle View item positions number
                        SELECTED_REPLAY_MESSAGE = position
                        edit_text_view_layout.visibility = View.GONE
                        if(all_comments[position].category=="g_chat"){

                            if(all_comments[position].replied_msg=="yes"){
                                val text_str = all_comments[position].data
                                val store_ = mapper.readValue<replay_data_model>(text_str)
                                txtQuotedMsg.text = store_.reply_message
                                SELECTED_REPLAY_TEXT = store_.reply_message
                                showQuotedMessage()
                            }
                            if(all_comments[position].replied_msg=="none"){
                                txtQuotedMsg.text = all_comments[position].data
                                SELECTED_REPLAY_TEXT = all_comments[position].data
                                showQuotedMessage()
                            }
                        }
                        if(all_comments[position].category=="g_i"){

                        }
                        if(all_comments[position].category=="sticker"){

                        }
                    }
                })

                val itemTouchHelper = ItemTouchHelper(message_swipe_controller)
                itemTouchHelper.attachToRecyclerView(comment_recycle_view)


                // for the swioing functions for swiping message left side for reedit the message
                // only text messages are editable not other template types of messages or messages
                val edit_message = swipe_to_edit(this,object : SwipeControllerActions{
                    override fun showReplyUI(position: Int){
                        EDIT_MESSAGE = true
                        POSITION_CONTAINER = position
                        replying_layout_id.visibility = View.GONE
                        if(all_comments[position].category=="g_chat"){

                            if(all_comments[position].replied_msg=="yes"){
                                val text_str = all_comments[position].data
                                val store_ = mapper.readValue<replay_data_model>(text_str)
                                edit_text_view.text = store_.reply_message  // showing the message before editing it
                                edit_message_show()                         // calling the functions for edit live edit text
                                data_input.setText(store_.reply_message)     // displaying the message in the edit text so that user will know what text to edit from before
                            }

                            if(all_comments[position].replied_msg=="none"){
                                val text_ = all_comments[position].data        // just showing the text that the user is going to edit
                                edit_text_view.text = text_
                                edit_message_show()      // calling the functions for edit live edit text
                                data_input.setText(text_)     // displaying the message in the edit text so that user will know what text to edit from before
                            }
                        }

                        Toast.makeText(context_,"You are editing your message to resend it",Toast.LENGTH_LONG).show()
                    }
                })
                val editing_messages = ItemTouchHelper(edit_message)
                editing_messages.attachToRecyclerView(comment_recycle_view)

                initiate_process_me(all_comments)

            }
        })

        thread.start()

        // setting the sender name
        val sender_ : TextView = findViewById(R.id.sender_show_id)
        sender_.text = "From : " + sender_name

        // Replay layout left swipe of message
        // for message replay layout

        val send_ = findViewById<Button>(R.id.send_comment_id)


        // posting the comments
        send_.setOnClickListener{
            TOTAL_MESSAGE++
            var replay_status = "none"
            val time_ = "${System.currentTimeMillis()}"
            var message_ : String = data_input.text.toString()
            data_input.setText("")
            val read_ = HashMap<Int,String>()

            if(SELECTED_REPLAY_MESSAGE!=-1){
                val replay_ = replay_data_model(SELECTED_REPLAY_TEXT,"$SELECTED_REPLAY_MESSAGE",message_)
                message_ = mapper.writeValueAsString(replay_)
                replay_status = "yes"
            }

            // if edit message is not activated
            if(EDIT_MESSAGE==false) {
                val comment_post = group_message_model(
                    group_name,
                    group_number,
                    "$TOTAL_MESSAGE",
                    message_,
                    "g_chat",
                    mapper.writeValueAsString(read_),
                    mapper.writeValueAsString(read_),
                    "none",
                    time_,
                    false,
                    false,
                    "none",
                    "none",
                    MY_NUMBER,
                    replay_status,
                    "none"
                )

                all_comments.add(comment_post)                         // this is the updated versions of latest comment
                thread_store.total_comment = all_comments              // this thread_store is updated to save in database

                val save_data: String = mapper.writeValueAsString(thread_store)       // this thread store String is for data column
                val DB = universal_chat_store(this, null)
                val thread = Thread({
                    // this all_comments store all messages that is sended in the thread
                    DB.update_group_reaction_template(
                        "you_comment",
                        group_number,
                        msg_number,
                        save_data
                    )
                    DB.close()
                })
                thread.start()
                initiate_process_me(all_comments)    // for sending to the adapter
                hideReplayLayout()
            }

            // if edit message is activated
            if(EDIT_MESSAGE==true){
                val send_data = group_message_model(
                    group_name,
                    group_number,
                    "$TOTAL_MESSAGE",
                    message_,
                    "g_chat",
                    mapper.writeValueAsString(read_),
                    mapper.writeValueAsString(read_),
                    "none",
                    time_,
                    true,
                    false,
                    "none",
                    "none",
                    MY_NUMBER,
                    replay_status,
                    "none"
                )
                all_comments[POSITION_CONTAINER] = send_data

                adapter.update_list(
                    "EDIT_REWRITE",
                    mapper.writeValueAsString(send_data),
                    POSITION_CONTAINER
                )    // sending to the adapter

                val DB = universal_chat_store(this, null)
                Thread({
                    thread_store.total_comment = all_comments
                    val final_updated_data = mapper.writeValueAsString(thread_store)
                    DB.update_group_comments("EDIT_REWRITE",group_number,msg_number,final_updated_data)
                }).start()

                cancel_edit_text()
            }
        }

        val attachment : ImageView = findViewById(R.id.options_file_choose_id_comment)
        attachment.setOnClickListener{
            show_bottom_dialog()
        }

        // for cancelling the replay layout
        cancelButton.setOnClickListener {
            hideReplayLayout()
        }

        // for cancelling the edittext layout
        cancel_edit_text_button.setOnClickListener{
            cancel_edit_text()
        }
    }

    private fun initiate_process_me(data : ArrayList<group_message_model>) {         // this will take all the type of message and will
        if (data != null){
            adapter.setData(data)
            comment_recycle_view.scrollToPosition(adapter.itemCount - 1)    // viewing on the screen the last message
        }
    }
    // for replay layout
    private fun showQuotedMessage(){
        data_input.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(data_input,InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
        replying_layout_id.visibility = View.VISIBLE
        cancel_edit_text()
    }

    // cancelling the replay layout
    private fun hideReplayLayout(){                      // this functions hide the replay layout for if not required
        reply_layout.visibility = View.GONE
        SELECTED_REPLAY_MESSAGE = -1
        SELECTED_REPLAY_TEXT = ""
    }

    fun initiate_message_me(_data : ArrayList<group_message_model>){
        adapter.setData(_data)
        comment_recycle_view.scrollToPosition(adapter.itemCount-1)    // for viewing the last message that is sended
    }

    // for editting message after sended
    private fun edit_message_show(){

        hideReplayLayout()        // so that if replay layout is selected then it will cancel at first place

        data_input.requestFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(data_input,InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
        edit_text_view_layout.visibility = View.VISIBLE

        // when the edit text is change then change the textView part of message layout
        data_input.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?){}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(current_text : CharSequence?, p1: Int, p2: Int, p3: Int) {
                edit_text_view.setText(current_text)    // just changing the text this will update the textView
            }
        })
    }

    //  #######################################  presshold section ####################
    // allowing the menu items which one is to visible when
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let{
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.presshold_menu,menu)

            if(com.example.database_part_3.message_holder.PRIVATE_CHAT ==true){                        // this controlls what menu should I show during presshold
                val copy_: MenuItem = menu!!.findItem(R.id.copy_msg_id)
                val forward: MenuItem = menu.findItem(R.id.forward_msg_id)
                copy_.setVisible(false)
                forward.setVisible(false)
            }
            return true
        }
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) : Boolean {  // this functions controlled the clicked long pressed functions
        Toast.makeText(this,"OnActionItemClicked is Activated!!",Toast.LENGTH_LONG).show()
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode : ActionMode?){
        adapter.tracker?.clearSelection()
        actionMode = null
    }
    //  ######################################################


    //############################ Image section ###################
    // now for detecting the url of choosing the image after selections from gallery
    fun color_selection(layout_number : Int){

        if(PICK_IMAGE!="")send_image.setBackgroundResource(R.drawable.dark_color_rectangle)

        if(layout_number==1){
            compression_1.setBackgroundResource(R.color.casual_white)
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

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int,
                                  data : Intent?)
    {

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
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
                val reduced_bitmap_2: Bitmap = ImageResizer.reduceBitmapSize(full_size_bitmap, 360000)
                val reduced_bitmap_3: Bitmap = ImageResizer.reduceBitmapSize(full_size_bitmap, 300000)

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
                    color_selection(1)
                }
                compression_2.setOnClickListener {
                    PICK_IMAGE = uri_2
                    color_selection(2)
                }
                compression_3.setOnClickListener {
                    PICK_IMAGE = uri_3
                    color_selection(3)
                }

                // for viewing the full image pixels
                // when clicked in image to fullView
                image_1.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", file_path.toString())
                    anotherIntent.putExtra("+private_chat", com.example.database_part_3.message_holder.PRIVATE_CHAT.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: original")
                }
                image_2.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_2)
                    anotherIntent.putExtra("+private_chat", com.example.database_part_3.message_holder.PRIVATE_CHAT.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: compressed_2")
                }
                image_3.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_3)
                    anotherIntent.putExtra("+private_chat", com.example.database_part_3.message_holder.PRIVATE_CHAT.toString())
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

                        /*  upload image to server and take the link of that image and then store to a variable  */

                        val data_ : image_data_model = image_data_model("","",PICK_IMAGE,"","")
                        val data_str = mapper.writeValueAsString(data_)
                        var read_store = HashMap<Int,String>()
                        var delivered_ = HashMap<Int,String>()
                        val read_ : String = mapper.writeValueAsString(read_store)
                        val delivered_str = mapper.writeValueAsString(delivered_)

                        sheet_.dismiss()
                        val send_data = group_message_model(group_name,group_number,message_number,data_str,"g_i",read_,delivered_str,"none",
                                                       time_,false,false,"none","none", MY_NUMBER,"none","none")
                        all_comments!!.add(send_data)
                        initiate_message_me(all_comments!!)
                        val save_data = mapper.writeValueAsString(send_data)

                        TOTAL_SELECT_IMAGE = 0                                   // renewing the number otherwise it will count the previous selected message
                        val DB = universal_chat_store(this,null)
                        val thread = Thread({
                            DB.update_group_reaction_template("you_comment",group_number,"$TOTAL_MESSAGE",save_data)
                        })
                        thread.start()
                        PICK_IMAGE = ""
//                        image_upload().uploadImage(context_,PICK_IMAGE.toUri())
                    }
                }
            }

//            if(data.clipData!=null){      // for multiple photos selection
//                val total_image = data.clipData!!.itemCount
//                Toast.makeText(this,"Total Image you have selected is ${total_image}",Toast.LENGTH_LONG).show()
//                Log.d("iiiiiiiiimage","data.clipData:${data.clipData} & total selected image is: ${total_image}")
//                for (i in 0..total_image-1){
//                    val get_uri : ClipData.Item = data.clipData!!.getItemAt(i)
//                    Log.d("LLLLLLLlllllink_uri","${get_uri.uri}")
//                    send_data.add(
//                        group_message_model(group_name,group_number,message_number,data_str,"g_i",read_,delivered_str,"none",
//                            time_,false,false,"none","none", MY_NUMBER,"none","none")
//                    )
//                    initiate_process_me(send_data)
//                }
//
//                TOTAL_SELECT_IMAGE = 0                                   // renewing the number otherwise it will count the previous selected message
//                val DB = universal_chat_store(this, null)
//                val thread: Thread = Thread({
//                    for (i in 0..total_image-1){
//                        val get_uri : ClipData.Item = data.clipData!!.getItemAt(i)    // this is the must way to extract the URIs of the Images
//
//                    }
//                })
//                thread.start()
//            }
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

        // Parse the gallery image url to uri
        Toast.makeText(this,"${savedImageURL}",Toast.LENGTH_SHORT).show()
        return savedImageURL
    }

    // calculate the size of image
    fun sizeOf(path_ : String): Int {
        val file = File(path_)
        var length = file.length()
        length = length / 1024
        return length.toInt()    // in KB
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


        photos.setOnClickListener{  // when user click to photos options
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
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
            startActivityForResult(intent, SELECT_VIDEO)
            sheet.dismiss()
        }

        _files.setOnClickListener{    // when user click to file options
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"),SELECT_FILES)
        }

        _contacts.setOnClickListener{   // when user click to contacts options
            /* making a new layout for displaying contacts with display pictures */
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


    // for cancelling edit text
    // cancelling the editext
    private fun cancel_edit_text(){
        hideReplayLayout()
        data_input.setText("")
        EDIT_MESSAGE = false
        POSITION_CONTAINER = 0
    }

    override fun onBackPressed() {
//        val data = Intent()
//        data.putExtra("FINAL_URI",arr_str);
//        setResult(Activity.RESULT_OK,data);
//        finish()
        super.onBackPressed()
    }
}