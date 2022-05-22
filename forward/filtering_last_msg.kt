package com.example.database_part_3.forward

import android.util.Log
import com.example.database_part_3.model.universal_model

// this is for sending the message to forwarding to the database
class container(var pair : String , var last_number : Int, var name : String , var _number : String){}


class filtering_last_msg(){
    fun filter( pairs_ : ArrayList<pair_to_model> ,pair_last_number : ArrayList<container>
                , message: ArrayList<universal_model.one_chat_property>) : ArrayList<universal_model.one_chat_property>{

        val final_pairs_number : ArrayList<container> = ArrayList<container>()
        for(j in pairs_){
            final_pairs_number.add(container(j.pairs_,0,j.name_,j._number))    // initializations of final number store
        }

        for(kk in pair_last_number){
            for(static_ in final_pairs_number){
                if(static_.pair==kk.pair){
                    static_.last_number = kk.last_number     // this is the last message update of every person contacts
            }
        } }

        val final_message : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()
        for (jj in final_pairs_number){
           var _number = jj.last_number
           for (msg in message) {
               _number++
               final_message.add(universal_model.one_chat_property(jj.pair,jj.name,_number.toString(),msg.data,msg.category,msg.read,msg._delete,msg.time_,
                              msg.edit_rewrite,msg.stared,msg.template,msg.remainder,msg.lock,MY_NUMBER,jj._number.toLong(),msg.replied_msg,msg.forwarded_msg))
           }
        }
        return  final_message
    }
}