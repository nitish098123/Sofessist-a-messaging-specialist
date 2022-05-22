package com.example.database_part_3.db

/*
    created by Nitish Kr Boro on 4/3/2022
*/

// this database table store all chat universally and allowed to retrive message for each perticular purpose
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.database_part_3.groups.select_contact_list_model
import com.example.database_part_3.model.universal_model.one_chat_property
import com.example.database_part_3.model.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.internal.addHeaderLenient
import java.util.function.DoubleBinaryOperator

val my_number : Long = 6900529357
class universal_chat_store(val context_ : Context, val factory : SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context_ , DATABASE_NAME,factory, DATABASE_VERSION){

    private val mapper = jacksonObjectMapper()

    companion object{
        // for saving chats prooerties into the table
        const val DATABASE_NAME = "SOFASSIST"
        const val DATABASE_VERSION = 3
        const val TABLE_NAME_UNIVERSAL_CHAT = "UNIVERSAL_CHAT_STORE"
        const val ID_COL = "ID"
        const val PAIR = "PAIR"
        const val MESSAGE_NUMBER = "message_number"
        const val DATA = "chat_data"
        const val NAME = "NAME_"
        const val CATEGORY = "category"
        const val READ = "read"
        const val DELETE = "chat_delete_or_not"
        const val TIME_ = "time"
        const val REMAINDER = "remainder"
        const val LOCK = "_lock"
        const val FROM = "_from_"
        const val TO = "_to_"
        const val REPLIED_MESSAGE="replied_msg"
        const val FORWARDED_MSG = "FORWARDED_MESSAGE"
        const val STARED_MSG_PROPERTY = "STARED_MSG"
        const val EDIT_REWRITE = "EDIT_AND_REWRITE"
        const val TAMPLATE_MSG = "TAMPLATE_MSG"

        // Informations saving about another person
        const val TABLE_NAME_PERSON_INFO="PERSON_INFO"
        const val CONTACT_NAME="CONTACT_NAME_"
        const val NUMBER_ = "CONTACT_NUMBER_"
        const val ID_COL_PERSON_INFO ="ID_"
        const val DP = "DISPLAY_PICTURE_"
        const val ABOUT = "about_"
        const val WALLPAPER = "WALLPAPER_"
        const val ARCHIVED = "ARCHIVED_"
        const val BLOCKED ="BLOCKED_"
        const val STARED_MESSAGE = "stared_message_"
        const val PHOTOS = "photos_"
        const val VIDEOS = "videos_"
        const val LINK = "link_"
        const val STICKER = "stickers_"
        const val DOCUMENTS="documents_"
        const val MUTE_ = "mute_"
        const val PIN_CONTACT = "pin_contact_"
        const val LAST_MESSAGE_NUMBER_SEEN = "recent_message_number_seen"
        const val REMAINDER_IN_PERSON_INFO = "remiander_chat_indexes_"
        const val LAST_MESSAGE_ARRIVED = "RECENT_MESSAGE_ARRIVED"
        const val GOUP_JOINED = "GROUP_JOINED_WITH_RESTRICT_USERS"
        const val PRIVATE_CHAT = "private_chats"
        const val SAVE_TO_GALLERY = "SAVE_TO_GALLERY"     // if "true" then save medias to gallery and if "false" then dont sabe to the galley
        const val LAST_MSG_ARRIVED_MSG_NUMBER = "_last_msg_number"

        // my account
        const val MY_ACCOUNT_TABLE ="MY_ACCOUNT_TABLE"
        const val MY_ACCOUNT_ID = "my_account_id"
        const val MY_NAME = "my_name"
        const val  MY_ABOUT = "my_about"
        const val MY_NUMBER = "MY_NUMBER"
        const val MY_DP = "MY_DP"

        //Group info
        const val GROUP_INFO_TABLE = "_GROUP_INFO_TABLE"
        const val TOTAL_GROUP_COL_NUM = "total_group_id_col"
        const val GROUP_NUMBER = "_group_number_"
        const val ADMIN = "_ADMIN_"
        const val MEMBER_ = "_MEMBERS_"
        const val PRIVATE_GROUP = "_PRIVATE_GROUP_"
        const val DESCRIPTION_ = "_description_"
        const val GROUP_NAME = "_group_name"
        const val GROUP_DP = "_GROUP_DP_"
        const val ALLOW_CHAT_IN_GROUP_MEMBERS = "_allow_chat_in_group"
        const val MUTE_SPECIFIC_PERSON = "mute_specific_person"
        const val GROUP_ARCHIVED  = "_group_archived"
        const val GROUP_WALLPAPER = "_group_wallpaper"
        const val MUTE_GROUP = "_mute_group"

        // group_one_chat_property
        const val GROUP_ONE_CHAT_PROPERTY = "GROUP_ONE_CHAT_PROPERTY_"
        const val ID_COL_ = "ID"
        const val GROUP_NUMBER_CHAT = "group_number"
        const val MESSAGE_NUMBER_ = "message_number"
        const val DATA_ = "chat_data"
        const val GROUP_NAME_CHAT = "NAME_"
        const val CATEGORY_ = "category"
        const val READ_ = "read"            // "arrayList<String,string>"  persons_name , read_time
        const val DELETE_ = "chat_delete_or_not"
        const val TIME_GROUP_CHAT = "time"
        const val REMAINDER_ = "remainder"
        const val FROM_ = "_from_"
        const val TO_ = "_to_"
        const val REPLIED_MESSAGE_ = "replied_msg"
        const val FORWARDED_MSG_ = "FORWARDED_MESSAGE"
        const val STARED_MSG_PROPERTY_ = "STARED_MSG"
        const val EDIT_REWRITE_ = "EDIT_AND_REWRITE"
        const val TAMPLATE_MSG_ = "TAMPLATE_MSG"
    }

