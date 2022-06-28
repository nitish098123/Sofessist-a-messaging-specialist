package com.example.database_part_3.model

class ContactModel {

    var name: String? = null
    var number: String? = null
    var _dp : String? = null

    fun setNames(name: String) {
        this.name = name
    }

    fun getNumbers(): String {
        return number.toString()
    }

    fun setNumbers(number: String) {
        this.number = number
    }

    fun getNames(): String {
        return name.toString()
    }
    fun setdp(_dp : String){
        this._dp = _dp
    }

}