package org.vmax.bitrate;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

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

    public static void crcCheck(ByteBuffer buf, int from, int to, int crcAddr) {
        CRC32 crc = new CRC32();
        buf.position(from);
        for(int i=from ; i<to; i++) {
            crc.update(buf.get());
        }
        buf.position(crcAddr);
        long expected = (buf.getInt()&0xffffffffL);
        if(  expected != crc.getValue()) {
            System.out.println("CRC expected:"+hex(expected)+" actual:"+hex(crc.getValue()));
        }
    }
    public static void crcSet(ByteBuffer buf, int from, int to, int crcAddr) {
        CRC32 crc = new CRC32();
        buf.position(from);
        for(int i=from ; i<to; i++) {
            crc.update(buf.get());
        }
        buf.position(crcAddr);
        buf.putInt((int) crc.getValue());
    }

    public static byte[] calculateDigest(RandomAccessFile raf) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        raf.seek(0);
        byte[] buf = new byte[4096];
        int nread;
        while((nread = raf.read(buf))>=0) {
            md.update(buf,0,nread);
        }
        byte[] digest = md.digest();
        byte[] digestReversed = new byte[digest.length];
        for (int i = 0; i < digest.length; i++) {
            digestReversed[i] = digest[i / 4 * 4 + 3 - (i % 4)];
        }
        return digestReversed;
    }
}
