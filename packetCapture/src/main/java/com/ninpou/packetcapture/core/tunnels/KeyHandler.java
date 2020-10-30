package com.ninpou.packetcapture.core.tunnels;

import java.nio.channels.SelectionKey;

public interface KeyHandler {
    void onKeyReady(SelectionKey key);
}
