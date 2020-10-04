package com.ninpou.packetcapture.struct;

import com.ninpou.packetcapture.core.util.net.Packets;

import java.util.Locale;


public class TcpHeader {
    public static final int FIN = 1;
    public static final int SYN = 2;
    public static final int RST = 4;
    public static final int PSH = 8;
    public static final int ACK = 16;
    public static final int URG = 32;

    static final short offset_src_port = 0; // 16-bit source port
    static final short offset_dest_port = 2; // 16-bit destination port
    static final int offset_seq = 4; //32-bit serial number
    static final int offset_ack = 8; //32-bit confirmation number
    static final byte offset_lenres = 12; //4-bit header length + 4 reserved bits
    static final byte offset_flag = 13; //2-bit reserved word + 6-bit flag
    static final short offset_win = 14; //16-bit window size
    static final short offset_crc = 16; //16-bit checksum
    static final short offset_urp = 18; //16-bit emergency offset

    public byte[] data;
    public int offset;

    public TcpHeader(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public int getHeaderLength() {
        int lenres = data[offset + offset_lenres] & 0xFF;
        return (lenres >> 4) * 4;
    }

    public short getSourcePort() {
        return Packets.readShort(data, offset + offset_src_port);
    }

    public void setSourcePort(short value) {
        Packets.writeShort(data, offset + offset_src_port, value);
    }

    public short getDestinationPort() {
        return Packets.readShort(data, offset + offset_dest_port);
    }

    public void setDestinationPort(short value) {
        Packets.writeShort(data, offset + offset_dest_port, value);
    }

    public byte getFlag() {
        return data[offset + offset_flag];
    }

    public short getCrc() {
        return Packets.readShort(data, offset + offset_crc);
    }

    public void setCrc(short value) {
        Packets.writeShort(data, offset + offset_crc, value);
    }

    public int getSeqID() {
        return Packets.readInt(data, offset + offset_seq);
    }

    public int getAckID() {
        return Packets.readInt(data, offset + offset_ack);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s%s%s%s%s%s %d->%d %s:%s",
                (getFlag() & SYN) == SYN ? "SYN" : "",
                (getFlag() & ACK) == ACK ? "ACK" : "",
                (getFlag() & PSH) == PSH ? "PSH" : "",
                (getFlag() & RST) == RST ? "RST" : "",
                (getFlag() & FIN) == FIN ? "FIN" : "",
                (getFlag() & URG) == URG ? "URG" : "",
                getSourcePort() & 0xFFFF,
                getDestinationPort() & 0xFFFF,
                getSeqID(),
                getAckID());
    }
}
