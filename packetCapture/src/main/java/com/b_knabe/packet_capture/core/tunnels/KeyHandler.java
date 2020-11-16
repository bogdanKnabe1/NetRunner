package com.b_knabe.packet_capture.core.tunnels;

import java.nio.channels.SelectionKey;

/*
* {@link SelectionKey} Distribution interface
*/
public interface KeyHandler {

    /**
     * When {@link SelectionKey} is ready
     *
     * @param key
     */

    void onKeyReady(SelectionKey key);
}
