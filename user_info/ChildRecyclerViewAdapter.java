package com.example.database_part_3.user_info;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.database_part_3.R;
import java.util.ArrayList;


public class ChildRecyclerViewAdapter extends RecyclerView.Adapter<ChildRecyclerViewAdapter.MyViewHolder>{

    private int PHOTOS = 0;
    private int VEDIOS = 1;
    private int DOCUMENTS = 2;
    private int AUDIOS = 3;
    private int LINKS = 4;

    public ArrayList<String> childModelArrayList;
    Context cxt;
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView text_to_show;

        public  MyViewHolder(View itemview){
            super(itemview);
            text_to_show = itemview.findViewById(R.id.child_item_of_pair_info);
        }
    }

    public ChildRecyclerViewAdapter(ArrayList<String> arrayList, Context mContext) {
        this.cxt = mContext;
        this.childModelArrayList = arrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {      // this is the point where we give different child layout for showing inside the recycle view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_view_photos, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
            String currentItem = childModelArrayList.get(position);
            holder.text_to_show.setText(currentItem);
    }

    @Override
    public int getItemCount() {
        return childModelArrayList.size();
    }
}