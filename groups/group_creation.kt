package com.example.database_part_3.groups

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.database_part_3.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class group_creation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tagList: ArrayList<String> = ArrayList<String>()
        tagList.add("sdgjfgsf")
        tagList.add("sdgjfgsf")
        tagList.add("sdgjfgsf")
        tagList.add("sdgjfgsf")
        tagList.add("sdgjfgsf")
        tagList.add("sdgjfgsf")

        val chipGroup = findViewById<ChipGroup>(R.id.chips_id)
        for (index in tagList.indices) {
            val tagName = tagList[index]
            val chip : Chip = Chip(this)
            chip.chipIcon = ContextCompat.getDrawable(this,R.drawable.display_picture)
            chip.text = tagName
            chip.isChipIconVisible = false
            chip.isCloseIconVisible = true
            chip.isClickable = true
            chip.isCheckable = false
            chip.setOnCloseIconClickListener {
                tagList.remove(tagName)
                chipGroup.removeView(chip)
            }
            chipGroup.addView(chip)
        }
    }
}