package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Plant> plantList;
    Context context;

    public MyRecyclerViewAdapter(List<Plant> plantList, Context context) {
        this.plantList = plantList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new RowCell(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((RowCell)holder).name.setText(plantList.get(position).getName());
        ((RowCell)holder).intro.setText(plantList.get(position).getIntro());

        Glide.with(((RowCell) holder).imageView.getContext()).load(plantList.get(position).getImageUrl()).into(((RowCell) holder).imageView);
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    private class RowCell extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name;
        public TextView intro;

        public RowCell(View view) {
            super(view);
            imageView = view.findViewById(R.id.cardview_imageview);
            name = view.findViewById(R.id.cardview_name);
            intro = view.findViewById(R.id.cardview_intro);
        }
    }
}
