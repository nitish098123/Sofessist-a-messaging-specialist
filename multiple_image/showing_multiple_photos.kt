package com.example.database_part_3.multiple_image

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.group_creation_adapter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ortiz.touchview.TouchImageView
import com.squareup.picasso.Picasso

class showing_multiple_photos : AppCompatActivity() ,
                                OnContactClickListener
    {

    private lateinit var delete_button : ImageView
    private lateinit var show_image : TouchImageView
    private lateinit var add_message : EditText
    private lateinit var send_button : Button
    private val mapper = jacksonObjectMapper()
    private lateinit var recycle_view_photo : RecyclerView
    private lateinit var adapter : image_show_adapter
    private lateinit var context_ : Context
    private var all_uri : ArrayList<image_select_model> = ArrayList<image_select_model>()
    private var position_ : Int = 0     // contains the position of current showing image

    override fun onCreate(savedInstanceState: Bundle?){     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_image_show_layout)

        // inflating buttons
        delete_button = findViewById(R.id.delete_this_image)
        show_image = findViewById(R.id.show_image_multiple)
        add_message = findViewById(R.id.add_message_in_image)
        send_button = findViewById(R.id.send_all_image_button)
        recycle_view_photo = findViewById(R.id.all_photo_show)
        context_ = this

        val handler = Handler()

        val uri_str = intent.getStringExtra("all_uri")!!
        all_uri = mapper.readValue<ArrayList<image_select_model>>(uri_str)

        Thread({
            handler.post{
                // showing multiple photos
                val layout_manager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
                recycle_view_photo.layoutManager = layout_manager
                recycle_view_photo.setHasFixedSize(true)
                adapter = image_show_adapter(this,all_uri,this)
                recycle_view_photo.adapter = adapter
                show_image_(all_uri[0].uri_str)
            }
        }).start()


        // for deleting the image from the sending list
        delete_button.setOnClickListener{
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Remove from sending list")
            dialog.setPositiveButton("Yes"){ d,_ ->
                all_uri.removeAt(position_)
                adapter.update_list(all_uri)

                position_ = 0
                show_image_(all_uri[0].uri_str)
                d.dismiss()
            }
            dialog.setNegativeButton("No"){d,_->

                d.dismiss()
            }
            dialog.show()
        }


        // afetr completing
        send_button.setOnClickListener{

            /* save to the database */

            // send back the final uri's of images to activity
            val arr_str = mapper.writeValueAsString(all_uri)
            val data = Intent()
            data.putExtra("FINAL_URI" , arr_str);
            setResult(Activity.RESULT_OK , data);
            finish()
        }
    }

    fun show_image_(uri_str : String){
          show_image.setImageURI(uri_str.toUri())
    }

    override fun onContactClickListener(position: Int){
         position_ = position
         show_image_(all_uri[position].uri_str)
    }

 }