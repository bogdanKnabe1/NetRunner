package com.ninpou.qbits.capture

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ninpou.packetcapture.core.nat.NatSession
import com.ninpou.packetcapture.core.util.common.StringUtil.getSocketSize
import com.ninpou.packetcapture.core.util.common.TimeFormatter
import com.ninpou.packetcapture.core.util.process_parse.ApplicationInfo
import com.ninpou.qbits.R
import java.util.*

class PacketAdapter(private val titles: List<String>, //Added NatSession list to implement all features with app name and icons + SSL connect
                    private val sessionList: List<NatSession>, context: Context?) : RecyclerView.Adapter<PacketAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private val defaultDrawable: Drawable? = ContextCompat.getDrawable(context!!, R.drawable.sym_def_app_icon)
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recycle_view,
                parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        val natSession = sessionList[pos]
        viewHolder.appName.text = if (natSession.getApplicationInfo() != null) natSession.getApplicationInfo().leaderAppName else viewHolder.itemView.context.getString(R.string.unknown)
        viewHolder.appImageIcon.setImageDrawable(if (natSession.getApplicationInfo() != null && natSession.getApplicationInfo().appPackageNames != null) ApplicationInfo.getIcon(viewHolder.itemView.context, Objects.requireNonNull(natSession.getApplicationInfo().appPackageNames.getAt(0))) else defaultDrawable)
        viewHolder.title.text = null
        val isTcp = NatSession.TCP == natSession.type
        viewHolder.title.text = if (isTcp) if (TextUtils.isEmpty(natSession.getRequestUrl())) natSession.getRemoteHost() else natSession.getRequestUrl() else null
        viewHolder.title.text = titles[pos]
        viewHolder.title.visibility = if (viewHolder.title.text.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        viewHolder.netState.text = natSession.getIpAndPort()
        viewHolder.capturedTime.text = TimeFormatter.formatToHHMMSSMM(natSession.refreshTime)
        viewHolder.tvNetSize.text = getSocketSize(natSession.getBytesSent() + natSession.getReceiveByteNum())
        viewHolder.itemView.setOnClickListener {
            val pos = viewHolder.layoutPosition
            onItemClickListener!!.onItemClick(null, viewHolder.itemView, pos, 0)
        }
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    //ViewHolder with all types of view. Implementing 1 row in recyclingView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appImageIcon: ImageView = itemView.findViewById(R.id.select_icon)
        var appName: TextView = itemView.findViewById(R.id.app_name)
        var title: TextView = itemView.findViewById(R.id.item_tv)
        var netState: TextView = itemView.findViewById(R.id.net_state)
        var capturedTime: TextView = itemView.findViewById(R.id.refresh_time)
        var tvNetSize: TextView = itemView.findViewById(R.id.net_size)

        init {
            itemView.tag = this
        }
    }

}