    override fun onCreate(db: SQLiteDatabase){
        // inside the query you should give allways space before and after comma
        val query = ("CREATE TABLE "+ TABLE_NAME_UNIVERSAL_CHAT +" ("
                 + ID_COL +" INTEGER PRIMARY KEY, " + PAIR + " TEXT, " +
                 MESSAGE_NUMBER + " TEXT, "+ DATA + " TEXT, " + NAME + " TEXT, " +
                 CATEGORY+" TEXT, "+READ+" TEXT, "+ DELETE+" TEXT, " + TIME_+" TEXT, "+
                 REMAINDER + " TEXT, "+ LOCK + " TEXT, " + FROM+" TEXT, "+TO+" TEXT, "+ REPLIED_MESSAGE+" TEXT, "+ FORWARDED_MSG +
                  " TEXT, "+ STARED_MSG_PROPERTY + " TEXT, "+ EDIT_REWRITE+" TEXT, "+ TAMPLATE_MSG + " TEXT);") // In name of table columns must not be the Keyword sensitive otherwise sql will confuse

        val query2 = ("CREATE TABLE "+ TABLE_NAME_PERSON_INFO +" ("
                + ID_COL_PERSON_INFO +" INTEGER PRIMARY KEY, " + CONTACT_NAME +" TEXT, "+ NUMBER_ +" TEXT, "+
                DP + " TEXT, "+ ABOUT + " TEXT, " + WALLPAPER +" TEXT, "+ ARCHIVED +" TEXT, "+
                BLOCKED +" TEXT, " + STARED_MESSAGE +" TEXT, "+ LINK + " TEXT, "+
                PHOTOS +" TEXT, "+ VIDEOS +" TEXT, "+ STICKER + " TEXT, "+ DOCUMENTS +" TEXT, "+
                MUTE_ +" TEXT, "+ PIN_CONTACT +" TEXT, "+ LAST_MESSAGE_NUMBER_SEEN +" TEXT, "+
                REMAINDER_IN_PERSON_INFO +" TEXT, "+ LAST_MESSAGE_ARRIVED +" TEXT, " +GOUP_JOINED+
                " TEXT, "+ PRIVATE_CHAT + " TEXT, " + SAVE_TO_GALLERY +" TEXT, "+ LAST_MSG_ARRIVED_MSG_NUMBER + " TEXT);")

        val query3 = ("CREATE TABLE "+ MY_ACCOUNT_TABLE +" (" + MY_ACCOUNT_ID+" INTEGER PRIMARY KEY, "+ MY_NAME +
                       " TEXT, "+ MY_ABOUT +" TEXT, "+ MY_NUMBER + " TEXT, "+ MY_DP+" TEXT);")

        val query4 = ("CREATE TABLE "+ GROUP_INFO_TABLE +" (" + TOTAL_GROUP_COL_NUM + " INTEGER PRIMARY KEY, "+ GROUP_NUMBER +
                    " TEXT, "+ ADMIN+" TEXT, "+ MEMBER_+" TEXT, "+ PRIVATE_GROUP +" TEXT, "+ DESCRIPTION_+" TEXT, "+
                    GROUP_NAME+" TEXT, "+ GROUP_DP+" TEXT, "+ ALLOW_CHAT_IN_GROUP_MEMBERS+" TEXT, "+ MUTE_SPECIFIC_PERSON+
                   " TEXT, "+ GROUP_ARCHIVED+" TEXT, "+ GROUP_WALLPAPER+" TEXT, "+ MUTE_GROUP+ " TEXT);")

        val query5 = ("CREATE TABLE "+ GROUP_ONE_CHAT_PROPERTY +" (" + ID_COL_ +" INTEGER PRIMARY KEY, " + GROUP_NUMBER_CHAT + " TEXT, " +
                MESSAGE_NUMBER_ + " TEXT, "+ DATA_ + " TEXT, " + GROUP_NAME_CHAT + " TEXT, " +
                CATEGORY_ +" TEXT, "+ READ_ +" TEXT, "+ DELETE_+" TEXT, " + TIME_GROUP_CHAT+" TEXT, "+
                REMAINDER_ + " TEXT, " + FROM_+" TEXT, "+ TO_+" TEXT, "+ REPLIED_MESSAGE_ +" TEXT, "+ FORWARDED_MSG_ +
                " TEXT, "+ STARED_MSG_PROPERTY_ + " TEXT, "+ EDIT_REWRITE_ +" TEXT, "+ TAMPLATE_MSG_ + " TEXT);")

        db.execSQL(query)
        db.execSQL(query2)
        db.execSQL(query3)
        db.execSQL(query4)
        db.execSQL(query5)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UNIVERSAL_CHAT)            // Remember that "EXISTS" spell must be correct
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PERSON_INFO)
        db.execSQL("DROP TABLE IF EXISTS " + MY_ACCOUNT_TABLE)
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_INFO_TABLE)
        db.execSQL("DROP TABLE IF EXISTS " + GROUP_ONE_CHAT_PROPERTY)
        onCreate(db)
    }

    fun save_message( pair : String, msg_num : String ,data : String,
                      category_ : String , read : Boolean, _delete : String,
                      _time : String,remainder : String ,
                      lock : Boolean , from : Long , to : String ,
                      replied_msg : String, forwarded_msg : String, stared : Boolean,
                      edit_msg : Boolean , tamplate_ : String , name_to : String) : Boolean {   // adding all messages to sql database
        val values = ContentValues()
        values.put(PAIR,pair)
        values.put(MESSAGE_NUMBER,msg_num)
        values.put(DATA,data)
        values.put(NAME,name_to)
        values.put(CATEGORY,category_)
        values.put(READ,"${read}")
        values.put(DELETE,_delete)
        values.put(TIME_,_time)
        values.put(REMAINDER,remainder)
        values.put(LOCK,"${lock}")
        values.put(FROM,"${from}")
        values.put(TO,to)
        values.put(REPLIED_MESSAGE,replied_msg)
        values.put(FORWARDED_MSG,forwarded_msg)
        values.put(STARED_MSG_PROPERTY,"${stared}")
        values.put(EDIT_REWRITE,"${edit_msg}")
        values.put(TAMPLATE_MSG,"${tamplate_}")


        var status : Boolean = true
        val db = this.writableDatabase     // accessing database for writting data
        try{
            // take the index of row of saved message
             db.insert(TABLE_NAME_UNIVERSAL_CHAT,null,values)
             status = true

            Log.d("","SSSSSSSSSAVE message : ${name_to} & messag_number : ${msg_num} & message is : ${data}")
            if(msg_num!="1"){      // Now also updating the last message to the persons info table
                val DB = this.writableDatabase
                val sql_string = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${LAST_MESSAGE_ARRIVED}=('${data}') , $LAST_MSG_ARRIVED_MSG_NUMBER=('$msg_num') WHERE ${NUMBER_}='${to}';"
                DB.execSQL(sql_string)
                DB.close()
            }
            if(msg_num=="1"){     // first time saving the message in sqlite database  into the person info  datanase part
                save_person_info(name_to,to,"","","",false,false,"","","","","","",
                    false,false,0,"","",false,true,msg_num)

                /* Now request for downloading bitmap of profile pic from server  */
            }
        }
        catch(e : Error){
            status = false
        }
        db.close()
        return status
    }


    // if operator="message" then all messages will collect
    // if operator="star" then star_msg will return
    // if operator="remainder"

    fun get_messages(pair : String , operator_: String) : ArrayList<universal_model.one_chat_property>{
        // the index_str is ()_()_()_()_() form

        val db = this.readableDatabase
        val _store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()

        var msg_num : String
        var data : String = ""
        var category_ : String
        var read : Boolean
        var _delete : String
        var _time : String
        var remainder : String
        var lock : Boolean
        var from : Long
        var to : Long
        var pair_ : String
        var star_msg : Boolean
        var edit_msg : Boolean
        var replay_msg : String
        var forward_msg : String
        var template : String
        var _name : String


            try {
                var str : String = "SELECT * FROM $TABLE_NAME_UNIVERSAL_CHAT WHERE $PAIR='$pair';"  // for message selections as default
                if(operator_=="star"){
                    str = "SELECT * FROM $TABLE_NAME_UNIVERSAL_CHAT WHERE $STARED_MSG_PROPERTY='true';"
                }
                if(operator_=="remainder"){
                    str = "SELECT * FROM $TABLE_NAME_UNIVERSAL_CHAT WHERE $REMAINDER!='none';"
                }
                val cursor : Cursor = db.rawQuery(str,null)
                if(cursor.count>0){
                    cursor.moveToFirst()
                    do {
                          msg_num =  cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_NUMBER))
                          pair_ = cursor.getString(cursor.getColumnIndexOrThrow(PAIR))
                          category_ = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY))
                          read = cursor.getString(cursor.getColumnIndexOrThrow(READ)).toBoolean()
                          _delete = cursor.getString(cursor.getColumnIndexOrThrow(DELETE))
                          _time = cursor.getString(cursor.getColumnIndexOrThrow(TIME_))
                          remainder = cursor.getString(cursor.getColumnIndexOrThrow(REMAINDER))
                          lock = cursor.getString(cursor.getColumnIndexOrThrow(LOCK)).toBoolean()
                          from = cursor.getString(cursor.getColumnIndexOrThrow(FROM)).toLong()
                          to = cursor.getString(cursor.getColumnIndexOrThrow(TO)).toLong()
                          data = cursor.getString(cursor.getColumnIndexOrThrow(DATA))
                          star_msg = cursor.getString(cursor.getColumnIndexOrThrow(STARED_MSG_PROPERTY)).toBoolean()
                          edit_msg = cursor.getString(cursor.getColumnIndexOrThrow(EDIT_REWRITE)).toBoolean()
                          replay_msg = cursor.getString(cursor.getColumnIndexOrThrow(REPLIED_MESSAGE))
                          forward_msg = cursor.getString(cursor.getColumnIndexOrThrow(FORWARDED_MSG))
                          template = cursor.getString(cursor.getColumnIndexOrThrow(TAMPLATE_MSG))
                          _name = cursor.getString(cursor.getColumnIndexOrThrow(NAME))

                        Log.d("","gettttttting message of name:${_name} & message:${data} pair_db=${pair_},msg_num:${msg_num} & provided_pair=${pair} & template:${template}")

