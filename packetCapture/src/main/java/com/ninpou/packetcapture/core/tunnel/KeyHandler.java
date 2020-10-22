package com.ninpou.packetcapture.core.tunnel;

import java.nio.channels.SelectionKey;

public interface KeyHandler {
    void onKeyReady(SelectionKey key);
}
