package org.vmax.bitrate;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Utils {

    public static long readUInt(RandomAccessFile raf, int addr) throws IOException {
        raf.seek(addr);
        byte[] b = new byte[Integer.BYTES];
        raf.read(b);
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }


    public static float readFloat(RandomAccessFile raf, int addr) throws IOException {
        raf.seek(addr);
        byte[] b = new byte[Float.BYTES];
        raf.read(b);
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    public static void writeFloat(RandomAccessFile raf, int addr, float val) throws IOException {
        raf.seek(addr);
        byte[] b = new byte[Float.BYTES];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(val);
        raf.write(b);
    }
    public static void writeUInt(RandomAccessFile raf, int addr, long val) throws IOException {
        raf.seek(addr);
        byte[] b = new byte[Integer.BYTES];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) val);
        raf.write(b);
    }

    public static String hex(long v) {
        String s = Long.toHexString(v & 0xffffffffL);
        while (s.length()<8) {
            s="0"+s;
        }
        return s ;
    }

    public static String hex(byte v) {
        String s = Long.toHexString(v & 0xffL);
        while (s.length()<2) {
            s="0"+s;
        }
        return s ;
    }


    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if(i>0) sb.append(" ");
            sb.append(hex(bytes[i]));
        }
        return sb.toString();
    }
}
