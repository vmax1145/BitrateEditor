package org.vmax.bitrate;

import org.vmax.bitrate.bitrateui.VerifyException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class Utils {

    public static long readUInt(byte[] fw, int addr)  {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }


    public static float readFloat(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw,addr,Float.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    public static void writeFloat(byte[] fw, int addr, float val)  {
        ByteBuffer bb = ByteBuffer.wrap(fw,addr,Float.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(val);
    }
    public static void writeUInt(byte[] fw, int addr, long val) {
        ByteBuffer bb = ByteBuffer.wrap(fw,addr,Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) val);
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

    public static void crcCheck(byte[] fw, int from, int len, int crcAddr) throws VerifyException {
        CRC32 crc = new CRC32();
        crc.update(fw, from, len);

        long expected = readUInt(fw,crcAddr);
        if(  expected != crc.getValue()) {
            throw new VerifyException("CRC "+crcAddr+" expected:"+hex(expected)+" actual:"+hex(crc.getValue()));
        }
    }
    public static void crcSet(byte[] buf, int from, int len, int crcAddr) {
        CRC32 crc = new CRC32();
        crc.update(buf,from,len);
        writeUInt(buf, crcAddr,crc.getValue());
    }

    public static byte[] calculateDigest(byte[] fw) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(fw);
        byte[] digest = md.digest();
        byte[] digestReversed = new byte[digest.length];
        for (int i = 0; i < digest.length; i++) {
            digestReversed[i] = digest[i / 4 * 4 + 3 - (i % 4)];
        }
        return digestReversed;
    }

    public static String toHexValue(float val) {
        byte[] b = new byte[Float.BYTES];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(val);
        bb.position(0);
        return hex(bb.getInt());
    }
}
