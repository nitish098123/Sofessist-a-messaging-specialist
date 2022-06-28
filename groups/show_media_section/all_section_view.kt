package com.example.database_part_3.groups.show_media_section

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.Items_detail_look_up
import com.example.database_part_3.groups.group_creation_adapter
import com.example.database_part_3.message_holder.PRIVATE_CHAT
import kotlin.math.exp


// this will show the all media sections

class all_section_view : AppCompatActivity(),
                         OnContactClickListener,
                         ActionMode.Callback  {

    private lateinit var photos_button : Button
    private lateinit var video_button : Button
    private lateinit var document_button : Button
    private lateinit var link_button : Button
    private lateinit var storing_template_button : Button
    var tracker : SelectionTracker<Long>? = null
    private lateinit var reaction_temp_recycle: RecyclerView
    private lateinit var photos_recycle : RecyclerView
    private lateinit var video_recycle : RecyclerView
    private lateinit var document_recycle : RecyclerView
    private lateinit var link_recycle : RecyclerView
    private var PREVISOUS_SELECTED_BUTTON = ""
    private var selectedPostItems : MutableList<Long> = mutableListOf()       // this contains the message position that is selected for activation
    private var actionMode : ActionMode? = null
    var group_number : String = ""
    var OPENNING_STATUS_PHOTO : Boolean = false                      // if some of the sections is openning  then this become true
    var OPENNING_STATUS_VIDEO : Boolean  = false
    var OPENNING_STATUS_DOCUMENT : Boolean = false
    var OPENNING_STATUS_LINK : Boolean = false
    var OPENNING_STATUS_REACTION : Boolean = false
    var PRIVATE_CHAT : Boolean = false
    private lateinit var null_photo_text : TextView
    private lateinit var null_video_text : TextView
    private lateinit var null_document_text : TextView
    private lateinit var null_link_text : TextView
    private lateinit var null_reaction_store : TextView


    var adapter : adapter_recycle_view ? = null

    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_section_view)

        val toolbar_ : Toolbar = findViewById(R.id.media_sections_tool_bar)
        setSupportActionBar(toolbar_)
        toolbar_.showOverflowMenu()

        group_number = intent.getStringExtra("group_number").toString()     // get group number from the previous activity
        PRIVATE_CHAT = intent.getStringExtra("private_chat").toBoolean()

        // bringing the data
        photos_button = findViewById(R.id.view_photo)
        photos_recycle = findViewById(R.id.photo_show_id)
        video_button = findViewById(R.id.view_videos_id)
        video_recycle = findViewById(R.id.videos_show_id_recycle)
        document_button = findViewById(R.id.view_documents_id)
        document_recycle = findViewById(R.id.view_document_recycle_view)
        link_button = findViewById(R.id.view_links_id)
        link_recycle = findViewById(R.id.view_link_recycle_view)
        storing_template_button = findViewById(R.id.view_template_button)          // brings the reactions tempplate data buttons
        reaction_temp_recycle = findViewById(R.id.view_template_recycle)    // view the rections template data

        null_document_text = findViewById(R.id.null_document_text)
        null_link_text = findViewById(R.id.null_links_text)
        null_photo_text = findViewById(R.id.null_photo_text)
        null_reaction_store = findViewById(R.id.null_template_text)
        null_video_text = findViewById(R.id.null_video_text)

        val handler = Handler()
        val thread = Thread({

            handler.post {

                // clicking to photos to view shared photos
                photos_button.setOnClickListener {
                    // get data from database and adapt the recycleView with that data
                    get_data_db("i",group_number)

                    var t =0
                    if(OPENNING_STATUS_PHOTO==true){
                       t++
                       photos_recycle.visibility = View.GONE
                       OPENNING_STATUS_PHOTO = false
                       photos_button.setBackgroundResource(R.drawable.round_background)
                    }
                    if(t==0){
                        if(OPENNING_STATUS_PHOTO==false){
                          photos_recycle.visibility = View.VISIBLE
                          OPENNING_STATUS_PHOTO = true
                          photos_button.setBackgroundResource(R.drawable.round_rectangle_light)
                        }
                    }


                    video_recycle.visibility = View.GONE
                    document_recycle.visibility =View.GONE
                    link_recycle.visibility = View.GONE
                    reaction_temp_recycle.visibility = View.GONE

                    OPENNING_STATUS_VIDEO = false
                    OPENNING_STATUS_DOCUMENT  = false
                    OPENNING_STATUS_LINK  = false
                    OPENNING_STATUS_REACTION = false
                }

                // clicking to videos to view video
                video_button.setOnClickListener{
                    var t=0
                    if(OPENNING_STATUS_VIDEO==false){
                        t++
                        OPENNING_STATUS_VIDEO = true
                        video_recycle.visibility = View.VISIBLE
                        video_button.setBackgroundResource(R.drawable.round_rectangle_light)   // when selected
                    }
                    if(t==0){
                        if (OPENNING_STATUS_VIDEO == true){
                            video_recycle.visibility = View.GONE
                            OPENNING_STATUS_VIDEO = false
                            video_button.setBackgroundResource(R.drawable.round_background)    // when unselected
                        }
                    }

                    // long pressed
                    long_pressed(video_recycle)

                    photos_recycle.visibility = View.GONE
                    document_recycle.visibility =View.GONE
                    link_recycle.visibility = View.GONE
                    reaction_temp_recycle.visibility = View.GONE

                    OPENNING_STATUS_PHOTO = false
                    OPENNING_STATUS_DOCUMENT  = false
                    OPENNING_STATUS_LINK  = false
                    OPENNING_STATUS_REACTION = false

                    // for button selecting part
                    photos_button.setBackgroundResource(R.drawable.round_background)    // when unselected
                    document_button.setBackgroundResource(R.drawable.round_background)
                    link_button.setBackgroundResource(R.drawable.round_background)
                    storing_template_button.setBackgroundResource(R.drawable.round_background)

                    get_data_db("v",group_number)
                }

                // document button
                document_button.setOnClickListener {
                    var t=0
                    if(OPENNING_STATUS_DOCUMENT==false){
                        t++
                        document_recycle.visibility = View.VISIBLE
                        OPENNING_STATUS_DOCUMENT = true
                        document_button.setBackgroundResource(R.drawable.round_rectangle_light)    // selected
                    }
                    if(t==0) {
                        if (OPENNING_STATUS_DOCUMENT == true) {
                            document_recycle.visibility = View.GONE
                            OPENNING_STATUS_DOCUMENT = false
                            document_button.setBackgroundResource(R.drawable.round_background)    // unselected
                        }
                    }

                    // document long pressed
                    long_pressed(document_recycle)

                    photos_recycle.visibility = View.GONE
                    video_recycle.visibility = View.GONE
                    link_recycle.visibility = View.GONE
                    reaction_temp_recycle.visibility = View.GONE

                    OPENNING_STATUS_PHOTO = false
                    OPENNING_STATUS_VIDEO  = false
                    OPENNING_STATUS_LINK  = false
                    OPENNING_STATUS_REACTION = false

                    // for button selecting part
                    photos_button.setBackgroundResource(R.drawable.round_background)
                    video_button.setBackgroundResource(R.drawable.round_background)
                    link_button.setBackgroundResource(R.drawable.round_background)
                    storing_template_button.setBackgroundResource(R.drawable.round_background)

                    get_data_db("d",group_number)
                }

                // link click
                link_button.setOnClickListener {
                    var t=0
                    if(OPENNING_STATUS_LINK==false){
                        t++
                        link_recycle.visibility = View.VISIBLE
                        OPENNING_STATUS_LINK = true
                        link_button.setBackgroundResource(R.drawable.round_rectangle_light)    // unselected
                    }
                    if(t==0){
                        if (OPENNING_STATUS_LINK == true){
                            link_recycle.visibility = View.GONE
                            OPENNING_STATUS_LINK = false
                            link_button.setBackgroundResource(R.drawable.round_background)    // unselected
                        }
                    }

                    // for long pressed of this sections items
                    long_pressed(link_recycle)

                    photos_recycle.visibility = View.GONE
                    video_recycle.visibility = View.GONE
                    document_recycle.visibility =View.GONE
                    reaction_temp_recycle.visibility = View.GONE

                    // for the diselecting color
                    OPENNING_STATUS_PHOTO = false
                    OPENNING_STATUS_VIDEO  = false
                    OPENNING_STATUS_DOCUMENT  = false
                    OPENNING_STATUS_REACTION = false

                    // for button selecting part
                    photos_button.setBackgroundResource(R.drawable.round_background)
                    video_button.setBackgroundResource(R.drawable.round_background)
                    document_button.setBackgroundResource(R.drawable.round_background)
                    storing_template_button.setBackgroundResource(R.drawable.round_background)

                    get_data_db("d",group_number)
                }

                // for storing reaction template
                storing_template_button.setOnClickListener{
                    var t=0
                    if(OPENNING_STATUS_REACTION==false){
                        t++
                        reaction_temp_recycle.visibility = View.VISIBLE
                        OPENNING_STATUS_REACTION = true
                        storing_template_button.setBackgroundResource(R.drawable.round_rectangle_light)    // unselected
                    }
                    if(t==0) {
                        if (OPENNING_STATUS_REACTION == true){
                            reaction_temp_recycle.visibility = View.GONE
                            OPENNING_STATUS_REACTION = true
                            storing_template_button.setBackgroundResource(R.drawable.round_background)    // unselected
                        }
                    }

                    // for long pressed
                    long_pressed(reaction_temp_recycle)

                    photos_recycle.visibility = View.GONE
                    video_recycle.visibility = View.GONE
                    link_recycle.visibility = View.GONE
                    document_recycle.visibility =View.GONE

                    // for the diselecting color
                    OPENNING_STATUS_PHOTO = false
                    OPENNING_STATUS_VIDEO  = false
                    OPENNING_STATUS_DOCUMENT  = false
                    OPENNING_STATUS_LINK = false

                    // for button selecting part
                    photos_button.setBackgroundResource(R.drawable.round_background)
                    video_button.setBackgroundResource(R.drawable.round_background)
                    document_button.setBackgroundResource(R.drawable.round_background)
                    link_button.setBackgroundResource(R.drawable.round_background)


                    get_data_db("storing_template",group_number)
                }

            }
        })

        thread.start()

    }


    override fun onContactClickListener(position: Int) {
        Toast.makeText(this,"You clicked the positions:${position}",Toast.LENGTH_LONG).show()
        /* intent to show largely */
    }

    // long pressed function
    fun long_pressed(message_list_show : RecyclerView){
        // this is for long pressed detections in the group chat
        tracker = SelectionTracker.Builder<Long>(
            "mySelection",
            message_list_show,
            StableIdKeyProvider(message_list_show),
            media_details_look_up(message_list_show),
            StorageStrategy.createLongStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
            adapter!!.tracker = tracker

            //  making the tracker for selections
            tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>(){
            override fun onSelectionChanged(){
                super.onSelectionChanged()
                tracker.let {
                    selectedPostItems = it!!.selection.toMutableList()
                    Log.d("","@@@@@@@@selectedPosition:${selectedPostItems}")
                    Log.d("","$$$$$$$$$$$$$ it.selection:${it.selection} , it: ${it}")
                    if(selectedPostItems.isEmpty()){    // if nothing is selected
                        actionMode?.finish()
                    }
                    else{                             // if some items of messages are selected
                        if(actionMode==null) actionMode = startSupportActionMode(this@all_section_view)
                        actionMode?.title = "${selectedPostItems.size}"        // number of selected items to show in toolbar
                    }
                }
            }
        })
    }


    // $$$$$$$$$ for long pressed detection $$$$$$$$$$$
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.let{
            val inflater : MenuInflater = it.menuInflater
            inflater.inflate(R.menu.media_section_menu,menu)
            return true
        }
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
       when(item?.itemId){
           R.id.view_in_chat -> {
               Toast.makeText(this,"view in chat clicked",Toast.LENGTH_LONG).show()
           }
           R.id.delete_item -> {
               Toast.makeText(this,"delete item clicked",Toast.LENGTH_LONG).show()
           }
       }
       return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?){
        adapter!!.tracker?.clearSelection()
        actionMode = null
    }

    // $$$$$$$$$$$$$$ finished long pressed detection $$$$$$$$$$

    // this will bring the data from database
    fun get_data_db(operators_ : String , group_number : String){
        var store_ = ArrayList<show_media_model>()
        val DB = universal_chat_store(this, null)

        val handler = Handler()
        Thread({

            store_ = DB.get_all_media_group(operators_, group_number)
            handler.post {

                if (store_.size != 0){

                // for image
                if (operators_ == "i"){
                        photos_recycle.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
                        photos_recycle.setHasFixedSize(true)
                        adapter = adapter_recycle_view(this, store_, this, PRIVATE_CHAT)
                        photos_recycle.adapter = adapter

//                        // making eleigible for long presssed
                        long_pressed(photos_recycle)
                }

                // for videos
                if (operators_ == "v"){
                    val layout_manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    video_recycle.layoutManager = layout_manager
                    video_recycle.setHasFixedSize(true)
                    adapter = adapter_recycle_view(this, store_, this, PRIVATE_CHAT)
                    video_recycle.adapter = adapter
                }

                // for documents
                if (operators_ == "d"){
                    val layout_manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    document_recycle.layoutManager = layout_manager
                    document_recycle.setHasFixedSize(true)
                    adapter = adapter_recycle_view(this, store_, this, PRIVATE_CHAT)
                    document_recycle.adapter = adapter
                }

                // for showing link
                if (operators_ == "l") {
                    val layout_manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    link_recycle.layoutManager = layout_manager
                    link_recycle.setHasFixedSize(true)
                    adapter = adapter_recycle_view(this, store_, this, PRIVATE_CHAT)
                    link_recycle.adapter = adapter
                }

                // for storing reaction
                if (operators_ == "storing_reaction"){
                    val layout_manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    reaction_temp_recycle.layoutManager = layout_manager
                    reaction_temp_recycle.setHasFixedSize(true)
                    adapter = adapter_recycle_view(this, store_, this, PRIVATE_CHAT)
                    reaction_temp_recycle.adapter = adapter
                }

                }

//                // ensure that all null text are gone visible if arrayLIst is full
//                null_text("i")
//                null_text("v")
//                null_text("l")
//                null_text("d")
//                null_text("storing_reaction")

                if(store_.size==0){
                    null_text(operators_)
                }
                ensure_others_null(operators_)
            }
        }).start()

    }


    // view unview the null text to show if happens
    fun null_text(operators_: String){

       if(operators_=="i"){
           if(OPENNING_STATUS_PHOTO==true)null_photo_text.visibility = View.VISIBLE
           if(OPENNING_STATUS_PHOTO==false)null_photo_text.visibility = View.GONE
       }

        if(operators_=="v"){
            if(OPENNING_STATUS_VIDEO==true)null_video_text.visibility = View.VISIBLE
            if(OPENNING_STATUS_VIDEO==false)null_video_text.visibility = View.GONE
        }

        if(operators_=="l"){
            if(OPENNING_STATUS_LINK==true)null_link_text.visibility = View.VISIBLE
            if(OPENNING_STATUS_LINK==false)null_link_text.visibility = View.GONE
        }

        if(operators_=="d"){
            if(OPENNING_STATUS_DOCUMENT==true)null_document_text.visibility = View.VISIBLE
            if(OPENNING_STATUS_DOCUMENT==false)null_document_text.visibility = View.GONE
        }

        if(operators_=="storing_reaction"){
            if(OPENNING_STATUS_REACTION==true)null_reaction_store.visibility = View.VISIBLE
            if(OPENNING_STATUS_REACTION==false)null_reaction_store.visibility = View.GONE
        }

    }


    // for ensuring others null representation is not viewed
    fun ensure_others_null(operators_ : String){
        if(operators_!="i"){
            null_photo_text.visibility = View.GONE
            OPENNING_STATUS_PHOTO = false
        }
        if(operators_!="v"){
            null_video_text.visibility = View.GONE
            OPENNING_STATUS_VIDEO = false
        }
        if(operators_!="l"){
            null_link_text.visibility = View.GONE
            OPENNING_STATUS_LINK = false
        }
        if(operators_!="d"){
            null_document_text.visibility = View.GONE
            OPENNING_STATUS_DOCUMENT = false
        }
        if(operators_!="storing_reaction"){
            null_reaction_store.visibility = View.GONE
            OPENNING_STATUS_REACTION = false
        }
    }

 }