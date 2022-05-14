package com.example.database_part_3.message_holder
/*
   this takes the messages from the EditText bar
   created on 1/3/2022 by Nitish Kr Boro
*/

import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.forward.chips_formation
import com.example.database_part_3.model.*
import com.example.database_part_3.user_info.ParentRecyclerViewAdapter
import com.example.database_part_3.user_info.screen_activity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chips_view.*
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.android.synthetic.main.image_show.*
import kotlinx.android.synthetic.main.network_testing.*
import kotlinx.android.synthetic.main.other_message.*
import kotlinx.android.synthetic.main.presshold_selection_me.*
import kotlinx.android.synthetic.main.select_template_option.*
import okhttp3.internal.notify
import java.util.*
import kotlin.collections.ArrayList

const val MY_NUMBER_LONG : Long = 6900529357
var TOTAL_SELECT_IMAGE = 0
var WALLPAPER_URI = ""

class chat_activity : AppCompatActivity() , ActionMode.Callback {
    var SELECTED_REPLAY_MESSAGE : Int = 0       // this stores the selected message number for replay
    private lateinit var adapter : DataAdapter
    private lateinit var edit_text : EditText
    private var TOTAL_MESSAGE = 0
    private var tracker : SelectionTracker<Long>? = null
    private lateinit var _tool : Toolbar
    private var opposite_number : Long = 0
    private var opposite_person_name = ""
    val context_ : Context = this
    var selectedPostItems :  MutableList<Long> = mutableListOf()
    val selected_store : ArrayList<Long> = ArrayList<Long>()
    private var actionMode : ActionMode? = null
    private var FIRST_ID = Menu.FIRST
    private var all_chats_store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
    private var PAIR_ = ""
    private lateinit var replay_of_message : TextView
    private lateinit var template_select : ImageView
    private var OPPOSITE_PERSON_NUMBER : String = ""
    private val mapper = jacksonObjectMapper()
    public val GALLERY_INTENT_CALLED = 1
    public val GALLERY_KITKAT_INTENT_CALLED = 2
    private var PRIVATE_CHAT : Boolean = false
    private var cancellationSignal : CancellationSignal ? = null
//    private val authenticationCallback : BiometricPrompt.AuthenticationCallback


    // for the selected items
    var TOTAL_ME = 0
    var TOTAL_OTHERS = 0
    var TOTAL_REACTION_TAMPLATE = 0
    var TOTAL_VOTER_TEMPLATE = 0

    var FIRST_POSITION = 0
    var LAST_POSITION = 0

