package com.example.hexbow.frag.fragutil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hexbow.R;
import com.example.hexbow.callback.CallbackInterface;
import com.example.hexbow.frame.Animation;
import com.example.hexbow.frame.FrameView;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.ViewHolder> {

    private Animation anim;
    private static CallbackInterface ci;

    public FrameAdapter(Animation anim){
        super();
        this.anim = anim;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public FrameView sView;

        public ViewHolder(View v){
            super(v);
            v.setOnClickListener(this);
            sView = v.findViewById(R.id.indivFrame);

        }

        @Override
        public void onClick(View v) {
            ci.onDismiss(getAdapterPosition());
        }

    }

    @NonNull
    @Override
    public FrameAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_row, parent, false);

        return new FrameAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameAdapter.ViewHolder holder, int position) {
        holder.sView.setFrame(anim.getFrame(position));
        anim.getFrame(position).setFrameView(holder.sView);
    }

    @Override
    public int getItemCount() {
        return anim.getNumFrames();
    }

    public static void setCallbackInterface(CallbackInterface ci){
        FrameAdapter.ci = ci;
    }
}
