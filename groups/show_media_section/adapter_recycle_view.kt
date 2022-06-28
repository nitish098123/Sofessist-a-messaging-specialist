package com.example.database_part_3.groups.show_media_section

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.database_part_3.Image_View.FullScreenImageActivity
import com.example.database_part_3.R
import com.example.database_part_3.front_page.OnContactClickListener
import com.example.database_part_3.groups.group_chat_adapter
import com.example.database_part_3.groups.group_message_model
import com.example.database_part_3.model.universal_model
import com.squareup.picasso.Picasso

class adapter_recycle_view(  private val context_ : Context,
                             private val show_data : ArrayList<show_media_model>,
                             private val onContactClickListener: OnContactClickListener,
                             private val private_chat : Boolean
                          )

    : RecyclerView.Adapter<adapter_recycle_view.DataAdapterViewHolder>(){

    var tracker : SelectionTracker<Long>? = null
    var DATA_TYPE = 0

    init {
        setHasStableIds(true)
    }

    companion object{
        val IMAGE_VIEW = 0
        val VIDEO_VIEW = 1
        val DOCUMENT_VIEW = 2
        val LINKS_VIEW = 3
        val REACTION_TEMPLATE = 4
        val AUDIO = 5
    }

    override fun getItemViewType(position: Int): Int {
        var data_type = -1
        if(show_data[position].category_ == "i")data_type = IMAGE_VIEW
        if(show_data[position].category_ == "v")data_type = VIDEO_VIEW
        if(show_data[position].category_ == "d")data_type = DOCUMENT_VIEW
        if(show_data[position].category_ =="l") data_type = LINKS_VIEW
        if(show_data[position].category_=="storing_reaction") data_type = REACTION_TEMPLATE

        DATA_TYPE = data_type
        return data_type
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): DataAdapterViewHolder {
        var _layout : Int = 0
        if(viewType== IMAGE_VIEW)_layout = R.layout.photo_item
        if(viewType == VIDEO_VIEW) _layout = R.layout.video_item
        if(viewType == DOCUMENT_VIEW) _layout = R.layout.document_item
        if(viewType == LINKS_VIEW) _layout = R.layout.link_item
        if(viewType == REACTION_TEMPLATE) _layout = R.layout.reaction_store_item

        val inflater = LayoutInflater.from(context_)
        val view = inflater.inflate(_layout, parent,false)
        return DataAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataAdapterViewHolder , position: Int) {
        val supply_data : show_media_model = show_data[position]
        holder.itemView.setOnClickListener{
            onContactClickListener.onContactClickListener(position)
        }
        tracker?.let{
            holder.BIND(supply_data)
        }
    }

    override fun getItemCount(): Int = show_data.size

    inner class DataAdapterViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        fun bind_image(datamodel : show_media_model){
            val uri = datamodel.uri_.toUri()
            Log.d("","iiiiiiiiiimage link for showing in history:${uri}")
            val image_view = itemView.findViewById<ImageView>(R.id.view_image_id)
            Picasso.with(context_).load(uri).resize(800,800).centerCrop().into(image_view)
            image_view.setOnClickListener{
                val intent = Intent(context_,FullScreenImageActivity::class.java)
                intent.putExtra("+image","$uri")
                intent.putExtra("+private_chat",private_chat)
                context_.startActivity(intent)
            }
        }

        fun bind_video(datamodel : show_media_model){
            val uri = datamodel.uri_.toUri()
            val video_view = itemView.findViewById<ImageView>(R.id.view_video_thumbnail)
            Glide.with(context_).asBitmap().load(uri).into(video_view);   // setting the thmbnail of vedio
            video_view.setOnClickListener{
                val intent = Intent(context_,FullScreenImageActivity::class.java)
                intent.putExtra("+image","$uri")
                intent.putExtra("+private_chat",private_chat)
                context_.startActivity(intent)
            }
        }

        fun bind_reaction_template(datamodel: show_media_model){
            val topic = datamodel.text_      // copying the text from reaction store
            val topic_show = itemView.findViewById<TextView>(R.id.topic_of_template)
            topic_show.setText(topic)
            val layout_ = itemView.findViewById<RelativeLayout>(R.id.topic_of_reaction_bar)
        }

        fun bind_document(datamodel : show_media_model){

        }

        fun bind_link(datamodel : show_media_model){

        }

        fun BIND(datamodel : show_media_model){
            if(datamodel.category_=="i")bind_image(datamodel)
            if(DATA_TYPE== VIDEO_VIEW)bind_video(datamodel)
            if(DATA_TYPE== DOCUMENT_VIEW) bind_document(datamodel)
            if(DATA_TYPE== LINKS_VIEW) bind_link(datamodel)
            if(DATA_TYPE== REACTION_TEMPLATE) bind_reaction_template(datamodel)
        }

        // used for the long pressed function in group_chat_activity()
        fun getItemDetails() : ItemDetailsLookup.ItemDetails<Long> = object : ItemDetailsLookup.ItemDetails<Long>(){
            override fun getPosition() : Int = adapterPosition
            override fun getSelectionKey() :Long = itemId
        }

    }

}
