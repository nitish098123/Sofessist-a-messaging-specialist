package com.example.database_part_3.message_holder
/*
   this takes the messages from the EditText bar
   created on 1/3/2022 by Nitish Kr Boro
*/

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.alarm.MyAlarm
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.model.universal_model
import com.example.database_part_3.model.universal_model.Me_user
import com.example.database_part_3.user_info.ParentRecyclerViewAdapter
import com.example.database_part_3.user_info.screen_activity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.home_page.*
import kotlinx.android.synthetic.main.image_show.*
import kotlinx.android.synthetic.main.other_message.*
import kotlinx.android.synthetic.main.presshold_selection_me.*
import java.util.*
import kotlin.collections.ArrayList

const val MY_NUMBER_LONG : Long = 6900529357
var TOTAL_SELECT_IMAGE = 0
class chat_activity : AppCompatActivity() , ActionMode.Callback {

    var SELECTED_REPLAY_MESSAGE : Int = 0       // this stores the selected message number for replay
    private lateinit var adapter : DataAdapter
    private lateinit var edit_text : EditText
    private var TOTAL_MESSAGE = 0
    private var tracker : SelectionTracker<Long>? = null
    private lateinit var _tool : Toolbar
    private var opposite_number : Long = 0
    val context_ : Context = this
    var selectedPostItems :  MutableList<Long> = mutableListOf()
    val selected_store : ArrayList<Long> = ArrayList<Long>()
    private var actionMode : ActionMode? = null
    private var FIRST_ID = Menu.FIRST
    private var all_chats_store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
    private var PAIR_ = ""
    private lateinit var replay_of_message : TextView
    // for the selected items
    var TOTAL_ME = 0
    var TOTAL_OTHERS = 0
    var TOTAL_REACTION_TAMPLATE = 0
    var TOTAL_VOTER_TEMPLATE = 0

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

        opposite_number = _number.toLong()
        messageList_recycle.layoutManager = LinearLayoutManager(this)
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

        // database is giving complete informations of every chat with every property
        val thread_ : Thread = Thread({

            all_chats_store = DB.get_messages(pair_)

            TOTAL_MESSAGE = all_chats_store.size

            if (all_chats_store.size > 0){
                val tt = all_chats_store.size
                var first_message : String = ""
                var j=0
                handler.post({
                    Toast.makeText(this,"the size is: ${first_message}",Toast.LENGTH_SHORT).show()
                    initiate_process_me(all_chats_store)
                })
            }
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

      tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
              override fun onSelectionChanged() {
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
                message_number = "${MY_NUMBER_LONG}"+"|"+"${_number}"+"|"+"${TOTAL_MESSAGE}"
                pair = "${MY_NUMBER_LONG}"+"|"+"${_number}"
            }
            if (MY_NUMBER_LONG < _number.toLong()) {
                message_number = "${_number}"+"|"+"${MY_NUMBER_LONG}"+"|"+"${TOTAL_MESSAGE}"
                pair = "${_number}"+"|"+"${MY_NUMBER_LONG}"
            }

            var _replay_status = ""
            if(SELECTED_REPLAY_MESSAGE==0) _replay_status ="no"
            if(SELECTED_REPLAY_MESSAGE!=0) _replay_status = "${SELECTED_REPLAY_MESSAGE}"
            store.add(universal_model.one_chat_property(message_number,_message_,"m",true,"no","${_time}",false,false,"none",
                 "none",false,MY_NUMBER_LONG,_number.toLong(),_replay_status,"none"))
            val handler: Handler = Handler()
            val db = universal_chat_store(this, null)

            val thread : Thread = Thread({
                val index_of_message : Boolean = db.save_message(pair, message_number, _message_, "m", true, "not", _time_, "none", false, 6900529357, _number,_replay_status,"no",false,edit_msg,"none")

                db.update_user_settings("last_msg_arrived",_message_,_number)      // last_message_arrived for one pair of message

                if(TOTAL_MESSAGE==1){     //  this means this person is talking for the first time
                    db.save_person_info(_name,_number,"","","",false,false,"","","","","","",
                    false,false,0,"","")    // this is first time to save this user in database
                }

                handler.post({
                    if (index_of_message == true) Toast.makeText(this, "data saved!", Toast.LENGTH_SHORT ).show()
                    if (index_of_message == false) Toast.makeText(this, "Error in saving data!",Toast.LENGTH_SHORT).show()
                })
            })

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

}    // last of onCreate functions


    override fun onBackPressed() {
        if(selectedPostItems.size!=0)selectedPostItems.clear()          // this will disellect all the selected items
         else super.onBackPressed()
        Log.d("@@@@@@@@diselection"," occurs!!!")
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
        } else{  choose_image_galary() }
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
   }

