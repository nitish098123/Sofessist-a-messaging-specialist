package com.example.database_part_3.user_info;

import com.example.database_part_3.R
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog


class screen_activity : AppCompatActivity() {
    private var parentRecyclerView : RecyclerView? = null
    private var ParentAdapter : RecyclerView.Adapter<*>? = null
    var parentModelArrayList : ArrayList<String> = ArrayList()
    private var parentLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pair_user_info)

        val _name : String = intent.getStringExtra("_name_").toString()
        val _number : String = intent.getStringExtra("_number_").toString()

        //  setting the name and number in the usersinfo
        val name_ = findViewById<TextView>(R.id.pair_user_name_id)
        val number_ = findViewById<TextView>(R.id.pair_user_number_id)
        name_.text = "~$_name"
        number_.text = _number

        //set the Categories for each array list set in the  ParentrecyclerAdapter
        parentModelArrayList.add("PHOTOS")
        parentModelArrayList.add("VEDIOS")
        parentModelArrayList.add("DOCUMENTS")
        parentModelArrayList.add("AUDIOS")
        parentModelArrayList.add("LINKS")

        val button_ = findViewById<TextView>(R.id.view_below_sheeet_media)
        button_.setOnClickListener({

            val sheet: BottomSheetDialog = BottomSheetDialog(this)
            sheet.setContentView(R.layout.below_sheet_info_media)

            parentRecyclerView = sheet.findViewById(R.id.pair_info_recycle_view_id)
            parentRecyclerView!!.setHasFixedSize(true)
            parentLayoutManager = LinearLayoutManager(this)
            ParentAdapter = ParentRecyclerViewAdapter(parentModelArrayList, this@screen_activity)
            parentRecyclerView!!.setLayoutManager(parentLayoutManager)
            parentRecyclerView!!.setAdapter(ParentAdapter)
            ParentAdapter!!.notifyDataSetChanged()
           sheet.show()

        })
    }
}
