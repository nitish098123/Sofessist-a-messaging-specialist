package com.example.database_part_3.model


// uses only when selected message or replied message contains media form

class replay_media_model(
    var selected_text_or_download_uri : String,     // download uri for selected message media
    var selected_msg_local_uri : String,            // local_uri of selected media
    var msg_number : String,                       // selected media message number
    var replay_media_local_uri : String,          //replay media local url
    var replay_media_download_uri : String,       // replay media download_url
    var total_pages_ : String,                    // this contains the total pages of PDF
    var front_page_image : String ,          // this stores the link of front page image of pdf to show that as thumbnail
    var name_of_file : String,               // this is name of file
    var total_size_ : String                // total size of document
)