package com.example.database_part_3.model

import android.net.Uri

// before saving the image data in database stringify from this json

class image_data_model(
    var upload_progress : String,       // it basically denotes the upload progress status of image
    var download_progress : String,     // it contains the download progress status of download status
    var local_url : String,          // the local url of image that is saved in phone
    var download_uri : String,       // this is url of image saved in server and link is stored here
    var text_ : String               // this is text if some text message you want to send with this image
)