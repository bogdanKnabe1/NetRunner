package com.ninpou.packetcapture.core.vpn

//check null ref's
class VpnEvent {
    private var onPacketListener: OnPacketListener? = null
    private var onStartListener: OnStartListener? = null
    private var onStopListener: OnStopListener? = null
    fun setOnStartListener(onStartListener: OnStartListener?) {
        this.onStartListener = onStartListener
    }

    fun setOnStopListener(onStopListener: OnStopListener?) {
        this.onStopListener = onStopListener
    }

    fun setOnPacketListener(onPacketListener: OnPacketListener?) {
        this.onPacketListener = onPacketListener
    }

    fun notifyReceive() {
        if (onPacketListener != null) onPacketListener!!.onReceive()
    }

    fun notifyStart() {
        if (onStartListener != null) onStartListener!!.onStart()
    }

    fun notifyStop() {
        if (onStopListener != null) onStopListener!!.onStop()
    }

    fun cancelAll() {
        onPacketListener = null
        onStartListener = null
        onStopListener = null
    }

    interface OnPacketListener {
        fun onReceive()
    }

    interface OnStartListener {
        fun onStart()
    }

    interface OnStopListener {
        fun onStop()
    }

    private object Singleton {
        var instance = VpnEvent()
    }

    companion object {
        @JvmStatic
        val instance: VpnEvent
            get() = Singleton.instance
    }
}