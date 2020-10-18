package com.ninpou.qbits.capture;

import android.content.Context;
import android.graphics.drawable.Drawable;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.util.processparse.AppInfo;
import com.ninpou.packetcapture.core.util.common.StringUtil;
import com.ninpou.packetcapture.core.util.common.TimeFormatter;
import com.ninpou.qbits.R;

import java.util.List;
import java.util.Objects;

public class PacketAdapter extends RecyclerView.Adapter<PacketAdapter.ViewHolder> {
    private List<String> titles;
    //Added NatSession list to implement all features with app name and icons + SSL connect
    private List<NatSession> sessionList;
    private AdapterView.OnItemClickListener onItemClickListener;
    private Drawable defaultDrawable;

    public PacketAdapter(List<String> titles, List<NatSession> sessionList, Context context) {
        this.titles = titles;
        this.sessionList = sessionList;
        this.defaultDrawable = ContextCompat.getDrawable(context, R.drawable.sym_def_app_icon);
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

        NatSession natSession = sessionList.get(pos);
        viewHolder.iv_app_icon.setImageDrawable(natSession.getAppInfo() != null && natSession.getAppInfo().packageNames != null ?
                AppInfo.getIcon(viewHolder.itemView.getContext(), Objects.requireNonNull(natSession.getAppInfo().packageNames.getAt(0))) : defaultDrawable);
        viewHolder.title.setText(null);
        boolean isTcp = NatSession.TCP.equals(natSession.type);
        viewHolder.title.setText(isTcp ?
                (TextUtils.isEmpty(natSession.getRequestUrl()) ? natSession.getRemoteHost() : natSession.getRequestUrl())
                : null);
        viewHolder.title.setText(titles.get(pos));
        viewHolder.title.setVisibility(viewHolder.title.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
        viewHolder.tv_net_state.setText(natSession.getIpAndPort());
        viewHolder.tv_capture_time.setText(TimeFormatter.formatToHHMMSSMM(natSession.getRefreshTime()));
        viewHolder.tv_net_size.setText(StringUtil.INSTANCE.getSocketSize(natSession.getBytesSent() + natSession.getReceiveByteNum()));

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

    //ViewHolder with all types of view. Implementing 1 row in recyclingView
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_app_icon;
        TextView title;
        TextView tv_net_state;
        TextView tv_capture_time;
        TextView tv_net_size;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_app_icon = itemView.findViewById(R.id.select_icon);
            title = itemView.findViewById(R.id.item_tv);
            tv_net_state = itemView.findViewById(R.id.net_state);
            tv_capture_time = itemView.findViewById(R.id.refresh_time);
            tv_net_size = itemView.findViewById(R.id.net_size);
            itemView.setTag(this);
        }
    }
}
