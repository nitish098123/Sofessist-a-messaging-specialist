package com.example.database_part_3.groups

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.icu.util.Calendar
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.view.ActionMode
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
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
import com.example.database_part_3.forward.chips_formation
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.image_size_reducer.ImageResizer
import com.example.database_part_3.image_upload.image_upload
import com.example.database_part_3.message_holder.*
import com.example.database_part_3.model.*
import com.example.database_part_3.sticker.sticker_editor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


var PRIVATE_CHAT_ : Boolean = false
class group_chat_activity : AppCompatActivity() , OnContactClickListener , ActionMode.Callback{

    private lateinit var adapter : group_chat_adapter
    private lateinit var context_ : Context
    private lateinit var message_list_show : RecyclerView
    private var first_click : Long = 0              // this is used for one click event
    private val DELTA_TIME_DOUBLE_CLICK = 360       //  this 1000 ms is the time between the two click of double click
    private var all_group_chats = ArrayList<group_message_model>()
    private var tracker : SelectionTracker<Long> ? = null
    private var selectedPostItems : MutableList<Long> = mutableListOf()       // this contains the message position that is selected for activation
    private var actionMode : ActionMode? = null
    private val mapper = jacksonObjectMapper()
    private var TOTAL_MESSAGE = 0
    private lateinit var compression_1 : RelativeLayout
    private lateinit var compression_2 : RelativeLayout
    private lateinit var compression_3 : RelativeLayout
    private lateinit var send_image : Button
    private lateinit var group_name : String
    private lateinit var group_number : String
    private var SELECTED_REPLAY_MESSAGE = -1        // if this is not -1 then sending is in replay mode
    private var SELECTED_REPLAY_TEXT = ""           // selected message to replay text is store here
    private lateinit var input_text : EditText
    private lateinit var reply_layout : RelativeLayout
    private lateinit var cancelButton : ImageButton
    private lateinit var txtQuotedMsg : TextView
    private lateinit var template_select : ImageView
    private lateinit var edit_text_view :  TextView
    private lateinit var edit_text_view_layout : RelativeLayout
    private lateinit var replying_layout_id : RelativeLayout
    private lateinit var cancel_edit_text : ImageButton
    var PICK_IMAGE : String = ""
    private val IMAGE_CHOOSE = 1000
    private val PERMISSION_CODE = 1001
    private val SELECT_VIDEO = 1000
    private val SELECT_FILES = 1000


    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // now to setting for the initializations of toolbars
        val toolbar_: Toolbar = findViewById(R.id.tool_bar_chat_screen)
        setSupportActionBar(toolbar_)
        toolbar_.showOverflowMenu()
        context_ = this

        val send_button = findViewById<Button>(R.id.btnsend)
        message_list_show = findViewById(R.id.messageList_recycle)
        reply_layout = findViewById(R.id.reply_layout)
        cancelButton = findViewById(R.id.cancelButton)
        txtQuotedMsg = findViewById(R.id.txtQuotedMsg)
        template_select = findViewById(R.id.select_tamplate_id)
        edit_text_view = findViewById(R.id.live_text_view_show)
        edit_text_view_layout = findViewById(R.id.edit_message_layout_id)
        cancel_edit_text = findViewById(R.id.cancel_edit_text)
        replying_layout_id = findViewById(R.id.replying_layout_id)


        group_name = intent.getStringExtra("+group_name").toString()
        group_number = intent.getStringExtra("+group_number").toString()
        val group_private = intent.getStringExtra("+private_chat").toBoolean()


