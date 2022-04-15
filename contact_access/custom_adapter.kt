package com.example.database_part_3.contact_access

/*
    created in 2/3/2022: By Nitish Kr Boro
    this method will bring all the contacts from phone with velocity 200 contacts per second
*/

import android.content.Context
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.database_part_3.R
import java.util.*
import kotlin.collections.ArrayList
import android.net.Uri
import android.Manifest
import android.content.ContentUris
import androidx.annotation.RequiresPermission
import com.example.database_part_3.model.ContactModel
import com.example.database_part_3.model.universal_model

// making class for retriving contacts
class custom_adapter(private val context: Context, private val contactModelArrayList: ArrayList<ContactModel>) : BaseAdapter() {

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getCount(): Int {
      return contactModelArrayList.size
    }

    override fun getItem(position: Int): Any {
        return contactModelArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.home_page, null, true)

            holder.tvname = convertView!!.findViewById(R.id.name) as TextView
            holder.tvnumber = convertView.findViewById(R.id.number) as TextView

            convertView.tag = holder
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = convertView.tag as ViewHolder
        }

        holder.tvname!!.setText(contactModelArrayList[position].getNames())
        holder.tvnumber!!.setText(contactModelArrayList[position].getNumbers())

        return convertView
    }


    private inner class ViewHolder {
        var tvname: TextView? = null
        var tvnumber: TextView? = null
    }
}