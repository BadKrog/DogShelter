package com.example.dogshelter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>  implements View.OnClickListener{

    private List itemIDs;   // ID
    private List itemNames;  // full name student
    private List itemBreeds;
    private List itemDates; // date added

    private Context context;
    private Intent intent;



    @Override
    public void onClick(View view) {
        TextView id = view.findViewById(R.id.textID);
        String strId = id.getText().toString();
        Log.d("mLog", "Нажал на список. Id="+strId);

        intent.putExtra("id_dog", strId);
        context.startActivity(intent);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView textID;
        public TextView textName;
        public TextView textBreed;
        public TextView textDate;

        public MyViewHolder(View v){
            super(v);
            textID = v.findViewById(R.id.textID);
            textName = v.findViewById(R.id.textFIO);
            textBreed = v.findViewById(R.id.textBreed);
            textDate = v.findViewById(R.id.textDate);
        }
    }

    public MyAdapter(List IDs, List Names, List Breeds, List Dates, Context context){
        itemIDs = IDs;
        itemNames = Names;
        itemBreeds = Breeds;
        itemDates = Dates;

        this.context = context;
        intent = new Intent(context, DogPage.class);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);

        v.setOnClickListener(this);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textID.setText(""+(long)itemIDs.get(position));
        holder.textName.setText((String) itemNames.get(position));
        holder.textBreed.setText((String) itemBreeds.get(position));
        holder.textDate.setText((String)itemDates.get(position));
    }

    @Override
    public int getItemCount() {
        return itemIDs.size();
    }
}