package com.example.database_part_3.user_info;

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import com.example.database_part_3.R
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.db.universal_chat_store
import com.example.database_part_3.message_holder.WALLPAPER_URI
import com.example.database_part_3.model.persons_info_last_msg
import com.example.database_part_3.theme.color_selection
import com.google.android.material.bottomsheet.BottomSheetDialog

class screen_activity : AppCompatActivity() {
    private var parentRecyclerView : RecyclerView? = null
    private var ParentAdapter : RecyclerView.Adapter<*>? = null
    var parentModelArrayList : ArrayList<String> = ArrayList()
    private var parentLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var context: Context
    private val IMAGE_CHOOSE = 1000
    private lateinit var private_chat_string : TextView
    private lateinit var save_gallery_text_id : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pair_user_info)
        context = this
        val _name : String = intent.getStringExtra("_name_").toString()
        val _number : String = intent.getStringExtra("_number_").toString()

        var _save_to_gallery : Boolean = true
        var _private_chat : Boolean = true

        private_chat_string = findViewById(R.id.private_chat_id_string)
        save_gallery_text_id = findViewById(R.id.save_gallery_text_id)


        // retriving the data from the databse
        val DB = universal_chat_store(this,null)
        val handler = Handler()
        var store_ = persons_info_last_msg("",true,true)
        val thread  = Thread({
        store_ =  DB.get_persons_info(_number)
           handler.post({
              _save_to_gallery = store_.save_to_gallery
               _private_chat = store_.private_chat
           })
        })

        change_string_gallery(_save_to_gallery)   // for changung the strings of the xml
        change_string_private(_private_chat)

        //  setting the name and number in the usersinfo
        val name_ = findViewById<TextView>(R.id.pair_user_name_id)
        val number_ = findViewById<TextView>(R.id.pair_user_number_id)
        name_.text = "~$_name"
        number_.text = _number

        //set the Categories for each array list set in the  ParentrecyclerAdapter
        parentModelArrayList.add("PHOTOS")
        parentModelArrayList.add("VEDIOS")
        parentModelArrayList.add("DOCUMENTS")
        parentModelArrayList.add("AUDIOS")
        parentModelArrayList.add("LINKS")

        val button_ = findViewById<Button>(R.id.view_below_sheeet_media)

        button_.setOnClickListener({
            val sheet : BottomSheetDialog = BottomSheetDialog(this)
            sheet.setContentView(R.layout.below_sheet_info_media)

            parentRecyclerView = sheet.findViewById(R.id.pair_info_recycle_view_id)
            parentRecyclerView!!.setHasFixedSize(true)
            parentLayoutManager = LinearLayoutManager(this)
            ParentAdapter = ParentRecyclerViewAdapter(parentModelArrayList, this@screen_activity)
            parentRecyclerView!!.setLayoutManager(parentLayoutManager)
            parentRecyclerView!!.setAdapter(ParentAdapter)
            ParentAdapter!!.notifyDataSetChanged()
           sheet.show()
        })

        // save to gallery
        val save_to_gallery = findViewById<ImageButton>(R.id.save_to_gallery_id)
        save_to_gallery.setOnClickListener{
            Toast.makeText(this,"save to gallery is : clicked:",Toast.LENGTH_SHORT).show()
            val list_items = arrayOf("Don't save media in gallery of this pair","Save media in gallery of this pair")
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setSingleChoiceItems(list_items,-1){ _dialog , i ->
                 var status : Boolean = true
                if(i==0){   // dont save
                     status = false
                    /*
                      Do the code for not to save the medias in the phone falley
                    */
                 }
                if(i==1){   // save to gallery
                    status = true
                }
                change_string_gallery(status)
                val DB = universal_chat_store(this,null)
                val thread : Thread = Thread({
                    DB.update_persons_info("save_to_gallery","$status",_number)
                })
                thread.start()
                DB.close()
                _dialog.dismiss()
            }
            mBuilder.setNeutralButton("cancel"){ dialog_ , which->
                dialog_.cancel()
            }
            mBuilder.show()
        }


        // for making the chats private
        val private_chats = findViewById<ImageButton>(R.id.private_chats_id)
        private_chats.setOnClickListener{
            Toast.makeText(this,"private chats is on:",Toast.LENGTH_SHORT).show()
            val list_items = arrayOf("Turn on private chat","Turn off private chat")
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setSingleChoiceItems(list_items,-1){ _dialog , i ->
                var status : Boolean = true
                if(i==0){   // dont save
                    status = true
                }
                if(i==1){   // save to gallery
                    status = false
                }
                change_string_private(status)
                val DB = universal_chat_store(this,null)
                val thread : Thread = Thread({
                    DB.update_persons_info("private_chat","$status",_number)
                })
                thread.start()
                DB.close()
                _dialog.dismiss()
            }
            mBuilder.setNeutralButton("cancel"){ dialog_ , which->
                dialog_.cancel()
            }
            mBuilder.show()
        }

        // changing wallpaper
        val wall_paper = findViewById<ImageView>(R.id.change_wallpaper)
        wall_paper.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            intent.action = Intent.ACTION_GET_CONTENT                   //  this one allow to pick multiple image from gallery
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),IMAGE_CHOOSE)
        }

        // Blocking  person
        val block_person = findViewById<ImageView>(R.id.block_person)
        block_person.setOnClickListener{
        }
    }


    // after choosing the image from gallery the uri link of that image will come here
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent : Intent?){
        super.onActivityResult(requestCode, resultCode, intent)
        if(resultCode == Activity.RESULT_OK && requestCode==IMAGE_CHOOSE){
            val _uri : Uri ? = intent!!.data
            Toast.makeText(this,"Uri of image is :${_uri}",Toast.LENGTH_SHORT).show()
            WALLPAPER_URI = "${intent.data}"

            // bringing the layout of chat screen into this file
            val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view : View = inflater.inflate(R.layout.activity_chat,null)
            val recycle_view : RecyclerView = view.findViewById(R.id.messageList_recycle)

            val input_stream = contentResolver.openInputStream(_uri!!)
            val drawable_ = Drawable.createFromStream(input_stream,_uri.toString())
            recycle_view.background = drawable_

        }
    }

    fun change_string_gallery(_save_to_gallery : Boolean){
        if(_save_to_gallery==true){
            save_gallery_text_id.text = "Allow media to \t save in gallery"
        }
        if(_save_to_gallery==false){
            save_gallery_text_id.text = "Disallow media to \t save in gallery"
        }
    }

    fun change_string_private(status : Boolean){
       if(status==true)private_chat_string.text = "Private chat (on)"
       if(status==false)private_chat_string.text = "Private chat (off)"
    }

}
