package com.ninpou.packetcapture.core.tunnel

import java.nio.channels.SelectionKey

interface KeyHandler {
    fun onKeyReady(key: SelectionKey?)
}