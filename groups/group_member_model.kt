package com.example.database_part_3.groups


// before saving the groups members saves in the form of group_member_class()

class group_member_model(
    var member_name : String ,    // name of group member
    var member_number : String ,  // number of that member
    var _admin : Boolean ,        // that member is admin or not
    var can_write_chat : Boolean   // that person can write chats on group or not
)