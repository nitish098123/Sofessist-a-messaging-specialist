package com.example.database_part_3.model

import com.example.database_part_3.groups.group_message_model
import java.util.*
import kotlin.collections.ArrayList

class reaction_store_model(
    var topic : String ,      // at which topic this template is made of
    var you_liked : Boolean , // true-> you liked this template
    var total_like : Int ,    // total number of liked of this template
    var total_comment : ArrayList<universal_model.one_chat_property>
)

class reaction_store_group_model(
    var topic : String ,      // at which topic this template is made of
    var you_liked : Boolean , // true-> you liked this template
    var total_like : Int ,    // total number of liked of this template
    var total_comment : ArrayList<group_message_model>   // store all the reaction of that particular thread
)