package com.example.database_part_3.message_holder

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

class swipe_message_animation(var view :View,
                               private val startHeight : Int,
                               private val targetHeight : Int) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        if(startHeight==0 || targetHeight==0){
            view.layoutParams.height =
                (startHeight + (targetHeight - startHeight) * interpolatedTime).toInt()
        }  else{
            view.layoutParams.height =
                (startHeight + targetHeight * interpolatedTime).toInt()
        }
        view.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}