package com.b_knabe.packet_capture.core.tunnels;

import java.nio.channels.SelectionKey;

public interface KeyHandler {
    void onKeyReady(SelectionKey key);
}
