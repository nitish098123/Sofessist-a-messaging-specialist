package com.example.database_part_3.groups.view_media_section.image

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView


// this is adapter of grid View for the photos in media view sections

class photo_adapter (private val list : ArrayList<Int>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val img = ImageView(parent?.context)
        img.setImageResource(list[position])
        img.scaleType = ImageView.ScaleType.FIT_XY
        img.layoutParams = ViewGroup.LayoutParams(250,250)

        return img
    }

    override fun getItem(position: Int): Int = list[position]

    override fun getItemId(position: Int): Long = 0

    override fun getCount(): Int = list.size
}