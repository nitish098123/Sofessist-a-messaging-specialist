package com.example.database_part_3.comments_view

import android.opengl.Visibility
import android.os.Bundle
import android.os.DropBoxManager
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.message_holder.DataAdapter
import com.example.database_part_3.model.reaction_store_model
import com.example.database_part_3.model.universal_model
import com.example.database_part_3.model.universal_model.one_chat_property
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.concurrent.thread

val MY_NUMBER_ = 6900529357
class comment_view_section : AppCompatActivity(){
    private lateinit var adapter : DataAdapter
    val mapper = jacksonObjectMapper()
    var TOTAL_COMMENTS = 0
    var PRIVATE_CHAT = false
    private lateinit var comment_recycle_view : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reaction_template_comment_view)

        // receives data from intent that is given
        val reaction_store_string : String = intent.getStringExtra("+comments").toString()
        val sender_name : String = intent.getStringExtra("+sender_name").toString()
        val comment_pair  : String = intent.getStringExtra("+pair_comment").toString()
        val PAIR : String = intent.getStringExtra("+PAIR").toString()
        val msg_number : String = intent.getStringExtra("+msg_number").toString()
        PRIVATE_CHAT = intent.getStringExtra("+private_chat").toBoolean()


        // for restricting screenshoot of chats
        if(PRIVATE_CHAT==true) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE) // for restricting the screenshoot the screen
        }
        if(PRIVATE_CHAT==false){
            val private_chat_ = findViewById<ImageView>(R.id.private_chat_thread_)
            private_chat_.visibility = View.GONE                                    // if it is not private chat then make it invisible
        }

        var thread_store = mapper.readValue<reaction_store_model>(reaction_store_string)

        // setting the topic
        val discuss_topic = findViewById<TextView>(R.id.discussion_topic_id)
        discuss_topic.text = thread_store.topic

        // all the one_chat_property comments
        var all_comments = ArrayList<one_chat_property>()
        all_comments = thread_store.total_comment

        if(all_comments.size==0)
        if(all_comments.size>0){
             val tt = all_comments.size
             TOTAL_COMMENTS = all_comments[tt-1].msg_num.toInt()    // this the last message number of one thread
        }

        // setting the sender name
        val sender_ : TextView = findViewById(R.id.sender_show_id)
        sender_.text = sender_name

        comment_recycle_view = findViewById<RecyclerView>(R.id.comment_recycle_view)
        comment_recycle_view.layoutManager = LinearLayoutManager(this)
        adapter = DataAdapter(this)
        comment_recycle_view.adapter = adapter

        initiate_process_me(all_comments)

        val send_ = findViewById<Button>(R.id.send_comment_id)

        // posting the comments
        send_.setOnClickListener{
            TOTAL_COMMENTS++
            val time_ = "${System.currentTimeMillis()}"
            val data_ : EditText = findViewById(R.id.text_comment_id)
            val message_ : String = data_.text.toString()
            data_.setText("")
            var comment_post : one_chat_property = one_chat_property("$comment_pair","$MY_NUMBER_","$TOTAL_COMMENTS",message_,"m",true,
                                  "no",time_,false, false,"none","none",false,MY_NUMBER_,1,"none","none")

            all_comments.add(comment_post)                       // this is the updated versions of latest comment

            thread_store.total_comment = all_comments

            val save_data : String = mapper.writeValueAsString(thread_store)       // this thread store Str
            val DB = universal_chat_store(this,null)
            val thread = Thread({
                DB.update_reaction_template("you_comment",save_data,PAIR,msg_number)     // you can now directly update this data into data of thread store
            })
            thread.start()

            initiate_process_me(all_comments)    // for sending to the adapter
        }
    }

    private fun initiate_process_me(data : ArrayList<one_chat_property>){         // this will take all the type of message and will
        adapter.setData(data)
        comment_recycle_view.scrollToPosition(adapter.itemCount-1)    // viewing on the screen the last message
    }


}