package com.example.hexbow.frag.fragutil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hexbow.R;
import com.example.hexbow.lamp.Lamp;
import com.example.hexbow.lamp.SwatchView;

public class SwatchAdapter extends RecyclerView.Adapter<SwatchAdapter.ViewHolder> {

    private Lamp toManage;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public SwatchView sView;

        public ViewHolder(View v){
            super(v);
            sView = v.findViewById(R.id.recyclerSwatch);

        }

    }

    public SwatchAdapter(Lamp toManage){
        this.toManage = toManage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.swatch_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.sView.setSwatch(toManage.getOnlineSwatches().get(position));
    }

    @Override
    public int getItemCount() {
        return toManage.getOnlineSwatches().size();
    }

}