    // for biometric lock
    val authenticationCallback : BiometricPrompt.AuthenticationCallback
        get() =
            @RequiresApi(Build.VERSION_CODES.P)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence?
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    notifyUser("Authentication error : $errString ")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    notifyUser("Authentication success!")
//                    startActivity(Intent(this@chat_activity,Sec))
                }
            }

    fun notifyUser(str : String){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()
    }
     private fun checkBiometricSupport() : Boolean{
         val keyguardManager : KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
         if(!keyguardManager.isKeyguardSecure){
             notifyUser("FingurPrint authentication has not been enabled in this settings")
             return false
         }
         return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
             true
         }  else true
     }
    private fun getCancel() : CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Authentication was canceled!")
        }
        return  cancellationSignal as CancellationSignal
    }

    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

    // now to seting for the initializations of toolbars
        val toolbar_ : Toolbar = findViewById(R.id.tool_bar_chat_screen)
        setSupportActionBar(toolbar_)
        toolbar_.showOverflowMenu()
    // if the above initializations if not given then the toolbars functions for menu will not work

        val _name : String = intent.getStringExtra("+name").toString()
        val _number : String = intent.getStringExtra("+number").toString()    // this means assured that if it is convertable to Long than only convert it
        OPPOSITE_PERSON_NUMBER = _number

        template_select = findViewById(R.id.select_tamplate_id)    // template selections id

        opposite_number = _number.toLong()

        val layout_manager = LinearLayoutManager(this)
        messageList_recycle.layoutManager = layout_manager
        adapter = DataAdapter(this)
        messageList_recycle.adapter = adapter


        // Id for replay of message
        replay_of_message = findViewById<TextView>(R.id.txtQuotedMsg)

        //  calling the existing message history from the database
        var pair_: String = ""

        if (MY_NUMBER_LONG > _number.toLong()) pair_ = "${MY_NUMBER_LONG}"+"|"+"${_number}"
        if (MY_NUMBER_LONG < _number.toLong()) pair_ = "${_number}"+"|"+"${MY_NUMBER_LONG}"
        PAIR_ = pair_
        val handler: Handler = Handler()         // must be declare in under the main thread
        val DB = universal_chat_store(this,null)                // this is for universal chat store database
        var persons_info = persons_info_last_msg("",false,true)

        // database is giving complete informations of every chat with every property
        val thread_ : Thread = Thread({
            val time_1 = System.currentTimeMillis()
            all_chats_store = DB.get_messages(pair_,"")
            persons_info = DB.get_persons_info(_number)
            TOTAL_MESSAGE = all_chats_store.size

            if (all_chats_store.size > 0){
                var first_message : String = ""

                handler.post({
                    Toast.makeText(this,"the size is: ${first_message}",Toast.LENGTH_SHORT).show()
                    initiate_process_me(all_chats_store)
                    // if the chats become private then privent the sceenshot
                    if(persons_info.private_chat==true)
                    {
                        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE)

                        checkBiometricSupport()
                        val biometricPrompt : BiometricPrompt = BiometricPrompt.Builder(this)
                            .setTitle("Biometric Authentication")
                            .setSubtitle("Authentication required")
                            .setNegativeButton("Cancel",this.mainExecutor,DialogInterface.OnClickListener{ dialog, which ->
                                notifyUser("Authentication cancled")
                            })
                            .build()
                        biometricPrompt.authenticate(getCancel(),mainExecutor,authenticationCallback)

                        Toast.makeText(this,"you cannot take th escreen shoot",Toast.LENGTH_SHORT).show()
                    }

                    // removing the lock butoon when pair chat is not private
                    if(persons_info.private_chat==false){
                        lock_pair_chat_id.visibility = View.GONE
                    }
                })
            }

            val time_2 =  System.currentTimeMillis()
            // it takes some 14ms
            Log.d("","nnnnnnnnnnnet time taken to bring data from the data base:${time_2 - time_1}")
            DB.close()
        })
        thread_.start()

        val text_name : TextView = findViewById(R.id.pair_message_person_name)
        text_name.setText("${_name}")

        // for tracker of long pressed of message
        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            messageList_recycle,
            StableIdKeyProvider(messageList_recycle),
            MyItemDetailsLookup(messageList_recycle),
            StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
            adapter.tracker = tracker


        // making the tracker for selections
        tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>(){
            override fun onSelectionChanged(){
            super.onSelectionChanged()

            tracker.let {
            selectedPostItems = it!!.selection.toMutableList()
            Log.d("@@@@@@@@selectedPosition:","${selectedPostItems}")
            if(selectedPostItems.isEmpty()){    // if nothing is selected
                 actionMode?.finish()
            } else{                             // if some items of messages are selected
                if(actionMode==null) actionMode= startSupportActionMode(this@chat_activity)
                actionMode?.title = "${selectedPostItems.size}"        // number of selected items to show in toolbar
            }
          }
        }
    })

    /* After sending button pressed */
    val _text : EditText = findViewById(R.id.txtMessage)
    edit_text = _text
    btnsend.setOnClickListener {
            val store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
            var _time_: String = "${Calendar.getInstance().timeInMillis}"   // set this time when message is sended
            val edit_msg : Boolean = false                                   // whenever the msg is edited update this
            val _message_: String = _text.text.toString()
            // save to database universal store
            TOTAL_MESSAGE++
            var message_number: String = ""
            var pair: String = ""

            if (MY_NUMBER_LONG > _number.toLong()) {
                message_number = "$TOTAL_MESSAGE"
                pair = "${MY_NUMBER_LONG}"+"|"+"${_number}"
            }
            if (MY_NUMBER_LONG < _number.toLong()) {
                message_number = "${TOTAL_MESSAGE}"
                pair = "${_number}"+"|"+"${MY_NUMBER_LONG}"
            }

            var _replay_status = ""
            if(SELECTED_REPLAY_MESSAGE==0) _replay_status ="no"
            if(SELECTED_REPLAY_MESSAGE!=0) _replay_status = "${SELECTED_REPLAY_MESSAGE}"

            store.add(universal_model.one_chat_property(pair,_name,message_number,_message_,"m",true,"no","${_time}",false,false,"none",
                 "none",false,MY_NUMBER_LONG,_number.toLong(),_replay_status,"none"))
            val handler: Handler = Handler()
            val db = universal_chat_store(this, null)
            val thread : Thread = Thread({
            val index_of_message : Boolean = db.save_message(pair, message_number, _message_, "m", true, "not", _time_, "none",
                                                            false, 6900529357, _number,_replay_status,"no",false,edit_msg,"none",_name)
            })

            all_chats_store.add(universal_model.one_chat_property(pair,_name,message_number,_message_,"m",true,"no","${_time}",false,false,"none",
                "none",false,MY_NUMBER_LONG,_number.toLong(),_replay_status,"none"))
            initiate_process_me(store)   // this store is onbe chat property for the evaluations of dataadapter
            set_clear()
            hideReplayLayout()           // this is to make close of selected to replay layout
            thread.start()
    }

    cancelButton.setOnClickListener {      //  when replay layout cacel button is pressed to close
       hideReplayLayout()
    }

    // selecting for the tamplate id
    select_tamplate_id.setOnClickListener {
            
        }

    // for accessing userInformations when clicked above the content bar
    tool_bar_chat_screen.setOnClickListener{     // this will navigate towards the user info
            val intent : Intent = Intent(this,screen_activity::class.java)
            intent.putExtra("_name_",_name)     // for passing name
            intent.putExtra("_number_",_number)   // for passing number

            startActivity(intent)
            Log.d(">>>clicked chat_Activity","ssdd")
      }

    // for giving options to choose files and photos and vedios
    val attachment : ImageView = findViewById(R.id.options_file_choose_id)
    attachment.setOnClickListener{
        show_bottom_dialog()
    }

    // for timer chat
    val timer_msg : ImageView = findViewById(R.id.timer_chat_icon_id)
    timer_msg.setOnClickListener{
       if(_text.text.isNotEmpty()){
              val sheet: BottomSheetDialog = BottomSheetDialog(this)
              sheet.setContentView(R.layout.timer_chat_layout)
              val time : EditText = sheet.findViewById(R.id.timer_chat_set_time)!!

              /* save this time to the timer sheet */
              sheet.show()
       }else{
            Toast.makeText(this,"Please enter the message",Toast.LENGTH_SHORT).show()
       }
    }

    // for accessing past media sharing
    val pp : ImageView = findViewById(R.id.between_media_access)
    pp.setOnClickListener {
       show_the_past_media()
    }
        
     // for message replay layout
     val message_swipe_controller = MessageSwipeController(this,object:SwipeControllerActions{
     override fun showReplyUI(position : Int){             // this will give the recycle View item positions number
         SELECTED_REPLAY_MESSAGE = position
         txtQuotedMsg.setText("${all_chats_store[position].data}")
         showQuotedMessage(all_chats_store[position])
     }
     })
     val itemTouchHelper = ItemTouchHelper(message_swipe_controller)
     itemTouchHelper.attachToRecyclerView(messageList_recycle)

     // template selections voter and reactions store
     template_select.setOnClickListener {
         selection_template()
     }

     // for viewing the url of wallpaper
     Toast.makeText(this,"URI of wallpaper : ${WALLPAPER_URI}",Toast.LENGTH_SHORT).show()
        if(WALLPAPER_URI!=""){
            val input_stream = applicationContext.contentResolver.openInputStream(WALLPAPER_URI.toUri())
            val drawable_ = Drawable.createFromStream(input_stream, WALLPAPER_URI)
            messageList_recycle.background = drawable_
        }
    }    // last of onCreate functions


    override fun onBackPressed(){
        if(selectedPostItems.size!=0)selectedPostItems.clear()          // this will disellect all the selected items
         else super.onBackPressed()
    }

    private fun set_clear(){
        val _text : EditText = findViewById(R.id.txtMessage)
        _text.setText("")   // clearing the message input space

        //  hiding the keyboard into its place
        //  val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //  inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun initiate_process_me(data : ArrayList<universal_model.one_chat_property>){         // this will take all the type of message and will
        SELECTED_REPLAY_MESSAGE = 0
        adapter.setData(data)
        messageList_recycle.scrollToPosition(adapter.itemCount-1)    // viewing on the screen the last message
   }

    private val IMAGE_CHOOSE = 1000
    private val PERMISSION_CODE = 1001
    private val SELECT_VIDEO = 1000
    private val SELECT_FILES = 1000

    private fun show_bottom_dialog(){    // The options will come for choosing files and photos
        val sheet : BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.attachment_of_files_option)

        val photos : RelativeLayout = sheet.findViewById(R.id.choose_photo_linear_layout)!!
        val videos : RelativeLayout=sheet.findViewById(R.id.choose_videos_layout_id)!!
        val _files : RelativeLayout = sheet.findViewById(R.id.choose_files_linear_id)!!
        val _contacts : RelativeLayout = sheet.findViewById(R.id.Choose_contact_attachment)!!


    photos.setOnClickListener{  // when user click to photos options
    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
    if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
         val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
         requestPermissions(permission,PERMISSION_CODE)
        }
       else{  choose_image_galary() }
    }
   else{
       choose_image_galary()
     }
   }

        videos.setOnClickListener{   // when user click to videos options
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_VIDEO)
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

   private fun choose_image_galary(){    // this one will pick all the images
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action = Intent.ACTION_GET_CONTENT                   //  this one allow to pick multiple image from gallery
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),IMAGE_CHOOSE)

