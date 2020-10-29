package com.ninpou.packetcapture.tcp_ip_level;

import androidx.annotation.NonNull;

import com.ninpou.packetcapture.core.util.net_utils.Packets;

import java.util.Locale;

/**!
 * Low-level java networking
 *
 * */
public class UdpPacketHeader {

    /**
     * UDP datagram format
     * Header length: 8 bytes
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜ 16-digit source port number ｜ 16-digit destination port number ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜ 16-digit UDP length ｜ 16-digit UDP checksum ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜ Data (if any) ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     **/


    /**
     * The following 4 offsets are the byte position of each attribute in the UDP message
     */
    static final short offset_src_port = 0; // source port
    static final short offset_dest_port = 2; //Destination port
    static final short offset_tlen = 4; //Datagram length
    static final short offset_crc = 6; //Checksum

    public byte[] data;
    public int offset;

    /**
     * @param data message data, it may be ip message or UDP message, so you need to operate according to offset when parsing message information
     * @param offset The offset of the UDP information relative to the message data data, which is 20 for IP messages and 0 for UDP.
     */
    public UdpPacketHeader(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    /**
     * Get the source port number
     * @return
     */
    public short getSourcePort() {
        return Packets.readShort(data, offset + offset_src_port);
    }
    /**
     * Modify the source port number in the message
     * @paramSourcePort port number
     */
    public void setSourcePort(short value) {
        Packets.writeShort(data, offset + offset_src_port, value);
    }

    public short getDestinationPort() {
        return Packets.readShort(data, offset + offset_dest_port);
    }

    public void setDestinationPort(short value) {
        Packets.writeShort(data, offset + offset_dest_port, value);
    }

    public int getTotalLength() {
        return Packets.readShort(data, offset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(int value) {
        Packets.writeShort(data, offset + offset_tlen, (short) value);
    }

    public short getCrc() {
        return Packets.readShort(data, offset + offset_crc);
    }

    public void setCrc(short value) {
        Packets.writeShort(data, offset + offset_crc, value);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%d->%d", getSourcePort() & 0xFFFF, getDestinationPort() & 0xFFFF);
    }
}
