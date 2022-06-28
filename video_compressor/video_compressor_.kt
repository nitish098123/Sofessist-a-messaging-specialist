package com.example.database_part_3.video_compressor

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.StorageConfiguration

class video_compressor_(val context_ : Context) {
    val data = mutableListOf<VideoDetailsModel>()
    fun compress_video(uri_ : ArrayList<Uri>){
        val handler = Handler()
        Thread({
        VideoCompressor.start(context_,uri_,true, StorageConfiguration(saveAt = Environment.DIRECTORY_MOVIES,isExternal = true),

             Configuration( quality = VideoQuality.LOW ,            //  set the video quality of of compressor as low you choose as time it takes
                            isMinBitrateCheckEnabled = true ),

             listener = object : CompressionListener {
                override fun onProgress(index: Int, percent: Float) {
                    if(percent<=100 && percent.toInt()%5==0)
                        handler.post {
                            data[index] = VideoDetailsModel("",uri_[index],"",percent)
                            Log.d("","%%%%%%%%%%%%%% of video compressor is: ${percent}%")
                        }
                }

                override fun onStart(index: Int) {
                    data.add(index, VideoDetailsModel("",
                                                       uri_[index],
                                                       ""))
                }

                override fun onCancelled(index: Int) {
                    Log.d("","cccccccccccccanceling the video compressor")
                }

                override fun onFailure(index: Int, failureMessage: String){
                    Log.d("","ffffffffffffailure in compressor:${failureMessage}")
                }

                override fun onSuccess(index: Int, size: Long, path: String?) {
                    data[index] = VideoDetailsModel( path,
                                                     uri_[index],
                                                     getFileSize(size),
                                                     100F )
                }

            }
            )

        }).start()   // thread is started
    }

    fun getFileSize(size : Long) : String{
        val kk = size/1000
        var final_size = ""
        if(kk>=1000){    // in MB
            final_size = "${kk/1000}MB"
        }
        if(kk<1000){
            final_size = "${kk}KB"
        }
        Log.d("","TTTTTTTTTTTTThe final size of video is:${final_size}")
        return final_size
    }
}