//        val KITKAT_VALUE = 1002
//        val intent: Intent
//        if (Build.VERSION.SDK_INT < 19) {
//            intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/*"
//            startActivityForResult(intent, KITKAT_VALUE)
//        } else {
//            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "image/*"
//            startActivityForResult(intent, KITKAT_VALUE)
//        }

   }

    // for selections image and sending image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data : Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode ==Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
            TOTAL_MESSAGE++
            var message_number : String = "${TOTAL_MESSAGE}"
//            TOTAL_SELECT_IMAGE = total_image                                       // updating the total selected image
            val send_data: ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()

            Log.d("dddddddddata","is :${data}")
            Log.d("Extracted path is:","${data!!.data}")

            Toast.makeText(this,"Extracted path is data.data:${data.data}",Toast.LENGTH_LONG).show()

            if(data.clipData==null){
                Toast.makeText(this,"You didnot selected any media",Toast.LENGTH_SHORT).show()
            }

            if(data.data!=null){    // this is for selecting single photo from gallery

             send_data.add(universal_model.one_chat_property(
                        PAIR_,
                        opposite_person_name,
                        message_number,
                        "${data.data}",
                        "i",
                        true,
                        "no",
                        "${Calendar.getInstance().time}",
                        false,
                        false,
                        "none",
                        "none",
                        false,
                        MY_NUMBER_LONG,
                        opposite_number,
                        "no",
                        "no"))
                    initiate_process_me(send_data)

                TOTAL_SELECT_IMAGE = 0                                   // renewing the number otherwise it will count the previous selected message
                val DB = universal_chat_store(this,null)
                val thread: Thread = Thread({
                  DB.save_message(PAIR_, message_number, "${data.data}", "i", true, "no", "3:00pm", "none", false, MY_NUMBER_LONG, opposite_number.toString(), "no", "no", false, false, "none",opposite_person_name)
                })
                thread.start()
            }

            if(data.clipData!=null){
                val total_image = data.clipData!!.itemCount
              Toast.makeText(this,"Total Image you have selected is ${total_image}",Toast.LENGTH_LONG).show()
                Log.d("iiiiiiiiimage","data.clipData:${data.clipData}")
                for (i in 0..total_image-1){
                    val get_uri : ClipData.Item = data.clipData!!.getItemAt(i)
                    Log.d("ggggggget_uri","${get_uri}")
                    Log.d("Llllllink_uri","${get_uri.uri}")
                    send_data.add(universal_model.one_chat_property(
                        PAIR_,
                        opposite_person_name,
                        message_number,
                        "${get_uri.uri}",          // this uri works fine
                        "i",
                        true,
                        "no",
                        "${Calendar.getInstance().time}",
                        false,
                        false,
                        "none",
                        "none",
                        false,
                        MY_NUMBER_LONG,
                        opposite_number,
                        "no",
                        "no"))
                initiate_process_me(send_data)
            }

            TOTAL_SELECT_IMAGE = 0                                   // renewing the number otherwise it will count the previous selected message
            val DB = universal_chat_store(this, null)
            val thread: Thread = Thread({
                for (i in 0..total_image-1){
                    val get_uri : ClipData.Item = data.clipData!!.getItemAt(i)    // this is the must way to extract the URIs of the Images
                    DB.save_message(
                        PAIR_,
                        message_number,
                        "${get_uri.uri}",
                        "i",
                        true,
                        "no",
                        "3:00pm",
                        "none",
                        false,
                        MY_NUMBER_LONG,
                        opposite_number.toString(),
                        "no",
                        "no",
                        false,
                        false,
                        "none",
                        opposite_person_name )
                }
            })
            thread.start()
        }
    }
    }

    private fun show_the_past_media(){
         var parentRecyclerView : RecyclerView? = null
         var ParentAdapter : RecyclerView.Adapter<*>? = null
         var parentModelArrayList : ArrayList<String> = ArrayList()
         var parentLayoutManager: RecyclerView.LayoutManager? = null

        parentModelArrayList.add("PHOTOS")
        parentModelArrayList.add("VEDIOS")
        parentModelArrayList.add("DOCUMENTS")
        parentModelArrayList.add("AUDIOS")
        parentModelArrayList.add("LINKS")

        val sheet: BottomSheetDialog = BottomSheetDialog(this)
        sheet.setContentView(R.layout.below_sheet_info_media)

        parentRecyclerView = sheet.findViewById(R.id.pair_info_recycle_view_id)
        parentRecyclerView!!.setHasFixedSize(true)
        parentLayoutManager = LinearLayoutManager(this)
        ParentAdapter = ParentRecyclerViewAdapter(parentModelArrayList, this)
        parentRecyclerView!!.setLayoutManager(parentLayoutManager)
        parentRecyclerView!!.setAdapter(ParentAdapter)
        ParentAdapter!!.notifyDataSetChanged()
        sheet.show()
    }

    // After long pressed in the message item the menu items will ben shown by this functions
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?) : Boolean {  // this functions controlled the clicked long pressed functions

      when(item?.itemId){
      R.id.delete_function_id -> {
              TOTAL_ME = 0
              TOTAL_OTHERS = 0
              TOTAL_VOTER_TEMPLATE = 0
              TOTAL_REACTION_TAMPLATE = 0

          for(i in selectedPostItems){
              val selection_types : String = adapter.position_to_type(i)
              if(selection_types=="TYPE_ME") TOTAL_ME++
              if(selection_types=="TYPE_OTHER") TOTAL_OTHERS++
              if(selection_types=="VOTING_TAMPLATE")TOTAL_VOTER_TEMPLATE++
              if(selection_types=="REACTION_TAMPLATE")TOTAL_REACTION_TAMPLATE++
          }
            if(TOTAL_ME!=selectedPostItems.size){
                  val mBuilder = AlertDialog.Builder(this)
                  mBuilder.setTitle("Delete ${selectedPostItems.size} Messages for me")
                  mBuilder.setNeutralButton("OK"){ dialog , which->
                      for(i in selectedPostItems){
                          adapter.delete_message(i.toInt())
                      }
                      dialog.dismiss()
                      val db = universal_chat_store(this,null)
                      for(k in selectedPostItems){
                          db.delete_specific_message_number("${PAIR_}|${k+1}")     // because message number in database starts from 1 and here in GUI system starts from 0
                      }
                  }
                  val dd = mBuilder.create()
                  dd.show()
            }
          if(TOTAL_ME==selectedPostItems.size){
                  val list_items = arrayOf("Delete ${selectedPostItems.size} messages for me","Delete ${selectedPostItems.size} messages for everyone")
                  val mBuilder = AlertDialog.Builder(this)
                  mBuilder.setTitle("Delete Messages")
                  mBuilder.setSingleChoiceItems(list_items,1){dialogInterface , i->
                      // delete for both side
                      // from database
                      // from recycle view
                      if(i==0){    // Delete  messages for me
                          for(j in selectedPostItems) {
                              adapter.delete_message(j.toInt())
                          }
                          Toast.makeText(this,"You Deleted ${selectedPostItems.size} messages for you",Toast.LENGTH_LONG).show()
                      }
                      if(i==1){    // Delete messages for everyOne
                          for(j in selectedPostItems) {
                              adapter.delete_message(j.toInt())
                          }
                          Toast.makeText(this,"You Deleted ${selectedPostItems.size} messages for everyOne",Toast.LENGTH_LONG).show()
                      }
                      val db = universal_chat_store(this,null)
                      for(k in selectedPostItems){
                          db.delete_specific_message_number("${PAIR_}|${k+1}")     // because message number in database starts from 1 and here in GUI system starts from 0
                      }
                  }
                  mBuilder.setNeutralButton("OK"){dialog , which->
                      dialog.dismiss()
                  }
                  val dd = mBuilder.create()
                  dd.show()
             }
          }    // this function will activate depending on th esend types of the user in the chat secreesn

      R.id.star_function_id -> {
            var stared_msg_number = 0
            val DB = universal_chat_store(this,null)
            for(i in selectedPostItems){
              if(all_chats_store[i.toInt()].stared==true)stared_msg_number++
            }
            if(selectedPostItems.size==stared_msg_number){   // when all the selected message is stared msg
                val list_items = arrayOf("Unstar all ${selectedPostItems.size} messages for me")
                val mBuilder = AlertDialog.Builder(this)
                mBuilder.setTitle("Unstar messages")
                val msg_number : ArrayList<String> = ArrayList<String>()
                for(i in selectedPostItems){
                    msg_number.add("${i+1}")                               // as the message number of the data base is one number ahead so add before retriving data
                }
                mBuilder.setSingleChoiceItems(list_items,1){ dialogInterface,i->
                    // dialog for unstar message
                    // change layout of recycle
                    val thread : Thread = Thread({
                        DB.update_one_chat_property("star",PAIR_, msg_number, "false")            // updating in the local database
                        for(j in selectedPostItems){
                            all_chats_store[j.toInt()].stared = false                                       // updating in the RAM
                        }
                    })
                    thread.start()
                    Toast.makeText(this,"You unstar all ${selectedPostItems.size} messages",Toast.LENGTH_LONG).show()
                    dialogInterface.dismiss()
                }
                mBuilder.setNeutralButton("Cancel"){ dialog , which->
                    dialog.cancel()
                }
                val dd = mBuilder.create()
                dd.show()
            }
            if(selectedPostItems.size!=stared_msg_number){   // when all the selected message is stared msg
                val list_items = arrayOf("Star all ${selectedPostItems.size} messages for me")
                val mBuilder = AlertDialog.Builder(this)
                mBuilder.setTitle("Star this messages")
                val msg_number : ArrayList<String> = ArrayList<String>()
                for(i in selectedPostItems){
                    msg_number.add("${i+1}")
                }
                mBuilder.setSingleChoiceItems(list_items,0) { dialogInterface , i ->
                    // dialog for star all message
                    // change layout of recycle
                    val thread : Thread = Thread({
                        DB.update_one_chat_property("star",PAIR_, msg_number, "true")        // updating in the local database
                        for(j in selectedPostItems){
                            all_chats_store[j.toInt()].stared = true                                   // updating in the RAM
                        }
                    })
                    thread.start()
                    Toast.makeText(this,"Stared messages moved to star folder",Toast.LENGTH_LONG).show()
                    dialogInterface.dismiss()
                }
                mBuilder.setNeutralButton("Cancel") { dialog , which->
                    dialog.cancel()
                    dialog.dismiss()
                }
                val dd = mBuilder.create()
                dd.show()
            }
          }

      R.id.forward_msg_id -> {        // this is used fro only forwarding mesages
          Toast.makeText(this,"Forward msg menu activate!!!",Toast.LENGTH_SHORT).show()

          var message_to_forward : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
          for( k in selectedPostItems){                             // this is for selected positions in the messageList
              message_to_forward.add(all_chats_store[k.toInt()])    // inserting all the messages to forward
          }

          val intent : Intent = Intent(this,chips_formation::class.java)
          val mapper = jacksonObjectMapper()
          val str : String = mapper.writeValueAsString(message_to_forward)
          intent.putExtra("messages",str)
          startActivity(intent)

    }

      R.id.copy_msg_id -> {
          TOTAL_VOTER_TEMPLATE = 0
          TOTAL_REACTION_TAMPLATE = 0
          var TOTAL_DOCUMENT_PHOTO_VEDIO = 0
          var LOCK_:Int = 0
          // copying the text that have selecte
          var index = ""
          for(i in selectedPostItems){
                val selection_types : String = adapter.position_to_type(i)
                if(selection_types=="VOTING_TAMPLATE")TOTAL_VOTER_TEMPLATE++
                if(selection_types=="REACTION_TAMPLATE")TOTAL_REACTION_TAMPLATE++
                if(selection_types=="IMAGE_OR_VEDIO_OR_DOCUMENT") TOTAL_DOCUMENT_PHOTO_VEDIO++

               if(all_chats_store[i.toInt()].category=="m") {
                   index = index + "${all_chats_store[i.toInt()].data} : "
               }
               if(all_chats_store[i.toInt()].lock==true) LOCK_++                //  this means user locked screenshoot
          }

         if(LOCK_==0) {
             if (TOTAL_REACTION_TAMPLATE == 0 && TOTAL_VOTER_TEMPLATE == 0 && TOTAL_DOCUMENT_PHOTO_VEDIO == 0) {       // this is text messages that can copied
                 val clipboard =
                     getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                 val clip: ClipData = ClipData.newPlainText("EditText", index)
                 clipboard.setPrimaryClip(clip)
                 Toast.makeText(this, "Messages are copied", Toast.LENGTH_SHORT).show()
             } else {          // this type of messages cannot be copied
                 Toast.makeText(this, "This type of messages cannot be copied!", Toast.LENGTH_SHORT)
                     .show()
             }
         }
         if(LOCK_!=0){
            Toast.makeText(this,"The user has locked one of your selected message , so you cannot copy it nor screenshoot it",Toast.LENGTH_LONG).show()
         }
    }

//    R.id.remainder_chat_id->{
//              var remainder_chat_msg = 0
//              for(i in selectedPostItems){    // now here i is the the index of selectedPositions
//                  if(all_chats_store[i.toInt()].remainder!="none")remainder_chat_msg++
//              }
//            val msg_numbers : ArrayList<String> = ArrayList<String>()
//            for(i in selectedPostItems){
//                msg_numbers.add("${PAIR_}|${i+1}")           // as the message number of the data base is one number ahead so add before retriving data
//            }
//            val DB = universal_chat_store(context_,null)
//
//            if(selectedPostItems.size==remainder_chat_msg){   // when all the selected message is remaindered msg
//
//            // make an alert dialog to remove the alarm time from database
//
//            val thread : Thread = Thread({
//                DB.update_one_chat_property("remainder", msg_numbers, "none")     // removing the alarm time from database
//              })
//            }
//            if(selectedPostItems.size!=remainder_chat_msg){   // when all selected messages are not remaindered and setting new remaindered
//                val sheet: BottomSheetDialog = BottomSheetDialog(this)
//                sheet.setContentView(R.layout.new_alarm_setting)
//
//                val time_picker = findViewById<TimePicker>(R.id.time_picker)
//                time_picker.setOnTimeChangedListener { _, hours,minute ->
//
//                }
//
//                sheet.show()
//            }
//        }

    }
    return true
    }

    //  this one is responsible for showing the each functions icon of long pressed
    // in this functions we all controll the menu icon features
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let {
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.presshold_menu,menu)
            return true
        }
        if(PRIVATE_CHAT==true){      // if it is a secret chat then make invisible some of the items
            val copy_ : MenuItem = menu!!.findItem(R.id.copy_msg_id)
            val forward : MenuItem = menu.findItem(R.id.forward_msg_id)
            copy_.setVisible(false)
            forward.setVisible(false)
        }
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode : ActionMode?){
        adapter.tracker?.clearSelection()
//        adapter.notifyDataSetChanged()
        actionMode = null
    }

    private fun set_options_for_remainder(msg_numbers : ArrayList<String> , hours_ : String , minute : String , _indi : String){

    }

    private fun hideReplayLayout(){                      // this functions hide the replay layout for if not required
        reply_layout.visibility = View.GONE
        SELECTED_REPLAY_MESSAGE = 0
    }                                                   //Swiping right function of message

    private fun showQuotedMessage(message : universal_model.one_chat_property){
        Toast.makeText(this,"The replay text is:${message.data}",Toast.LENGTH_SHORT).show()
        edit_text.requestFocus()
        replay_of_message.setText("${message.data}")
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(edit_text,InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
    }

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

        var template_data : template_selection_ ? = null
        var TEMPLATE_NUMBER = ""

        _reaction!!.setOnClickListener {
            TEMPLATE_NUMBER = "storing_reaction"
            _show_selection!!.setText("Enter Topic For '${TEMPLATE_NUMBER}' template")
        }
        _vote!!.setOnClickListener {
          TEMPLATE_NUMBER = "voting"
            _show_selection!!.setText("Enter Topic For '${TEMPLATE_NUMBER}' template")
        }

        val store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
        var _time : String = "${Calendar.getInstance().timeInMillis}"   // set this time when message is sended
        var _message_: String = ""
        // save to database universal store
        TOTAL_MESSAGE++
        var message_number: String = ""
        var pair : String = ""

        if(MY_NUMBER_LONG > OPPOSITE_PERSON_NUMBER.toLong()){
            message_number = "${TOTAL_MESSAGE}"
            pair = "${MY_NUMBER_LONG}"+"|"+OPPOSITE_PERSON_NUMBER
        }
        if(MY_NUMBER_LONG < OPPOSITE_PERSON_NUMBER.toLong()){
            message_number ="${TOTAL_MESSAGE}"
            pair = OPPOSITE_PERSON_NUMBER + "|" + "${MY_NUMBER_LONG}"
        }

        var _replay_status = ""
        if(SELECTED_REPLAY_MESSAGE==0) _replay_status ="no"
        if(SELECTED_REPLAY_MESSAGE!=0) _replay_status = "${SELECTED_REPLAY_MESSAGE}"

//        all_chats_store.add(universal_model.one_chat_property(message_number,_message_,"m",true,"no",_time,false,false,"storing_reaction",
//            "none",false,MY_NUMBER_LONG,OPPOSITE_PERSON_NUMBER.toLong(),_replay_status,"none"))     // update message in Ram

        val db = universal_chat_store(this,null)
        val handler : Handler = Handler()
        val thread : Thread = Thread({
            db.save_message(pair, message_number, _message_, "t", true, "not", _time, "none", false, 6900529357, OPPOSITE_PERSON_NUMBER,_replay_status,"no",false,false,TEMPLATE_NUMBER,opposite_person_name)
            handler.post({
                sheet.cancel()
            })
        })

        // after clicking the post button
        _post!!.setOnClickListener{
            val text_ = topic!!.text.toString()
            if(text_.isEmpty()){
                Toast.makeText(this,"please enter the Topic",Toast.LENGTH_SHORT).show()
            }
            if(text_.isNotEmpty()){
                if(TEMPLATE_NUMBER=="storing_reaction"){
                    val arr : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
                    var reaction_tem : reaction_store_model = reaction_store_model(text_,false,0,arr)
                    _message_ = mapper.writeValueAsString(reaction_tem)
                    Log.d("sssssssstringify","of reaction store model ${_message_}")
                    store.add(universal_model.one_chat_property(PAIR_,opposite_person_name,message_number,_message_,"t",true,"no",_time,false,false,"storing_reaction",
                        "none",false,MY_NUMBER_LONG,OPPOSITE_PERSON_NUMBER.toLong(),_replay_status,"none"))
                }
                if(TEMPLATE_NUMBER=="voting"){
                    var voting_temp : voting_template = voting_template(text_,0,0,0)
                    _message_ = mapper.writeValueAsString(voting_temp)
                    store.add(universal_model.one_chat_property(PAIR_,opposite_person_name,message_number,_message_,"v",true,"no",_time,false,false,"voting",
                        "none",false,MY_NUMBER_LONG,OPPOSITE_PERSON_NUMBER.toLong(),_replay_status,"none"))
                }
                initiate_process_me(store)
            }
            thread.start()
        }
    }
}
