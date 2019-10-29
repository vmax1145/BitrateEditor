package org.vmax.amba;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Verify;
import org.vmax.amba.plugins.PostProcessor;
import org.vmax.amba.plugins.PreProcessor;

import javax.swing.*;
import java.io.File;
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

    protected static byte[] loadFirmware(FirmwareConfig cfg) throws Exception {

        File f = new File(cfg.getFwFileName());
        return loadFirmware(cfg, f);
    }

    public static byte[] loadFirmware(FirmwareConfig cfg, File f) throws Exception {
        if(!f.exists()) {
            System.out.println("FW file "+cfg.getFwFileName()+" not found");
            System.exit(0);
        }
        byte[] fwBytes = FileUtils.readFileToByteArray(f);

        if(cfg.getPreProcessor()!=null) {
            PreProcessor preprocessor = (PreProcessor) Class.forName(cfg.getPreProcessor().getClassName()).newInstance();
            preprocessor.withConfig(cfg);
            fwBytes = preprocessor.preprocess(fwBytes);
        }


        for(Verify verify : cfg.getVerify()) {
            if(verify.getVal()!=null) {
                byte[] bytes = verify.getVal().getBytes("ASCII");

                for (int i = 0, addr = verify.getAddr(); i < bytes.length; i++, addr++) {
                    byte b = bytes[i];
                    if (fwBytes[addr] != b) {
                        throw new VerifyException("Verify fail:" + verify.getVal());
                    }
                }
            }
            else if(verify.getCrc()!=null) {
                crcCheck(fwBytes,verify.getCrc().getFromAddr(), verify.getCrc().getLen(), verify.getAddr());
            }
        }
        return fwBytes;
    }

    public static void saveFirmware(JFrame tool, FirmwareConfig cfg, byte[] fwBytes ) throws Exception {
            for(Verify verify : cfg.getVerify()) {
                if(verify.getCrc()!=null) {
                    crcSet(fwBytes,verify.getCrc().getFromAddr(), verify.getCrc().getLen(), verify.getAddr());
                }
            }
            if(cfg.getPostProcessor()!=null) {
                PostProcessor postprocessor = (PostProcessor) Class.forName(cfg.getPostProcessor().getClassName()).newInstance();
                postprocessor.withConfig(cfg);
                fwBytes = postprocessor.postprocess(fwBytes);
            }

            File out = null;
            if(cfg.isShowFileDialog() || cfg.getFwFileName()==null) {
                JFileChooser jfc = new JFileChooser(new File("."));
                if(cfg.getFwFileName()!=null) {
                    jfc.setSelectedFile(new File(cfg.getFwFileName() + ".mod"));
                }
                if(jfc.showSaveDialog(tool) == JFileChooser.APPROVE_OPTION) {
                    out = jfc.getSelectedFile();
                }
            }
            else {
                out = new File(cfg.getFwFileName() + ".mod");
                if(out.exists()) {
                    JOptionPane.showMessageDialog(null,"File "+out.getName()+" already exists");
                    return;
                }
            }
            if(out!=null) {
                FileUtils.writeByteArrayToFile(out, fwBytes);
            }

    }


    //------------------



    public static long readUShort(byte[] fw, int addr)  {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort() & 0xffffL;
    }


    public static void writeUShort(byte[] fw, int addr, long val) {
        ByteBuffer bb = ByteBuffer.wrap(fw,addr,Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) val);
    }


    public static String hex(long v, int digits) {
        String s = Long.toHexString(v & 0xffffffffL);
        while (s.length()<digits) {
            s="0"+s;
        }
        return s ;
    }


//    public static byte[] loadTable(Config cfg) throws IOException {
//        File f = new File(cfg.getFileName());
//        if(!f.exists()) {
//            System.out.println("File "+cfg.getFileName()+" not found");
//            System.exit(0);
//        }
//        int len = cfg.getNcol() * cfg.getNrow() * cfg.getType().getByteLen();
//        byte[] bytes = new byte[len];
//        try (FileInputStream fis = new FileInputStream(f)) {
//            fis.skip(cfg.getTableAddr());
//            fis.read(bytes);
//        }
//        return bytes;
//    }

    public static long readUByte(byte[] fw, int addr)  {
        return fw[addr] & 0xffL;
    }
    public static void writeUByte(byte[] fw, int addr, long val) {
        fw[addr]= (byte) val;
    }

}
