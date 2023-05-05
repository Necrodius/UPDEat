package com.updeat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.updeat.R;
import com.updeat.models.listeners.RecyclerViewClickListener;
import com.updeat.models.Eatery;

import java.util.ArrayList;

public class EateryRecyclerAdapter extends RecyclerView.Adapter<EateryRecyclerAdapter.ThisViewHolder> {

    Context context;
    ArrayList<Eatery> eateryArrayList;
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
        Eatery eatery = eateryArrayList.get(position);
        holder.eateryName.setText(eatery.getName());
        holder.eateryTime.setText(eatery.getTimerange());
    }

    @Override
    public int getItemCount() {
        return eateryArrayList.size();
    }
    public static class ThisViewHolder extends RecyclerView.ViewHolder{

        TextView eateryName, eateryTime;

        public ThisViewHolder(@NonNull View itemView) {
            super(itemView);
            eateryName = itemView.findViewById(R.id.txtEateryName);
            eateryTime = itemView.findViewById(R.id.txtEatTime);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewClickListener.onItemClick(getAdapterPosition());
                }
            });

        }
    }
}
