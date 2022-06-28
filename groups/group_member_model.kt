package com.example.database_part_3.groups

import com.example.database_part_3.model.dp_


// before saving the groups members saves in the form of group_member_class()

class group_member_model(
    var dp_: String,              // display picture of that person
    var member_name : String ,    // name of group member
    var member_number : String ,  // mobile number of that member
    var _admin : Boolean ,        // that member is admin or not
    var can_write_chat : Boolean   // that person can write chats on group or not
)