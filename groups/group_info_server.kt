package com.example.database_part_3.groups

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R

class group_info_server : AppCompatActivity(){

    private lateinit var view_photos_button : Button
    private lateinit var view_videos_button : Button
    private lateinit var view_documents_button : Button
    private lateinit var view_links_button : Button

    private lateinit var show_photos_recycle : RecyclerView
    private lateinit var show_videos_recycle : RecyclerView
    private lateinit var show_documents_recycle : RecyclerView
    private lateinit var show_links_recycle : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {     // this means u can take inputs from layouts directly
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_section_view)

        val toolbar_: Toolbar = findViewById(R.id.tool_bar_chat_screen)   // toolbar of media section
        setSupportActionBar(toolbar_)
        toolbar_.showOverflowMenu()

        view_photos_button = findViewById(R.id.view_photo)
        show_photos_recycle = findViewById(R.id.photo_show_id)
        view_videos_button = findViewById(R.id.view_videos_id)
        show_videos_recycle = findViewById(R.id.videos_show_id_recycle)
        view_documents_button = findViewById(R.id.view_documents_id)
        show_documents_recycle = findViewById(R.id.view_document_recycle_view)
        view_links_button = findViewById(R.id.view_links_id)
        show_links_recycle = findViewById(R.id.view_link_recycle_view)

        // showing recycle View of photos
        view_photos_button.setOnClickListener {

        }

    }
}