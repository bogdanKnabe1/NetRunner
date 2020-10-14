package com.ninpou.qbits.capture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.qbits.R;

import java.util.List;


public class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ViewHolder> {
    private List<String> titles;
    //Added NatSession list to implement all features with app name and icons + SSL connect
    private List<NatSession> sessionList;
    private AdapterView.OnItemClickListener onItemClickListener;

    public PacketAdapter(List<String> titles, List<NatSession> sessionList) {
        this.titles = titles;
        this.sessionList = sessionList;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<String> getTitles() {
        return titles;
    }

    @NonNull
    @Override
    public PacketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycle_view,
                parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PacketAdapter.ViewHolder viewHolder, int pos) {
        viewHolder.title.setText(titles.get(pos));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = viewHolder.getLayoutPosition();
                onItemClickListener.onItemClick(null, viewHolder.itemView, pos, 0);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_tv);
        }
    }
}
