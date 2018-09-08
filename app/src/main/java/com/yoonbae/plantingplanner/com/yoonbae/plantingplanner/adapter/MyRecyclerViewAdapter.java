package com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.yoonbae.plantingplanner.AddActivity;
import com.yoonbae.plantingplanner.R;
import com.yoonbae.plantingplanner.ViewActivity;
import com.yoonbae.plantingplanner.com.yoonbae.plantingplanner.vo.Plant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Plant> plantList;
    private List<String> keyList;
    private Context context;
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

        ((RowCell)holder).imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewActivity.class);
                intent.putExtra("name", plantList.get(position).getName());
                intent.putExtra("kind", plantList.get(position).getKind());
                intent.putExtra("intro", plantList.get(position).getIntro());
                intent.putExtra("imageUrl", plantList.get(position).getImageUrl());
                context.startActivity(intent);
            }
        });

        ((RowCell)holder).imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String items[] = {"식물수정", "식물삭제", "취소"};
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("");
                ab.setItems(items, new DialogInterface.OnClickListener() {    // 목록 클릭시 설정
                    public void onClick(DialogInterface dialog, int index) {
                    if(index == 0) {
                        Intent intent = new Intent(context, AddActivity.class);
                        intent.putExtra("FLAG", "U");
                        intent.putExtra("name", plantList.get(position).getName());
                        intent.putExtra("kind", plantList.get(position).getKind());
                        intent.putExtra("intro", plantList.get(position).getIntro());
                        intent.putExtra("imageUrl", plantList.get(position).getImageUrl());
                        intent.putExtra("uid", plantList.get(position).getUid());
                        context.startActivity(intent);
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

        public RowCell(View view) {
            super(view);
            imageView = view.findViewById(R.id.cardview_imageview);
            name = view.findViewById(R.id.cardview_name);
            intro = view.findViewById(R.id.cardview_intro);
            imageButton = view.findViewById(R.id.cardview_btn);
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

}
