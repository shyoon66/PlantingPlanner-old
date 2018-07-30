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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.yoonbae.plantingplanner.AddActivity;
import com.yoonbae.plantingplanner.ListActivity;
import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Plant> plantList;
    private List<String> keyList;
    Context context;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    public MyRecyclerViewAdapter(List<Plant> plantList, List<String> keyList, Context context) {
        this.plantList = plantList;
        this.keyList = keyList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, parent, false);
        return new RowCell(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ((RowCell)holder).name.setText(plantList.get(position).getName());
        ((RowCell)holder).intro.setText(plantList.get(position).getIntro());
        Glide.with(((RowCell) holder).imageView.getContext()).load(plantList.get(position).getImageUrl()).into(((RowCell) holder).imageView);

        ((RowCell)holder).imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String items[] = {"식물수정", "식물삭제", "취소"};
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {
                        if(index == 0) {

                        } else if(index == 1) {
                            deletePlant(position);
                        } else {
                            dialog.dismiss();
                        }

                        dialog.dismiss();
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

    private void deletePlant(final int position) {
        database = FirebaseDatabase.getInstance();
        String key = keyList.get(position);

        database.getReference().child("plant").child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String imageName = plantList.get(position).getImageName();
                storage = FirebaseStorage.getInstance();
                storage.getReference().child("images").child(imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "식물이 삭제됐습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "식물 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "식물 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
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
