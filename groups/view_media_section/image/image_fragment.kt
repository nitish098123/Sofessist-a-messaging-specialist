package com.example.database_part_3.groups.view_media_section.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.example.database_part_3.R

class image_fragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflater = inflater.inflate(R.layout.photo_grid_view, container, false)
        val list = ArrayList<Int>()
        for(i in 0..10) list.add(R.drawable.save_to_galary)

        val grid_view = inflater.findViewById<GridView>(R.id.gridView)
        grid_view.adapter = photo_adapter(list)

        return inflater
    }
    // Here "layout_login" is a name of layout file
    // created for LoginFragment
}