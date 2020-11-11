package com.b_knabe.packet_capture;


import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void vpnIsOn() {
        Assert.assertFalse(vpnConnectionTest());
    }

    public boolean vpnConnectionTest() {
        String intrface = "";
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    intrface = networkInterface.getName();
                Log.d("DEBUG", "IFACE NAME: " + intrface);
                if (intrface.contains("tun") || intrface.contains("ppp") || intrface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        return false;
    }
}
