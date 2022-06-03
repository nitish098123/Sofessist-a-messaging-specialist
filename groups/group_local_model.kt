package com.example.database_part_3.groups

class group_local_model(
    var _group_number : String,
    var _members : ArrayList<group_member_model> ,   // string of numbers of ArrayList<>
    var _private : Boolean ,  // true-> cannot take screen shoot , forward copy etc
    var descriptions : String,
    var _group_name : String,
    var _dp : String ,         // string of json of-> _link: server_link, uri : URI of image in local device
    var mute_group : Boolean ,
    var mute_specific : ArrayList<String> ,  // list of all persons who you don't want notification
    var _archived_group : Boolean,
    var group_wallpaper : String
    )