        //  settings for private chats
        PRIVATE_CHAT_ = group_private
        val lock_show = findViewById<ImageView>(R.id.lock_pair_chat_id)
        if(PRIVATE_CHAT_== true)window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE) // for restricting the screenshoot the screen
        if(PRIVATE_CHAT_==false)lock_show.visibility = View.GONE

        // make thread and bring data and send to the adapter
        val handler = Handler()
        val DB = universal_chat_store(this,null)

        val thread = Thread({
            all_group_chats = DB.get_group_messages(group_number)
            val ss = all_group_chats.size

            if(ss>0)TOTAL_MESSAGE = all_group_chats[ss-1].msg_num.toInt()       // copy the last message number of the group message

            handler.post {
                message_list_show.layoutManager = LinearLayoutManager(this)
                message_list_show.setHasFixedSize(true)
                adapter = group_chat_adapter(this, this)
                message_list_show.adapter = adapter

                for(i in all_group_chats){
                    initiate_message_me(i)
                }
                val final_time = System.currentTimeMillis()
                Log.d("","FFFFFFFFFFFFFinal time after adapting is:${final_time}")
                // this is for long pressed detections in the group chat
                tracker = SelectionTracker.Builder<Long>(
                    "mySelection",
                    message_list_show,
                    StableIdKeyProvider(message_list_show),
                    Items_detail_look_up(message_list_show),
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
                                if(actionMode==null) actionMode = startSupportActionMode(this@group_chat_activity)
                                actionMode?.title = "${selectedPostItems.size}"        // number of selected items to show in toolbar
                            }
                        }
                    }
                })

                // Replay layout right swipe of message
                // for message replay layout
                val message_swipe_controller = MessageSwipeController(this,object: SwipeControllerActions {
                    override fun showReplyUI(position : Int){             // this will give the recycle View item positions number
                        edit_text_view_layout.visibility = View.GONE
                        SELECTED_REPLAY_MESSAGE = position
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
                        if(all_group_chats[position].category=="g_i"){

                        }
                        if(all_group_chats[position].category=="storing_reaction"){

                        }
                        if(all_group_chats[position].category=="voting_template"){

                        }
                        if(all_group_chats[position].category=="sticker"){

                        }
                    }
                })
                val itemTouchHelper = ItemTouchHelper(message_swipe_controller)
                itemTouchHelper.attachToRecyclerView(message_list_show)


                // for the swioing functions for swiping message left side for reedit the message
                // only text messages are editable not other template types of messages or messages
                val edit_message = swipe_to_edit(this,object : SwipeControllerActions{
                    override fun showReplyUI(position: Int){
                        replying_layout_id.visibility = View.GONE
                        if(all_group_chats[position].category=="g_chat"){

                            if(all_group_chats[position].replied_msg=="yes"){
                                val text_str = all_group_chats[position].data
                                val store_ = mapper.readValue<replay_data_model>(text_str)
                                 edit_text_view.text = store_.reply_message  // showing the message before editing it
                                 edit_message_show()                         // calling the functions for edit live edit text
                                 input_text.setText(store_.reply_message)     // displaying the message in the edit text so that user will know what text to edit from before
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
            }
        })
        thread.start()

        // for all chat store of group messages
        input_text = findViewById<EditText>(R.id.txtMessage)

        // for showing the all information of group
        toolbar_.setOnClickListener{
            val intent = Intent(this,groups_info::class.java)
            intent.putExtra("+group_name",group_name)
            intent.putExtra("+group_number",group_number)
            startActivity(intent)
        }

        send_button.setOnClickListener{
            TOTAL_MESSAGE++
            var replay_status = "none"
            val time_ = "${System.currentTimeMillis()}"
            var msg_data = input_text.text.toString()
            val read_people = HashMap<Int,String>()                      // this actually stores the persons number and time of message seen
            val read_str = mapper.writeValueAsString(read_people)        // as this message is sended by you so this have to store the all seen people
            if(SELECTED_REPLAY_MESSAGE!=-1){
                val replay_ = replay_data_model(SELECTED_REPLAY_TEXT,"$SELECTED_REPLAY_MESSAGE",msg_data)
                msg_data = mapper.writeValueAsString(replay_)
                replay_status = "yes"
            }
            val send_data = group_message_model("","","",msg_data,"g_chat",read_str,"",
                                     "none",time_, false,false,"none","none", MY_NUMBER,replay_status,"none")

            all_group_chats.add(send_data)
            initiate_message_me(send_data)
            input_text.setText("")
            val DB = universal_chat_store(this,null)
            val thread = Thread({                // save message to table database
                DB.save_group_message(group_name,group_number,"$TOTAL_MESSAGE",msg_data,"g_chat",read_str,"",
                                      "none",time_, false,false,"none","none", MY_NUMBER,replay_status,"none")
            })
            thread.start()
            hideReplayLayout()
        }

        val attachment : ImageView = findViewById(R.id.options_file_choose_id_)
        attachment.setOnClickListener{
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

       //  cancelling the edit text layout
       cancel_edit_text.setOnClickListener {
           hideReplayLayout()
           input_text.setText("")
       }
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
        SELECTED_REPLAY_MESSAGE = -1
        SELECTED_REPLAY_TEXT = ""
    }

    fun initiate_message_me(_data : group_message_model){
        adapter.setData(_data)
        message_list_show.scrollToPosition(adapter.itemCount-1)    // for viewing the last message that is sended
    }

    //  setOnClickListener of message
    // this is responsible for double click listener of message for message info
    override fun onContactClickListener(position: Int){

        var _used = 0
        if(first_click!=0L){
            val now_ : Long = System.currentTimeMillis()
            if(_used==0) {
                if (now_ - first_click <= DELTA_TIME_DOUBLE_CLICK) {
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

    //  #######################################  presshold section ####################
    // allowing the menu items which one is to visible when
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let{
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.presshold_menu,menu)

            if(PRIVATE_CHAT ==true){                        // this controlls what menu should I show during presshold
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data : Intent?){

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
            val send_data = ArrayList<group_message_model>()
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
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: original")
                }
                image_2.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_2)
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT.toString())
                    startActivity(anotherIntent)
                    Log.d("", "vvvvvvviewing the full image: compressed_2")
                }
                image_3.setOnClickListener{
                    val anotherIntent = Intent(this, FullScreenImageActivity::class.java)
                    anotherIntent.putExtra("+image", uri_3)
                    anotherIntent.putExtra("+private_chat", PRIVATE_CHAT.toString())
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

                        val data_ : image_data_model = image_data_model(PICK_IMAGE,"","")
                        val data_str = mapper.writeValueAsString(data_)
                        var read_store = HashMap<Int,String>()
                        var delivered_ = HashMap<Int,String>()
                        val read_ : String = mapper.writeValueAsString(read_store)
                        val delivered_str = mapper.writeValueAsString(delivered_)

                        sheet_.dismiss()
                        send_data.add(
                           group_message_model(group_name,group_number,message_number,data_str,"g_i",read_,delivered_str,"none",
                               time_,false,false,"none","none", MY_NUMBER,"none","none")
                        )
                        for(i in send_data)initiate_message_me(i)

                        TOTAL_SELECT_IMAGE = 0                                   // renewing the number otherwise it will count the previous selected message
                        val DB = universal_chat_store(this, null)
                        val thread : Thread = Thread({
                            DB.save_group_message(group_name,group_number,message_number,data_str,"g_i",read_,delivered_str,"none",
                                time_,false,false,"none","none", MY_NUMBER,"none","none")
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
        intent.action = Intent.ACTION_GET_CONTENT                   //  this one allow to pick multiple image from gallery
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),IMAGE_CHOOSE)

    }
    //##########################################################

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
        var store = group_message_model(group_name,group_number,"","","",read_str,delivered_str,"none","",false,false,"yes","none",MY_NUMBER,"none","none")

        var _message_: String = ""


        // live text change of the template
        topic!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(current_text : CharSequence?, p1: Int, p2: Int, p3: Int) {
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

            if(text_.isNotEmpty()) {
                if(TEMPLATE_NUMBER == "storing_reaction"){
                    var comment_store = ArrayList<group_message_model>()
                    var reaction_store = reaction_store_group_model(text_,false,0,comment_store)
                    _message_ = mapper.writeValueAsString(reaction_store)

                    store.data = _message_           // saving all message properties to the store
                    store.time_ = _time
                    store.category = "storing_reaction"
                    val thread : Thread = Thread({
                      db.save_group_message(group_name,group_number,"$TOTAL_MESSAGE",_message_,"storing_reaction",read_str,delivered_str,"none",_time,false,false,"yes","none",MY_NUMBER,"none","none")
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
                    val thread: Thread = Thread({
                        db.save_group_message(group_name,group_number,"$TOTAL_MESSAGE",_message_,"voting",read_str,delivered_str,"none",_time,false,false,"yes","none",MY_NUMBER,"none","none")
                    })
                    thread.start()
                    db.close()
                }
                initiate_message_me(store)
                sheet.cancel()
            }
        }
    }


    //################################## functions for editable text messages ##############
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
}