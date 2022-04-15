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
import com.example.database_part_3.model.universal_model


class universal_chat_store(context :Context, factory:SQLiteDatabase.CursorFactory?):
    SQLiteOpenHelper(context, DATABASE_NAME,factory, DATABASE_VERSION){

    companion object{

        // for saving chats prooerties into the table
        const val DATABASE_NAME = "SOFASSIST"
        const val DATABASE_VERSION = 3
        const val TABLE_NAME_UNIVERSAL_CHAT = "UNIVERSAL_CHAT_STORE"
        const val ID_COL = "ID"
        const val PAIR = "PAIR"
        const val MESSAGE_NUMBER = "message_number"
        const val DATA = "chat_data"
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


        // my account
        const val MY_ACCOUNT_TABLE ="MY_ACCOUNT_TABLE"
        const val MY_ACCOUNT_ID = "my_account_id"
        const val MY_NAME = "my_name"
        const val  MY_ABOUT = "my_about"
        const val MY_NUMBER = "MY_NUMBER"
        const val MY_DP = "MY_DP"
    }

    override fun onCreate(db: SQLiteDatabase){
        // inside the query you should give allways space before and after comma
        val query = ("CREATE TABLE "+ TABLE_NAME_UNIVERSAL_CHAT +" ("
                 + ID_COL +" INTEGER PRIMARY KEY, " + PAIR + " TEXT, " +
                 MESSAGE_NUMBER + " TEXT, "+ DATA + " TEXT, " +
                 CATEGORY+" TEXT, "+READ+" TEXT, "+
                 DELETE+" TEXT, " + TIME_+" TEXT, "+
                 REMAINDER + " TEXT, "+ LOCK + " TEXT, "+FROM+" TEXT, "+TO+" TEXT, "+ REPLIED_MESSAGE+" TEXT, "+ FORWARDED_MSG +
                  " TEXT, "+ STARED_MSG_PROPERTY + " TEXT, "+ EDIT_REWRITE+" TEXT, "+ TAMPLATE_MSG + " TEXT);") // In name of table columns must not be the Keyword sensitive otherwise sql will confuse

        val query2 = ("CREATE TABLE "+ TABLE_NAME_PERSON_INFO +" ("
                + ID_COL_PERSON_INFO +" INTEGER PRIMARY KEY, " + CONTACT_NAME +" TEXT, "+ NUMBER_ +" TEXT, "+
                DP + " TEXT, "+ ABOUT + " TEXT, " + WALLPAPER +" TEXT, "+ ARCHIVED +" TEXT, "+
                BLOCKED +" TEXT, " + STARED_MESSAGE +" TEXT, "+ LINK + " TEXT, "+
                PHOTOS +" TEXT, "+ VIDEOS +" TEXT, "+ STICKER + " TEXT, "+ DOCUMENTS +" TEXT, "+
                MUTE_ +" TEXT, "+ PIN_CONTACT +" TEXT, "+ LAST_MESSAGE_NUMBER_SEEN +" TEXT, "+
                REMAINDER_IN_PERSON_INFO +" TEXT, "+ LAST_MESSAGE_ARRIVED +" TEXT, " +GOUP_JOINED+ " TEXT);")

        val query3 = ("CREATE TABLE "+ MY_ACCOUNT_TABLE +" (" + MY_ACCOUNT_ID+" INTEGER PRIMARY KEY, "
                + MY_NAME +" TEXT, "+ MY_ABOUT +" TEXT, "+ MY_NUMBER + " TEXT, "+ MY_DP+" TEXT);")

        db.execSQL(query)
        db.execSQL(query2)
        db.execSQL(query3)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_UNIVERSAL_CHAT)            // Remember that "EXISTS" spell must be correct
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PERSON_INFO)
        db.execSQL("DROP TABLE IF EXISTS " + MY_ACCOUNT_TABLE)
        onCreate(db)
    }

    fun save_message( pair : String, msg_num : String ,data : String,
                      category_ : String , read : Boolean, _delete : String,
                      _time : String,remainder : String ,
                      lock : Boolean , from : Long , to : String ,
                      replied_msg : String, forwarded_msg : String, stared : Boolean,
                      edit_msg : Boolean , tamplate_ : String
                      ) : Boolean {   // adding all messages to sql database
        val values = ContentValues()
        values.put(PAIR,pair)
        values.put(MESSAGE_NUMBER,msg_num)
        values.put(DATA,data)
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
             db.close()
             Log.d("Saving data pairs>>>",pair)
             status = true
        } catch(e : Error) {
            status = false
        }
      return status
    }


    fun get_messages(pair : String) : ArrayList<universal_model.one_chat_property>{
        // the index_str is ()_()_()_()_() form

        val db = this.readableDatabase
        val _store : ArrayList<universal_model.one_chat_property> = ArrayList<universal_model.one_chat_property>()


        var msg_num : String
        var data : String
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


            try {
                val cursor : Cursor = db.rawQuery("SELECT * FROM ${TABLE_NAME_UNIVERSAL_CHAT};", null)
                Log.d("Size of cursor?????","${cursor.count}")
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

                          if(pair_== pair) {      // this will save the message property from the database to show
                              _store.add(universal_model.one_chat_property(
                                      msg_num,
                                      data,
                                      category_,
                                      read,
                                      _delete,
                                      _time,
                                      edit_msg,
                                      star_msg,
                                      template,
                                      remainder,
                                      lock,
                                      from,
                                      to,
                                      replay_msg,
                                      forward_msg ) )
                          Log.d("|||||||||msg_number :","${msg_num}")
                          Log.d("|||||||||msg_number :","as remiander :${remainder}") // showing to the screen the status of remainder iof the databse
                      }
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
                          last_message_arrived : String) : Boolean {  // this data will directly bring from server
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

        val db = this.writableDatabase     // accessing database for writting data
        try{
            db.insert(TABLE_NAME_PERSON_INFO,null,values)
            db.close()
            return  true
            Log.d("|||||||||||Person ","Information saved")
        } catch(e : Exception) {
            Log.d(">>>>>Error in saving","names in person_info db")
            return false
        }
    }



    // this will getback all the contacts that you chat with in past or trying to chat with -> direct connections to home page
    fun get_past_used_contact() : ArrayList<universal_model.front_contact_msg>{
        val db = this.readableDatabase
        var store : ArrayList<universal_model.front_contact_msg> = ArrayList<universal_model.front_contact_msg>()
        var check_name : String = ""
        var last_msg : String = ""         // stores the last message arrived
        var number_ : String = ""

        try {
            val cursor : Cursor = db.rawQuery("SELECT * FROM ${TABLE_NAME_PERSON_INFO};", null)
            Log.d("Size of cursor?????","???${cursor.count}")
            if(cursor.count>0){
                cursor.moveToFirst()
                do {
                    check_name = cursor.getString(cursor.getColumnIndexOrThrow(CONTACT_NAME))
                    last_msg = cursor.getString(cursor.getColumnIndexOrThrow(LAST_MESSAGE_ARRIVED))
                    number_ = cursor.getString(cursor.getColumnIndexOrThrow(NUMBER_))

                    store.add(universal_model.front_contact_msg(check_name,last_msg,"3:00pm",number_))
                    Log.d("@@@@@@name:${check_name}","${last_msg}")
                } while (cursor.moveToNext())
            }
            db.close()

        } catch ( e : Exception) {
            Log.d("?????????Error in ","searching name in table")
        }
        return store
    }

    // one time settings for the others person
    fun update_user_settings(operators: String, value : String, contact_number: String){
//        UPDATE ORDERTABLE SET QUANTITY = (INSERT VALUE OF YOUR EDIT TEXT) WHERE NAME =   'Order2'

        try {

            val DB = this.writableDatabase
            var sql_query = ""
            if (operators == "mute") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${MUTE_}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "about") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${ABOUT}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "wallpaper") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${WALLPAPER}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "archived") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${ARCHIVED}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "blocked") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${BLOCKED}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "pinned") {
                sql_query =
                    "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${PIN_CONTACT}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "last_time_seen_message") {
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${LAST_MESSAGE_NUMBER_SEEN}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
            }
            if (operators == "last_msg_arrived") {
                sql_query = "UPDATE ${TABLE_NAME_PERSON_INFO} SET ${LAST_MESSAGE_ARRIVED}='${value}' WHERE ${NUMBER_}=${contact_number}"
                DB.execSQL(sql_query)
                Log.d("!!!!!!!!!!!!!", "updated lst_msg")
            }
              DB.close()
        }  catch (e : Exception){
            Log.d("error in ","updating last arrived msg")
        }
    }

    // for saving path of the media and unique one in personinfo Table
    fun save_media_path(media : String , path : String){
        val db = this.writableDatabase
        var sql_query : String = ""

          // this directly saved only at the sapecific column
        if(media=="i")sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${PHOTOS}) VALUES (${path});"
        if(media=="v")sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${VIDEOS}) VALUES (${path});"
        if(media=="s")sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${STICKER}) VALUES (${path});"
        if(media=="d")sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${DOCUMENTS}) VALUES (${path});"
        if(media=="l")sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${LINK}) VALUES (${path});"

        if(media=="stared_msg"){sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${STARED_MESSAGE}) VALUES (${path});"}
        if(media=="remainder"){sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${REMAINDER}) VALUES (${path});"}
        if(media=="joined_group"){sql_query = "INSERT ${TABLE_NAME_PERSON_INFO} (${GOUP_JOINED}) VALUES (${path});"}

        db.execSQL(sql_query)
        db.close()
    }

    // saving the details of My account
    fun save_to_my_account(operators : String , value : String){
         val db = this.writableDatabase
        val vl = ContentValues()

        if(operators=="name"){
            Log.d("!!!!!!saving name","my account")
            vl.put(MY_NAME,value)
        }
        if(operators=="number"){
            vl.put(MY_NUMBER,value)
        }
        if(operators=="about"){
            vl.put(MY_ABOUT,value)
        }
        if(operators=="dp"){
            vl.put(MY_DP,value)
        }

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

            Log.d("||||||||| name:","${name_}")
            Log.d("||||||||| about:","${about}")
        }catch (e: Exception){
           Log.d("error in finding",": account")
        }
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

    fun update_one_chat_property(operators_ : String, msg_numbers : ArrayList<String> , new_value : String ){
      // the  correct form of the sql update in the database is : UPDATE TABLE_NAME SET COLUMN = ('${new_value}') WHERE ${another_column}='${value};'
        val db = this.writableDatabase
        var msg_number = 0
        var sql_query = ""
        for(i in msg_numbers) {
            if (operators_ == "star") {
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${STARED_MSG_PROPERTY}=('${new_value}') WHERE ${MESSAGE_NUMBER}='${i}';"
                  Log.d("^^^^star msg_num:","${i},value ${new_value}")
                db.execSQL(sql_query)
            }
            if (operators_ == "remainder") {
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${REMAINDER}=('${new_value}') WHERE ${MESSAGE_NUMBER}='${i}';"
                Log.d("@@@@@@@@@remiander","activated value :${new_value}")
                db.execSQL(sql_query)
            }
            if (operators_ == "share_block") {
                sql_query = "UPDATE ${TABLE_NAME_UNIVERSAL_CHAT} SET ${LOCK} = ('${new_value}') WHERE ${MESSAGE_NUMBER}='${i}';"
                db.execSQL(sql_query)
            }
        }
        db.close()
    }
 }