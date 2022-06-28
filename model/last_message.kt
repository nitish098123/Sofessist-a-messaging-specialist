package com.example.database_part_3.model

// whenever last message is saved into the database
// than this class is called along with last message and time
class last_message(var last_message : String ,
                   var _time : String = "${System.currentTimeMillis()}")