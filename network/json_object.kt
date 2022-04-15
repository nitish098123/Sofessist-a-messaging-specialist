package com.example.database_part_3.network

// this file is for making class datatype ti convert into the json

/*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
val mapper = jacksonObjectMapper()
val user = User(102, "test", "pass12", "Test User")
val userJson = mapper.writeValueAsString(user)
now userJson is json string
*/

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


// {_s:"s" , p:`${name}_${i_p.value}` , d:`${input.value}` ,to : `${i_p.value}`, f:`${name}`, n:10,ti: "8.00PM"}    for one message sending
// var send_data = { _s : "u" , N : name , type : "a_r"};   sending name and information to server


data class json_object(
 val _s : String,
 val pair : String,
 val d : String,
 val to : String,
 val f : String,
 val n : Int,
 val ti : String
)

data class login(
  val _s : String,
  val N : String,
  val type : String
)