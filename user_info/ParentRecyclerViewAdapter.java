package com.example.database_part_3.user_info;


// this recycle view is for horizontal one
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.database_part_3.R;
import com.example.database_part_3.model.universal_model;
import java.util.ArrayList;

public class ParentRecyclerViewAdapter extends RecyclerView.Adapter<ParentRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<String>  parentModelArrayList;
    public Context cxt;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView category;
        public RecyclerView childRecyclerview;

        public MyViewHolder(View itemView){
            super(itemView);
            category = itemView.findViewById(R.id.photos_name_id);
            childRecyclerview = itemView.findViewById(R.id.parent_recycle_view);
        }
    }

    public ParentRecyclerViewAdapter(ArrayList<String> exampleList, Context context) {
        this.parentModelArrayList = exampleList;
        this.cxt = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_recycle_view, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public int getItemCount() {
        return parentModelArrayList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){

        String currentItem = parentModelArrayList.get(position);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(cxt, LinearLayoutManager.HORIZONTAL, false);
        holder.childRecyclerview.setLayoutManager(layoutManager);
        holder.childRecyclerview.setHasFixedSize(true);
        holder.category.setText(currentItem);

        ArrayList<String> arrayList = new ArrayList<>();
        if (parentModelArrayList.get(position).equals("PHOTOS")) {
            arrayList.add("Photos Name1");
            arrayList.add("Photos Name2");
            arrayList.add("Photos Name3");
            arrayList.add("Photos Name4");
            arrayList.add("Photos Name5");
            arrayList.add("Photos Name6");
            arrayList.add("Photos Name7");
            arrayList.add("Photos Name8");
            arrayList.add("Photos Name9");
        }

        if (parentModelArrayList.get(position).equals("VEDIOS")) {
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
            arrayList.add("vedios number 1");
        }
        if (parentModelArrayList.get(position).equals("DOCUMENTS")) {
            arrayList.add("Document number 1");
            arrayList.add("Document number 1");
            arrayList.add("Document number 1");
            arrayList.add("Document number 1");
            arrayList.add("Document number 1");
            arrayList.add("Document number 1");
        }
        if (parentModelArrayList.get(position).equals("AUDIOS")) {
            arrayList.add("audios number 1");
            arrayList.add("audios number 1");
            arrayList.add("audios number 1");
            arrayList.add("audios number 1");
            arrayList.add("audios number 1");
            arrayList.add("audios number 1");
        }
        if (parentModelArrayList.get(position).equals("LINKS")) {
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
            arrayList.add("Link number 1");
        }

        ChildRecyclerViewAdapter childRecyclerViewAdapter = new ChildRecyclerViewAdapter(arrayList,holder.childRecyclerview.getContext());
        holder.childRecyclerview.setAdapter(childRecyclerViewAdapter);

    }

}