//        if(pair_ == pair){      // this will save the message property from the database to show
        _store.add(universal_model.one_chat_property(pair, _name, msg_num, data, category_, read, _delete, _time, edit_msg, star_msg, template, remainder, lock, from, to, replay_msg, forward_msg))
//        }
        } while(cursor.moveToNext())
        }
            db.close()
        }

            catch (e: Error) {
                println("error in querying the database")
            }
      return  _store
    }

    fun save_person_info( name : String,
                          _number: String,
                          dp : String,
                          about : String,
                          wallpaper : String,
                          archived : Boolean,
                          blocked : Boolean,
                          stared_message : String,
                          photos : String,
                          videos : String,
                          link : String,
                          stickers : String,
                          documents : String,
                          mute : Boolean,
                          pin_contact : Boolean,
                          last_message_seen_number : Int,
                          remainder_chat_indexes : String,
                          last_message_arrived : String,
                          private_chats : Boolean,
                          save_to_gallery : Boolean,
                          last_msg_arrived_msg_number : String
                          ) : Boolean {  // this data will directly bring from server
        val values = ContentValues()
        values.put(CONTACT_NAME,"${name}")
        values.put(NUMBER_,"${_number}")
        values.put(DP,"${dp}")
        values.put(ABOUT,about)
        values.put(WALLPAPER,wallpaper)
        values.put(ARCHIVED,"${archived}")
        values.put(BLOCKED,"${blocked}")
        values.put(STARED_MESSAGE,stared_message)
        values.put(PHOTOS,photos)
        values.put(VIDEOS,videos)
        values.put(LINK,"${link}")
        values.put(STICKER,stickers)
        values.put(DOCUMENTS,documents)
        values.put(MUTE_,"${mute}")
        values.put(PIN_CONTACT,"${pin_contact}")
        values.put(LAST_MESSAGE_NUMBER_SEEN,"${last_message_seen_number}")
        values.put(REMAINDER_IN_PERSON_INFO,remainder_chat_indexes)
        values.put(LAST_MESSAGE_ARRIVED,last_message_arrived)
        values.put(PRIVATE_CHAT,"$private_chats")
        values.put(SAVE_TO_GALLERY,"$save_to_gallery")
        values.put(LAST_MSG_ARRIVED_MSG_NUMBER,last_msg_arrived_msg_number)

        val db = this.writableDatabase     // accessing database for writting data
        try{
            db.insert(TABLE_NAME_PERSON_INFO,null,values)
            return  true
        } catch(e : Exception) {
            return false
        }
        db.close()
    }

    // when we user click into the toolbar of chat activity
    fun get_persons_info(person_number : String) : persons_info_last_msg{
        val db = this.readableDatabase
        var dp = ""
        var private_chat : String = ""
        var store : persons_info_last_msg = persons_info_last_msg("",false,true)
        var save_to_gallery_ = ""
        try {
            val cursor : Cursor = db.rawQuery("SELECT * FROM ${TABLE_NAME_PERSON_INFO} WHERE $NUMBER_='$person_number';", null)
            if(cursor.count>0){
                cursor.moveToFirst()
                do {
                     dp = cursor.getString(cursor.getColumnIndexOrThrow(DP))
                     private_chat = cursor.getString(cursor.getColumnIndexOrThrow(PRIVATE_CHAT))
                     save_to_gallery_ = cursor.getString(cursor.getColumnIndexOrThrow(SAVE_TO_GALLERY))     // permission to save gallery
                    /* get persons all information */

                    store = persons_info_last_msg(dp,private_chat.toBoolean(),save_to_gallery_.toBoolean())
                } while (cursor.moveToNext())
            }
            db.close()

        } catch ( e : Exception) {
            Log.d("?????????Error in ","searching name in table")
        }
      return store
    }

    // get persons dp,name,number
    fun get_person_dp_name_number() : HashMap<String,String>{
        val db = this.readableDatabase
        var dp = ""
        var number_ : String = ""
        var store  =  HashMap<String,String>()   // (number,dp)
        try {
            val cursor : Cursor = db.rawQuery("SELECT $CONTACT_NAME, $NUMBER_, $DP FROM ${TABLE_NAME_PERSON_INFO};", null)
            if(cursor.count>0){
                cursor.moveToFirst()
                do {
                    dp = cursor.getString(cursor.getColumnIndexOrThrow(DP))
                    number_ = cursor.getString(cursor.getColumnIndexOrThrow(NUMBER_))

                   store[number_] = dp
                } while (cursor.moveToNext())
            }
            db.close()

        } catch ( e : Exception) {
            Log.d("?????????Error in ","searching name in table")
        }
        return store
    }

    // this will getback all the contacts that you chat with in past or trying to chat with -> direct connections to home page
    fun get_past_used_contact() : ArrayList<universal_model.front_contact_msg>{
        val db = this.readableDatabase
        var store : ArrayList<universal_model.front_contact_msg> = ArrayList<universal_model.front_contact_msg>()
        var check_name : String = ""
        var last_msg : String = ""         // stores the last message arrived
        var number_ : String = ""
        var private_chat : Boolean = false

        try {
            val cursor : Cursor = db.rawQuery("SELECT * FROM ${TABLE_NAME_PERSON_INFO};", null)
            if(cursor.count>0){
                cursor.moveToFirst()
                do {
                    check_name = cursor.getString(cursor.getColumnIndexOrThrow(CONTACT_NAME))
                    last_msg = cursor.getString(cursor.getColumnIndexOrThrow(LAST_MESSAGE_ARRIVED))
                    number_ = cursor.getString(cursor.getColumnIndexOrThrow(NUMBER_))
                    private_chat = cursor.getString(cursor.getColumnIndexOrThrow(PRIVATE_CHAT)).toBoolean()

                    store.add(universal_model.front_contact_msg(check_name,last_msg,"3:00pm",number_,private_chat))
                } while (cursor.moveToNext())
            }
            db.close()

        }catch ( e : Exception){
            Log.d("?????????Error in ","searching name in table")
        }
        return store
    }

    // one time settings for the others person
    fun update_persons_info(operators: String, value : String, contact_number: String){
//        UPDATE ORDERTABLE SET QUANTITY = (INSERT VALUE OF YOUR EDIT TEXT) WHERE NAME =   'Order2'

        try {

            val DB = this.writableDatabase
            var sql_query = ""
            if (operators == "mute") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${MUTE_}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "about") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${ABOUT}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "wallpaper") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${WALLPAPER}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "archived") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${ARCHIVED}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "blocked") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${BLOCKED}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "pinned") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${PIN_CONTACT}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "last_time_seen_message") {
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${LAST_MESSAGE_NUMBER_SEEN}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
            }
            if (operators == "last_msg_arrived") {
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${LAST_MESSAGE_ARRIVED}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
                Log.d("!!!!!!!!!!!!!", "updated lst_msg")
            }
            if(operators=="private_chat"){
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${PRIVATE_CHAT}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
                Log.d("","uuuuuuuuuuupdaing private chat to:$value")
            }
            if(operators=="save_to_gallery"){
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${SAVE_TO_GALLERY}='${value}' WHERE ${NUMBER_}=${contact_number};"
                DB.execSQL(sql_query)
                Log.d("","ggggggggellery save_to_gallery to:$value")
            }
              DB.close()
        }  catch (e : Exception){
            Log.d("error in ","updating last arrived msg")
        }
    }

    // saving the details of My account for the first time
    fun save_to_my_account(name : String ,
                           dp_path : String,
                           about : String,
                           number_ : String) {
        val db = this.writableDatabase
        val vl = ContentValues()

            vl.put(MY_NAME,name)
            vl.put(MY_NUMBER,number_)
            vl.put(MY_ABOUT,about)
            vl.put(MY_DP,dp_path)

        db.insert(MY_ACCOUNT_TABLE,null,vl)
        db.close()
    }

    fun get_my_account() : universal_model.get_my_account?{
        val db = this.readableDatabase
        var store : universal_model.get_my_account? = null
        var name_ = ""
        var number_ = ""
        var about = ""
        var dp_ = ""
        try{
            val cursor : Cursor = db.rawQuery("SELECT *FROM ${MY_ACCOUNT_TABLE};", null)
            if(cursor.count>0){
                cursor.moveToFirst()
                do {
                    name_ = cursor.getString(cursor.getColumnIndexOrThrow(MY_NAME))
                    about = cursor.getString(cursor.getColumnIndexOrThrow(MY_ABOUT))
                    number_ = cursor.getString(cursor.getColumnIndexOrThrow(MY_NUMBER))
                    dp_ = cursor.getString(cursor.getColumnIndexOrThrow(MY_DP))

                    store = universal_model.get_my_account(name_,number_,about,dp_)

                    Log.d("@@@@@@MY_name_get:${name_}","${about}")
                } while (cursor.moveToNext())
            }
        }catch (e: Exception){
           Log.d("error in finding",": account")
        }
        db.close()
       return  store
    }

    fun update_my_account(operators : String , value : String){
        val db = this.writableDatabase
        var sql = ""

        if(operators=="name"){
            sql = "UPDATE ${MY_ACCOUNT_TABLE} SET ${MY_NAME}=('${value}') WHERE ${MY_ACCOUNT_ID}='1';"
        }
        if(operators=="number"){
            sql = "UPDATE ${MY_ACCOUNT_TABLE} SET ${MY_NUMBER}=('${value}') WHERE ${MY_ACCOUNT_ID}='1';"
        }
        if(operators=="about"){
            sql = "UPDATE ${MY_ACCOUNT_TABLE} SET ${MY_ABOUT}=('${value}') WHERE ${MY_ACCOUNT_ID}='1';"
        }
        if(operators=="dp"){
            sql = "UPDATE ${MY_ACCOUNT_TABLE} SET ${MY_DP}=('${value}') WHERE ${MY_ACCOUNT_ID}='1';"
        }

        db.execSQL(sql)
        db.close()
    }

    fun update_one_chat_property(operators_ : String, pair_ : String , msg_numbers : ArrayList<String> , new_value : String){
      // the  correct form of the sql update in the database is : UPDATE TABLE_NAME SET COLUMN = ('${new_value}') WHERE ${another_column}='${value};'
        val db = this.writableDatabase
        var sql_query = ""
        for(i in msg_numbers){
            if (operators_ == "star"){           // till now this is correct
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${STARED_MSG_PROPERTY}=('${new_value}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='${i}';"
                Log.d("^^^^star msg_num:","${i},value ${new_value}")
                db.execSQL(sql_query)
            }
            if (operators_ == "remainder"){
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${REMAINDER}=('${new_value}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='${i}';"
                Log.d("@@@@@@@@remiander","activated value :${new_value}")
                db.execSQL(sql_query)
            }
            if (operators_ == "lock_message"){
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${LOCK}=('${new_value}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='${i}';"
                db.execSQL(sql_query)
                Log.d("^^^^lock_msg_num","${i},value ${new_value}")
            }
        }
        db.close()
    }

    fun delete_specific_message_number(pair : String ,msg_number : ArrayList<String>){
        val DB = this.writableDatabase
        var sql_string = ""
        for(k in msg_number){
            sql_string = "DELETE FROM ${TABLE_NAME_UNIVERSAL_CHAT} WHERE $PAIR='${pair}' AND ${MESSAGE_NUMBER}='${k+1}';"
           Log.d("","dddddddddddeleting of pair:${pair} & msg_number: ${k+1}")
        }
        DB.execSQL(sql_string)
//      DB.delete("$TABLE_NAME_UNIVERSAL_CHAT","${MESSAGE_NUMBER}='${msg_number}'",null)
        DB.close()
    }

    fun forward_funciton(message_to_forward : ArrayList<one_chat_property> , selected_persons : ArrayList<String>){   // selected persons are teh array of mobile numbers
        val db = this.readableDatabase
        var mobile_number : String = ""
        val container_msg_num = HashMap<String,Int>()    // key-> mobile number , value->last_msg_number

        for(_number in selected_persons){
            val cursor: Cursor = db.rawQuery("SELECT ${LAST_MSG_ARRIVED_MSG_NUMBER} FROM ${TABLE_NAME_PERSON_INFO} WHERE ${NUMBER_}='${_number}';",null)
            Log.d("Size of cursor?????", "${cursor.count}")

            if (cursor.count > 0){
                cursor.moveToFirst()
                do {
                    mobile_number = cursor.getString(cursor.getColumnIndexOrThrow(PAIR))
                    container_msg_num[mobile_number] = mobile_number.toInt()
                } while (cursor.moveToNext())
            }
            if(cursor.count==0){     // this means this is forward message is the first time message delivered
                container_msg_num[mobile_number]=0
                Toast.makeText(context_,"This forward message is first time message!!",Toast.LENGTH_LONG).show()
            }
        }
        db.close()

        // now for the saving the data part to database
        var pair : String = ""

        for(i in selected_persons){
            if(i.toLong() > my_number) pair="${i}|$my_number"
            if(i.toLong() < my_number) pair="${my_number}|${i}"
            val time_ = "${System.currentTimeMillis()}"

              // now for every messages
            for(message_ in message_to_forward){
                var msg_num = container_msg_num[i]!!
                msg_num++
                save_message(pair, "$msg_num",message_.data,message_.category,true,"none",time_,"none",
                    false,my_number,i,"none","yes",false,false,message_.template,"")
            }
        }
    }

    // for template update in data base
    /*  if operator is comment then datatype of comment must be comment */
    fun update_reaction_template(operator_ : String , new_value : String , pair_: String , msg_number: String){

        if(operator_=="you_liked" || operator_=="you_comment"){       // this simply update the new edited value
            val DB = this.writableDatabase
            val new_data = "$new_value"
            val sql_string = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${DATA}=('${new_data}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='$msg_number';"
            DB.execSQL(sql_string)
            DB.close()
            Log.d("","pppppppupdating the new value of reacitons template of:$new_value")
        }


        // this comes from the server
        if(operator_!="you_liked"){
            var new_data: String = ""
            val db = this.readableDatabase
            var _string = "SELECT $DATA FROM $TABLE_NAME_UNIVERSAL_CHAT WHERE $PAIR='$pair_' AND $MESSAGE_NUMBER='$msg_number';"
            val cursor: Cursor = db.rawQuery(_string, null)
            var template_data : String = ""
            if (cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    template_data = cursor.getString(cursor.getColumnIndexOrThrow(DATA))
                } while (cursor.moveToNext())
            }
            db.close()

            var data: reaction_store_model = mapper.readValue<reaction_store_model>(template_data)    // parsing data that arrived from the database

            if (operator_ == "new_comment_server"){                                          // whenever new comment is posted into one topic
                val comment_: universal_model.one_chat_property = mapper.readValue<universal_model.one_chat_property>(new_value)      // new comment that should be posted is parsed here
                data.total_comment.add(comment_)
                new_data = mapper.writeValueAsString(data)       // this is complitly updated data
            }
            if (operator_ == "new_like_server") {              // whenever server pulls the total like of topic
                 var total_ = data.total_like
                 total_++
                 data.total_like = total_
                new_data = mapper.writeValueAsString(data)
            }

            val DB = this.writableDatabase
            val sql_string = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${DATA}=('${new_data}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='$msg_number';"
            DB.execSQL(sql_string)
            DB.close()

            Log.d("", "nnnnnnnnnnnnnew data from storing reaction to update is:${new_data}")
        }
    }

    // update the voting template
    fun update_voting_template(new_value : String , pair_ : String , msg_number: String){
        val DB = this.writableDatabase
        val sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${DATA}=('${new_value}') WHERE ${PAIR}='${pair_}' AND ${MESSAGE_NUMBER}='${msg_number}';"
        DB.execSQL(sql_query)
        DB.close()
    }



    // Now for groups

    // saving information of specific group
    fun save_group_info(
        _group_number : String,
        _admin : ArrayList<select_contact_list_model>,      // string of Arraylist
        _members : ArrayList<select_contact_list_model>,   // string of numbers of ArrayList<>
         _private : Boolean,   // true-> cannot take screen shoot , forward copy etc
        descriptions : String,
        _group_name : String,
         _dp : String,         // string of json of-> _link: server_link, uri : URI of image in local device
         _can_message : ArrayList<select_contact_list_model>, // arrayList of all allowed members + with '_outer_person_'
         mute_group : Boolean,
         mute_specific : ArrayList<String>,  // list of all persons who you don't want notification
        _archived_group : Boolean,
        _group_wallpaper : String
    ){

        var values = ContentValues()
        values.put(GROUP_NUMBER,_group_number)
        values.put(ADMIN,mapper.writeValueAsString(_admin))     // list of group admin
        values.put(MEMBER_,mapper.writeValueAsString(_members))
        values.put(PRIVATE_GROUP,_private.toString())
        values.put(DESCRIPTION_,descriptions)
        values.put(GROUP_NAME,_group_name)
        values.put(GROUP_DP,_dp)
        values.put(ALLOW_CHAT_IN_GROUP_MEMBERS,mapper.writeValueAsString(_can_message))
        values.put(MUTE_GROUP,mute_group.toString())
        values.put(MUTE_SPECIFIC_PERSON,mapper.writeValueAsString(mute_specific))
        values.put(GROUP_ARCHIVED,_archived_group.toString())
        values.put(GROUP_WALLPAPER,_group_wallpaper)

        val db = this.writableDatabase     // accessing database for writting data
        try {
            // take the index of row of saved message
            db.insert(GROUP_INFO_TABLE, null, values)
            Toast.makeText(context_,"A new group is created",Toast.LENGTH_LONG).show()
        } catch (e: Error){
            Log.d("","eeeeeeeeeror in saving of data of group: $e")
        }
    }


    fun save_group_message(){

    }
}