    // for selections image and sending image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode ==Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
            TOTAL_MESSAGE++
            var message_number: String = "${PAIR_}|${TOTAL_MESSAGE}"
            Log.d("########total uri is :","${data}")

            if(data?.clipData==null){
                Log.d("%%%%%%%% didnot","selected any media")
                Toast.makeText(this,"You didnot selected any media",Toast.LENGTH_SHORT).show()
            }

//          if (data?.clipData!!.itemCount!=0)
            if(data?.clipData!=null){
            val total_image : Int = data?.clipData!!.itemCount                    // saving the total number of image uri selected
            TOTAL_SELECT_IMAGE = total_image                                       // updating the total selected image
            Log.d("#######selected images","${total_image}")
            var store_uri_string: String = ""                                       // string variable for savign uris to save in sqlite data base
            val send_data: ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()

            for (i in 0 until total_image) {
//              store_image_uri.add(data?.clipData!!.getItemAt(i).uri)
                store_uri_string =
                    store_uri_string + "|${data?.clipData!!.getItemAt(i).uri}"    // this data will store to the database with separations with |uri|uri|uri
                send_data.add(
                    universal_model.one_chat_property(
                        message_number,
                        "${data?.clipData!!.getItemAt(i).uri}",
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
                DB.save_message(
                    PAIR_,
                    message_number,
                    store_uri_string,
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
                    "none")
            })
            thread.start()
        }
    } }

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

    R.id.delete_function_id ->{
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
                  val list_items = arrayOf("delete all ${selectedPostItems.size} messages for me","delete all ${selectedPostItems.size} messages for everyone")
                  val mBuilder = AlertDialog.Builder(this)
                  mBuilder.setTitle("Delete Messages")
                  mBuilder.setSingleChoiceItems(list_items,1){dialogInterface , i->
                      // delete for yours only
                      // from database
                      // from recycle view
                      Toast.makeText(this,"Clicked to the delete items:${i}",Toast.LENGTH_LONG).show()
                      dialogInterface.dismiss()
                  }
                  mBuilder.setNeutralButton("Cancel"){ dialog , which->
                      dialog.cancel()
                  }
                  val dd = mBuilder.create()
                  dd.show()
            }
              if(TOTAL_ME==selectedPostItems.size){
                  val list_items = arrayOf("delete all ${selectedPostItems.size} messages")
                  val mBuilder = AlertDialog.Builder(this)
                  mBuilder.setTitle("Delete Messages")
                  mBuilder.setSingleChoiceItems(list_items,1){dialogInterface , i->
                      // delete for both side
                      // from database
                      // from recycle view
                      Toast.makeText(this,"You Deleted ${selectedPostItems.size} messages for you",Toast.LENGTH_LONG).show()
                  }
                  mBuilder.setNeutralButton("Cancel"){ dialog , which->
                      dialog.cancel()
                  }
                  val dd = mBuilder.create()
                  dd.show()
          }

          }    // this function will activate depending on th esend types of the user in the chat secreesn

    R.id.star_function_id ->{
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
                    msg_number.add("${PAIR_}|${i+1}")                               // as the message number of the data base is one number ahead so add before retriving data
                }
                mBuilder.setSingleChoiceItems(list_items,1) { dialogInterface,i->
                    // dialog for unstar message
                    // change layout of recycle
                    val thread : Thread = Thread({
                        DB.update_one_chat_property("star", msg_number, "false")            // updating in the local database
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
                    msg_number.add("${PAIR_}|${i+1}")
                }
                mBuilder.setSingleChoiceItems(list_items,1) { dialogInterface , i ->
                    // dialog for star all message
                    // change layout of recycle
                    val thread : Thread = Thread({
                        DB.update_one_chat_property("star", msg_number, "true")        // updating in the local database
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

    R.id.forward_msg_id ->{        // this is used fro only forwarding mesages
              Toast.makeText(this,"Forward msg menu activate!!!",Toast.LENGTH_SHORT).show()
            // Intent to the forward message tamplate
            // select the contacts to forward
            // send to the server
        }

    R.id.copy_msg_id ->{
            TOTAL_VOTER_TEMPLATE = 0
            TOTAL_REACTION_TAMPLATE = 0

            for(i in selectedPostItems){
                val selection_types : String = adapter.position_to_type(i)
                if(selection_types=="VOTING_TAMPLATE")TOTAL_VOTER_TEMPLATE++
                if(selection_types=="REACTION_TAMPLATE")TOTAL_REACTION_TAMPLATE++
            }
            if(TOTAL_REACTION_TAMPLATE==0 && TOTAL_VOTER_TEMPLATE==0){
                // show that messages are coppied
                // copy all message
                Toast.makeText(this,"Messages are copied",Toast.LENGTH_SHORT).show()
            }
        }

    R.id.remainder_chat_id->{
              var remainder_chat_msg = 0
              for(i in selectedPostItems){    // now here i is the the index of selectedPositions
                  if(all_chats_store[i.toInt()].remainder!="none")remainder_chat_msg++
              }
            val msg_numbers : ArrayList<String> = ArrayList<String>()
            for(i in selectedPostItems){
                msg_numbers.add("${PAIR_}|${i+1}")                            // as the message number of the data base is one number ahead so add before retriving data
                Log.d("@@@@@Now selected message:","is: ${i}")
            }
            val DB = universal_chat_store(context_,null)

            if(selectedPostItems.size==remainder_chat_msg){   // when all the selected message is remaindered msg
                  Log.d("@@@@@Entering to ","all set reminader sections")
                  val list_items = arrayOf("Remove remainder from all ${selectedPostItems.size} messages for me")
                  val mBuilder = AlertDialog.Builder(this)
                  mBuilder.setTitle("Remove remiander")
                  mBuilder.setSingleChoiceItems(list_items,1){ dialogInterface,i->
                      // dialog for unremainder message
                      // change layout of recycle
                      val thread : Thread = Thread({
                          DB.update_one_chat_property("remainder", msg_numbers, "none")
                      })
                      thread.start()
                      Toast.makeText(this,"Remainder removed for this messages",Toast.LENGTH_LONG).show()
                      dialogInterface.dismiss()
                  }
                  mBuilder.setNeutralButton("Cancel"){ dialog , which->
                      dialog.cancel()
                  }
                  val dd = mBuilder.create()
                  dd.show()
              }

            if(selectedPostItems.size!=remainder_chat_msg){   // when all selected messages are not remaindered
                val mBuilder = AlertDialog.Builder(this)
                val layout = LinearLayout(context_)
                layout.orientation = LinearLayout.HORIZONTAL

                // this is for hours inputing
                val input_hours = EditText(context_)
                input_hours.hint = "Hours"
                input_hours.inputType = InputType.TYPE_CLASS_NUMBER
                layout.addView(input_hours) // Notice this is an add method

                // this is for minute inputing
                val minute_ = EditText(this)
                minute_.hint = "Minute"
                minute_.inputType = InputType.TYPE_CLASS_NUMBER
                layout.addView(minute_) // Another add method

                // this is for inputing AM or PM
                val _indi = EditText(this)
                _indi.hint = "AM or PM"
                _indi.inputType = InputType.TYPE_CLASS_TEXT
                layout.addView(_indi) // Another add method

                mBuilder.setView(layout) // Again this is a set method, not add
                mBuilder.setTitle("Please enter the time")
                mBuilder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    // Here you get get input text from the Edittext
                    var hours_ = input_hours.text.toString()
                    var minute_ = minute_.text.toString()
                    var _status : String  = _indi.text.toString()
                    set_options_for_remainder(msg_numbers,hours_,minute_,_status)

                    set_alarm(hours_,minute_)
                })
                val dd = mBuilder.create()
                dd.show()
                // for changing layout of the items
                val _total_selected = selectedPostItems.size
                for(j in selectedPostItems) {
                    adapter.notifyItemRangeChanged(selectedPostItems[0].toInt(),selectedPostItems[_total_selected-1].toInt(),"ADD_REMAINDER")
                }
            }
        }

  }
    return true
}

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {        //  this one is responsible for showing the each functions icon of long pressed
        mode?.let {
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.presshold_menu,menu)
            return true
        }
        return false
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode : ActionMode?){
        adapter.tracker?.clearSelection()
        adapter.notifyDataSetChanged()
        actionMode = null
    }

    private fun set_options_for_remainder(msg_numbers : ArrayList<String>,hours_ : String, minute : String,_indi : String){
        val list_items = arrayOf("Add remainder in all ${selectedPostItems.size} messages for me","Add remainder in all ${selectedPostItems.size} messages for all")
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Add Remiander")
        val mapper = jacksonObjectMapper()
        val DB = universal_chat_store(this,null)
        mBuilder.setSingleChoiceItems(list_items,1) { dialogInterface,i->
            // dialog for unremainder message
            // change layout of recycle

            val time_ = mapper.writeValueAsString(universal_model.time_(hours_.toString(),minute,_indi))   // this is the stringify status of time in hpur sand minutes
            var save_time_data : String = ""
            if(i==0) {                                                                                      // for setting remainder for only me
               save_time_data =  mapper.writeValueAsString(universal_model.remainder_store("me",time_))
               Toast.makeText(this,"adding remainder for others",Toast.LENGTH_SHORT).show()
            }
            if(i==1){                                                                                       // for setting remainder for only all
               save_time_data = mapper.writeValueAsString(universal_model.remainder_store("all",time_))
                Toast.makeText(this,"adding remainder for all",Toast.LENGTH_SHORT).show()
            }
            val thread: Thread = Thread({
                    DB.update_one_chat_property("remainder", msg_numbers, save_time_data)
            })
                thread.start()
                dialogInterface.dismiss()
        }

        mBuilder.setNeutralButton("Cancel"){ dialog , which->
            dialog.cancel()
        }
        val dd = mBuilder.create()
        dd.show()
    }    // this function is specific for when user is selected for add remainder as time

    private  fun set_alarm(hour_ : String , minute_ : String){       //this functions is for universal alarm setting in this chatScreen also for the timer chat
        val calendar : Calendar = Calendar.getInstance()

        if(Build.VERSION.SDK_INT >= 23){
            calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hour_.toInt(),minute_.toInt(), 0)
        }
        else{
            calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hour_.toInt(),minute_.toInt(), 0)
        }
        setAlarm(calendar.timeInMillis)
    }
    private fun setAlarm(timeInMillis : Long){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context_,MyAlarm::class.java)

        val pendingIntent = PendingIntent.getBroadcast(this,0,intent,0)

        alarmManager.setRepeating(
            AlarmManager.RTC,
            timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
      Toast.makeText(this,"Alarm is set",Toast.LENGTH_SHORT).show()
    }

    private fun hideReplayLayout(){                      // this functions hide the replay layout for if not required
        reply_layout.visibility = View.GONE
        SELECTED_REPLAY_MESSAGE = 0
    }                                                   //Swiping function of message
    private fun showQuotedMessage(message : universal_model.one_chat_property){
        Toast.makeText(this,"The replay text is:${message.data}",Toast.LENGTH_SHORT).show()
        edit_text.requestFocus()
        replay_of_message.setText("${message.data}")
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager?.showSoftInput(edit_text,InputMethodManager.SHOW_IMPLICIT)
        reply_layout.visibility = View.VISIBLE
    }
}
