package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yoonbae.plantingplanner.AddActivity;
import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ((RowCell)holder).name.setText(plantList.get(position).getName());
        ((RowCell)holder).intro.setText(plantList.get(position).getIntro());
        Glide.with(((RowCell) holder).imageView.getContext()).load(plantList.get(position).getImageUrl()).into(((RowCell) holder).imageView);

        ((RowCell)holder).imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String items[] = {"식물수정", "식물삭제"};
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {

                    }
                });
                ab.show();
            }
        });
/*        ArrayAdapter menuAdapter = ArrayAdapter.createFromResource(context, R.array.list_menu, android.R.layout.simple_spinner_dropdown_item);
        menuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((RowCell)holder).spinner.setAdapter(menuAdapter);
        ((RowCell)holder).spinner.setOnItemSelectedListener(this);*/
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    private class RowCell extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView name;
        public TextView intro;
        ImageButton imageButton;
        //public Spinner spinner;

        public RowCell(View view) {
            super(view);
            imageView = view.findViewById(R.id.cardview_imageview);
            name = view.findViewById(R.id.cardview_name);
            intro = view.findViewById(R.id.cardview_intro);
            imageButton = view.findViewById(R.id.cardview_btn);
            //spinner = view.findViewById(R.id.cardview_spinner);
        }
    }

    //아이템 중 하나를 선택 했을때 호출되는 콜백 메서드
/*    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position != 0) {

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }*/
}
