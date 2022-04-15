package com.example.database_part_3.login

import com.fasterxml.jackson.core.JsonStreamContext
import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface APIService {
    @POST("/submit")
    suspend fun createEmployee(@Body requestBody: RequestBody) : Response<ResponseBody>
}