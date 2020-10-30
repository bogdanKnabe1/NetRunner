package com.ninpou.packetcapture.tcp_ip_level;

import com.ninpou.packetcapture.core.util.net_utils.Packets;

import java.util.Locale;

/**!
 * Low-level java networking structure
 * */
public class IpPacketHeader {
    // The serial numbers here are all byte positions
    public static final short IP = 0x0800;
    public static final byte ICMP = 1;

    public static final byte TCP = 6; //6: TCP protocol number
    public static final byte UDP = 17; //17: UDP protocol number
    public static final byte offset_proto = 9; //9: 8-bit protocol offset
    public static final int offset_src_ip = 12; //12: source ip address offset
    public static final int offset_dest_ip = 16; //16: target ip address offset
    static final byte offset_ver_ihl = 0; //0: version number (4bits) + header length (4bits)
    static final byte offset_tos = 1; //1: service type offset
    static final short offset_tlen = 2; //2: total length offset
    static final short offset_identification = 4; //4: 16-bit identifier offset
    static final short offset_flags_fo = 6; //6: flag (3bits) + chip offset (13bits)
    static final byte offset_ttl = 8; //8: survival time offset
    static final short offset_crc = 10; //10: first checksum offset
    static final int offset_op_pad = 20; //20: option + padding

    // ip message data
    public byte[] data;
    public int offset;

    public IpPacketHeader(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public int getDataLength() {
        return this.getTotalLength() - this.getHeaderLength();
    }
    /**
     * Header length, mData[mOffset + offset_ver_ihl] & 0x0F Only the last 4 bits are reserved
     * (mData[mOffset + offset_ver_ihl] & 0x0F) * 4 times 4 means that each bit represents 4 bytes,
     * A total of 1111 (binary) * 4 = 60 bytes, so the ip header can be up to 60 bytes
     * @return
     */
    public int getHeaderLength() {
        return (data[offset + offset_ver_ihl] & 0x0F) * 4;
    }
    // 4 << 4 means the version is IPv4
    public void setHeaderLength(int value) {
        data[offset + offset_ver_ihl] = (byte) ((4 << 4) | (value / 4));
    }

    public byte getTos() {
        return data[offset + offset_tos];
    }

    public void setTos(byte value) {
        data[offset + offset_tos] = value;
    }

    public int getTotalLength() {
        return Packets.readShort(data, offset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(int value) {
        Packets.writeShort(data, offset + offset_tlen, (short) value);
    }

    public int getIdentification() {
        return Packets.readShort(data, offset + offset_identification) & 0xFFFF;
    }

    public void setIdentification(int value) {
        Packets.writeShort(data, offset + offset_identification, (short) value);
    }

    public short getFlagsAndOffset() {
        return Packets.readShort(data, offset + offset_flags_fo);
    }

    public void setFlagsAndOffset(short value) {
        Packets.writeShort(data, offset + offset_flags_fo, value);
    }

    public byte getTTL() {
        return data[offset + offset_ttl];
    }

    public void setTTL(byte value) {
        data[offset + offset_ttl] = value;
    }

    public byte getProtocol() {
        return data[offset + offset_proto];
    }

    public void setProtocol(byte value) {
        data[offset + offset_proto] = value;
    }

    public short getCrc() {
        return Packets.readShort(data, offset + offset_crc);
    }

    public void setCrc(short value) {
        Packets.writeShort(data, offset + offset_crc, value);
    }

    public int getSourceIP() {
        return Packets.readInt(data, offset + offset_src_ip);
    }

    public void setSourceIP(int value) {
        Packets.writeInt(data, offset + offset_src_ip, value);
    }

    public int getDestinationIP() {
        return Packets.readInt(data, offset + offset_dest_ip);
    }

    public void setDestinationIP(int value) {
        Packets.writeInt(data, offset + offset_dest_ip, value);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s->%s Protocol=%s, HeaderLen=%d", Packets.ipToString(getSourceIP()),
                Packets.ipToString(getDestinationIP()), getProtocol(), getHeaderLength());
    }
}
