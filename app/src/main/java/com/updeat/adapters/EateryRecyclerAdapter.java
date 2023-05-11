package com.updeat.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.updeat.R;
import com.updeat.models.listeners.RecyclerViewClickListener;
import com.updeat.models.Eatery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EateryRecyclerAdapter extends RecyclerView.Adapter<EateryRecyclerAdapter.ThisViewHolder> {

    Context context;
    ArrayList<Eatery> eateryArrayList;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    List<Address> addresses;

    private static RecyclerViewClickListener recyclerViewClickListener;
    public void setFilteredList(ArrayList<Eatery> filteredList){
        this.eateryArrayList = filteredList;
        notifyDataSetChanged();
    }

    public EateryRecyclerAdapter(Context context, ArrayList<Eatery> eateryArrayList, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.eateryArrayList = eateryArrayList;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }


    @NonNull
    @Override
    public EateryRecyclerAdapter.ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.eatery_item, parent, false);

        return new ThisViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EateryRecyclerAdapter.ThisViewHolder holder, int position) {
        if(eateryArrayList.get(position) != null){
            Eatery eatery = eateryArrayList.get(position);

            holder.eateryName.setText(eatery.getName());
            holder.eateryTime.setText(eatery.getTimerange());
            
            if (eatery.getBudget() == 1){
                holder.eateryBudget.setText("₱");
            }
            else if (eatery.getBudget() == 2){
                holder.eateryBudget.setText("₱₱");
            }
            else if (eatery.getBudget() == 3){
                holder.eateryBudget.setText("₱₱₱");
            }
            String coords = eatery.getLatitude().toString() + ", " + eatery.getLongitude().toString();

            holder.eateryAddress.setText(coords);
        }
    }

    @Override
    public int getItemCount() {
        return eateryArrayList.size();
    }
    public static class ThisViewHolder extends RecyclerView.ViewHolder{

        TextView eateryName, eateryTime, eateryBudget, eateryAddress;
        ImageView eateryImage;

        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);
            eateryName = itemView.findViewById(R.id.txtEateryName);
            eateryTime = itemView.findViewById(R.id.txtEatTime);
            eateryImage = itemView.findViewById(R.id.imgEateryPic);
            eateryBudget = itemView.findViewById(R.id.txtBgt);
            eateryAddress = itemView.findViewById(R.id.txtAddr);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickListener.onItemClick(getAdapterPosition());
                }
            });

        }
    }
}
