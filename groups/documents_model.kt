package com.example.database_part_3.groups

class documents_model(
    var file_name : String,      // name of documents saved in phone
    var local_uri : String,      // local link of file
    var download_uri : String,   // download uri of file
    var file_type : String,      // contains the type of file of document
    var progress_ : String,     // for updating the progress of download or upload
    var total_size : String ,    // 'contains total document size
    var total_pages : String,     // this is for pdf view contains the total pages in pdf
    var front_page_image : String    //  this contains the link of front page image if pdf
)