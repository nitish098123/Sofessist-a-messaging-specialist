package com.example.database_part_3.groups

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil

class group_diff_util_callback (private val oldList: List<group_message_model>,
                                private val newList: List<group_message_model>) : DiffUtil.Callback() {

    override fun getCourseNew(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].data === newList.get(newItemPosition).data
    }
    override fun areContentsTheSame(oldItemPosition : Int, newItemPosition : Int): Boolean {
        val  data1 = oldList[oldItemPosition].data
        val msg_number1 = oldList[oldItemPosition].msg_num
        val data2 = newList[newItemPosition].data
        val msg_number2 = newList[newItemPosition].msg_num
        return  data1==data2 && msg_number1 == msg_number2
    }
    @Nullable
    override fun geeksPayload(oldCourse: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldCourse, newPosition)
    }
}