package com.example.database_part_3.groups


// this is model of one message of any group message
class group_message_model(
    var group_name : String,
    var group_number : String,
    var msg_num : String,        //  message_number to String
    var data : String,           // Message in String   ............//  "none" if not used template , 1./*"storing_reaction" : { likes :{//total number of likes}, comments:{// all comments stored with name & time} ,topic: String }*/
                                 // 2./*"voting" :{ topic_names{ a):{ number of voters }  b):{ nmber of voters}  ....}  , comments : {  all comments with name & time}} */  for template message data
    var category : String,       // m: message , v:vedio , i : Image, d: document, s : sticker
    var read : String,           //  if (from=my_number) then "Hashmap<number,time>" mobile number of seen people and time the message seen otherwise "true" or "false"
    var delivered_ : String ,    // "Hashmap<number,time>" mobile number of people to whom message is delivered otherwise "true" or "false"
    var _delete : String,        //  "no": not deleted , "me" : deleted this message only for me & "both" : deleted for everyone
    var time_ : String,
    var edit_rewrite : Boolean,  // true: edited to rewrite the message , false: not edited
    var stared : Boolean,        // true : Stared this message , false : not given star to this message
    var template : String,       // "none" -> if not any data is used :
    var remainder : String,      // "all" : {time} , "me" : {time} , "none" : not remainder
    var from : Long,
    var replied_msg : String,    // "yes" , "none":- normal message , "text_image":-selected text and sended image in replay , "image_text":-selected image and replyed txt ,
                                 // "video_text","text_video" , "vote_text","sticker_text","text_sticker" , "video_sticker" ,
    var forwarded_msg : String   // "no": normal_message , "yes_name" : {name of previous origin} , "yes" : normal_forward
)