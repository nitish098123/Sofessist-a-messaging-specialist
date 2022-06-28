package com.example.database_part_3.model

import org.w3c.dom.Comment
import java.sql.Time


// always use datatypes like this way
sealed class universal_model {
    data class Header(
        val bgColor: Int,
        val title: String
    ) : universal_model()

    data class Me_user (
        var msg_number : String,        // this msg_number is directly from the database sql ite
        var  name_ : String,
        var message_ : String,
        var time_ : String
       ) : universal_model()

    data class other_user(
        var _name : String,
        var _message : String
    ) : universal_model()

    data class contact_transfer(  // this is used for transferring contacts from one file to another
        var _name : String,
        var _number : Long
    ) : universal_model()

    data class one_chat_property(
        var pair : String,
        var name : String,
        var msg_num : String,    //  message_number to String
        var data : String,     // Message in String
        var category : String ,  // m: message , v:vedio , i : Image, d: document, s : sticker
        var read : Boolean,      // true : readed , false : unread
        var _delete : String,    //  "no": not deleted , "me" : deleted this message only for me & "both" : deletd for everyone
        var time_ : String,
        var edit_rewrite : Boolean,  // true: edited to rewrite the message , false: not edited
        var stared : Boolean,        // true : Stared this message , false : not given star to this message
        var template : String,       //  "none" if not used template , 1./*"storing_reaction" :{ likes :{//total number of likes}, comments:{// all comments stored with name & time} ,topic: String }*/
                                         // 2./*"voting" :{ topic_names{ a):{ number of voters }  b):{ nmber of voters}  ....}  , comments : {  all comments with name & time}} */
        var remainder : String, // "all" : {time} , "me" : {time} , "none" : not remainder
        var lock : Boolean,     // "true": locked message , "false" : normal message
        var from : Long,
        var to : Long,
        var replied_msg : String,   // "message_number" , "no" : normal message
        var forwarded_msg : String   // "no": nomal message , "yes_name" : {name of previous origin} , "yes" : normal forward
    ) : universal_model()

 data class person_info (
       var dp : String  , // path of the picture
       var about : String,
       var archived : Boolean ,   // true : Arciveds , fa;se : normal contact
       var blocked : Boolean,   // true : Blocked
       var stared_msg : String ,  // { 1, 2, 3, }  messages in json
       var  mute : Boolean , // true : muted contact
       var pined : Boolean , // true : pinned this contact
       var last_message_number_seen : String , // message number of last time message seen
       var remainder_chat : String , // if "both" : { message : time , .... } , "me" : { message : time , .... }
       var  photos : String , // {path1 in gallary , path2 , path3}
       var vedios : String , // same as photos
       var Link : String , // Link as string {, , , , }
       var Sticker : String , // path of galary inj json
       var Document : String , // path in file_manager stolred in json
       var groups_joined : String // Json of {group_number : {restricted_person, , , } , ....}
    ) : universal_model()

    data class photo_model(
        var path : String
    ) : universal_model()

   data class audio_model(
       var path : String
   ) : universal_model()

    data class link_model(
        var path : String
    ) : universal_model()

    data class front_contact_msg(
        var _name_ : String ,
        var  last_msg : String,
        var time : String ,
        var number_ : String,
        var private_chat : Boolean ,     // this conforms either to give the fingerprint authentication or not
        var pair_chat : Boolean ,        // if(group) then contains only individual private chat
        var display_picture : String     // display picture of that person or group
    ) :  universal_model()

    data class get_my_account(
      var _name : String,
      var _number : String,
      var about : String,
      var dp : String
   ) : universal_model()

    data class select_msg(
        var total : Int =0,
         var _me : Int =0,
        var _other: Int =0,
        var _tamplate : Int = 0
    ) : universal_model()

    data class time_(
        var hours_ : String,    // this is fro hours input
        var minute_ : String,   // this is for minute input
        var _indi : String     // this one is for AM or PM
    ) : universal_model()

    data class remainder_store(
        var satus : String ,      // for the order fro : "all" , "me" , "none"
        var time_ : String        // stringify version of time (gours and minute)
    )
}