package com.example.database_part_3.groups

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil

class group_diff_util_callback( private val oldList: List<group_message_model>,
                                private val newList: List<group_message_model> ) : DiffUtil.Callback(){

    override fun getNewListSize(): Int {
       return newList.size
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        var _type = false
        if(oldList[oldItemPosition].msg_num == newList[newItemPosition].msg_num) _type = true
        return  _type
    }

    override fun areContentsTheSame(oldItemPosition : Int, newItemPosition : Int): Boolean {
        return when {
            oldList[oldItemPosition].data != newList[newItemPosition].data->{
                return false
            }
            oldList[oldItemPosition].edit_rewrite!=newList[newItemPosition].edit_rewrite ->{
                return false
            }
            oldList[oldItemPosition].stared != newList[newItemPosition].stared ->{
                return false
            }
            oldList[oldItemPosition].remainder != newList[newItemPosition].remainder ->{     // this will notify alarm change
                return false
            }
            else -> true
        }
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}