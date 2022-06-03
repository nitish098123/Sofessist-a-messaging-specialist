package com.example.database_part_3.groups

class group_info_model(
    val group_dp : String,
    val group_member : ArrayList<group_member_model> ,     // this collectively contains all members , with admins , with write chat permission in group
    val group_notification : Boolean ,   // notification of group
    val group_wallpaper : String,
    val individual_private_chat : Boolean , // private chat features applied only for individual
    val group_private_chat : Boolean,        // private chat from group creation applied to all group members
    val group_auto_delete : String ,         // "none" , "String"
    val mute_notification_specific : ArrayList<String>, // list of persons that are muted
)