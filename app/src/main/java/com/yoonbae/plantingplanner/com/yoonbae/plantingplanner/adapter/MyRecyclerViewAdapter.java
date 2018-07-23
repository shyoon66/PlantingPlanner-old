package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Plant> plantList = new ArrayList<>();

    public MyRecyclerViewAdapter() {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new RowCell(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //((RowCell)holder).imageView.setImageResource(plantList.get(position).getImageUrl());
        ((RowCell)holder).title.setText(plantList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private class RowCell extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView title;
        public TextView subtitle;

        public RowCell(View view) {
            super(view);
            imageView = (ImageView)view.findViewById(R.id.cardview_imageview);
            title = (TextView)view.findViewById(R.id.cardview_title);
            subtitle = (TextView)view.findViewById(R.id.cardview_subtitle);
        }
    }
}
