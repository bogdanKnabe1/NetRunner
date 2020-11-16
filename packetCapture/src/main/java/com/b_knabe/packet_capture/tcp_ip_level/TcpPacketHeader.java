package com.b_knabe.packet_capture.tcp_ip_level;

import com.b_knabe.packet_capture.core.util.net_utils.Packets;

import java.util.Locale;

/**!
 * Low-level java networking
 *
 * */
public class TcpPacketHeader {

    /**
     *  TCP header format, 4 bytes per line, the first 20 bytes are fixed length
     * ０                                               15 16								     31
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜               source port             　｜       　destination port                       ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－ ｜
     * ｜　　　　　　　　　　　　　　　　　　　　　　　　 sequence　number　　　　　　　　　　　　        　　　　｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜　　　　　　　　　　　　　　　　　　　　　 acknowledgement 　number   　　　　　　　　　　　　　　　　  ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜　TCP header　　｜　　Protection　　      ｜Ｕ｜Ａ｜Ｐ｜Ｒ｜Ｓ｜Ｆ｜                             ｜
     * ｜　　　Length 　｜　　 stay　　            ｜Ｒ｜Ｃ｜Ｓ｜Ｓ｜Ｙ｜Ｉ｜　　　　　　window size         ｜
     * ｜　　（４ bit）   ｜　（６ bit）           ｜Ｇ｜Ｋ｜Ｈ｜Ｔ｜Ｎ｜Ｎ｜                              ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜              checksum               ｜           urgent　pointer）                        ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜                    Option + padding (0 or more 32-bit words, up to 40 bytes)             ｜
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * ｜                             Data (0 or more bytes)                                       |
     * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
     * */


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

    // ip message data
    public byte[] data;
    // The offset of the tcp message relative to the ip message (unit: byte), generally 20, it must be set again after receiving the ip message
    public int offset;

    public TcpPacketHeader(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    /**
     * After removing the lower 4 bits is the length of the header of the tcp message, each of which represents 4 bytes
     * So a total of 1111 (binary) * 4 = 60 bytes
     * @return
     */
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
