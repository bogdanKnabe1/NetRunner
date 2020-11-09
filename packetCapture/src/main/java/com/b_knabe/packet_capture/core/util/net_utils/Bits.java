package com.b_knabe.packet_capture.core.util.net_utils;

/**!
 * Low-level java
 * Network Event Handler
 * IMMUTABLE
 * */

public class Bits {
    public static short getUnsignedByte(byte value) {
        return (short) (value & 0xFF);
    }

    public static int getUnsignedShort(short value) {
        return value & 0xFFFF;
    }

    public static long getUnsignedInt(int value) {
        return value & 0xFFFFFFFFL;
    }
}
