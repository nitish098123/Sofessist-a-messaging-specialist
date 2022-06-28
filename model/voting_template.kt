package com.example.database_part_3.model

class voting_template(
    var topic : String,
    var total_up_vote : Int ,
    var total_down_vote : Int,
    var your_vote : Int        // 1 means-> up_vote , 2 means-> down_vote , 0-> none
)