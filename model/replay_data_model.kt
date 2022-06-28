package com.example.database_part_3.model


/*  when some message is selected to reply message then this class is stringify in the data column database  */
class replay_data_model(
    var text_ : String ,        // the message in form of text which is selected
    var msg_number : String,    //  the message number of selected text to be replay
    var reply_message : String  //  the now original text of replay
)