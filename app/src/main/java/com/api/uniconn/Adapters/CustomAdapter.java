package com.api.uniconn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.api.uniconn.Activites.activity_details;
import com.api.uniconn.Models.CardViewItems;
import com.api.uniconn.Rest.Models.NearbySearch;
import com.api.uniconn.Rest.Models.Result;
import com.api.uniconn.java.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;


/**
 * Created by Rakshit on 5/30/2016.
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private ArrayList<CardViewItems> android;
    private View view;

    public CustomAdapter(ArrayList<CardViewItems> android) {
        this.android = android;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        viewHolder.card_name.setText(android.get(i).getName());
        viewHolder.card_type.setText(android.get(i).getType());
        viewHolder.card_distance.setText(android.get(i).getDistance());
        viewHolder.card_address.setText(android.get(i).getLocation());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), activity_details.class);
                Log.i("CustomAdapter",viewHolder.getLayoutPosition()+"");
                intent.putExtra("position",viewHolder.getLayoutPosition());
                view.getContext().startActivity(intent);
            }
        });
        //viewHolder.tv_api_level.setText(android.get(i).getTruncated());
    }


    @Override
    public int getItemCount() {
        return android.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView card_name;
        TextView card_type;
        TextView card_distance;
        TextView card_address;

        public ViewHolder(View view) {
            super(view);
            card_name = (TextView)view.findViewById(R.id.card_name);
            card_type = (TextView)view.findViewById(R.id.card_type);
            card_distance = (TextView)view.findViewById(R.id.card_distance);
            card_address = (TextView)view.findViewById(R.id.card_address);
        }
    }
}
