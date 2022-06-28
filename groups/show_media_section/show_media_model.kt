package com.example.database_part_3.groups.show_media_section

class show_media_model(
    var uri_ : String,           // link of showing image or video
    var text_ : String,          // if some text has sended along with this media
    var category_ : String,      // "i"-image , "v"-video , "d"-document, "l"-links
    var time_ : String,          // time at which the message is sended
    var msg_number : String ,    // message number of that item
    var from_ : String,          // who sended this media
)