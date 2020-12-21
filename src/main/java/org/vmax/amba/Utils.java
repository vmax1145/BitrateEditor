package org.vmax.amba;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.cfg.*;
import org.vmax.amba.plugins.PostProcessor;
import org.vmax.amba.plugins.PreProcessor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

public class Utils {

    public static long readUInt(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xffffffffL;
    }

    public static long readInt(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }


    public static float readFloat(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Float.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getFloat();
    }

    public static void writeFloat(byte[] fw, int addr, float val) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Float.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(val);
    }

    public static void writeUInt(byte[] fw, int addr, long val) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Integer.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt((int) val);
    }

    public static String hex(long v) {
        String s = Long.toHexString(v & 0xffffffffL);
        while (s.length() < 8) {
            s = "0" + s;
        }
        return s;
    }

    public static String hex(byte v) {
        String s = Long.toHexString(v & 0xffL);
        while (s.length() < 2) {
            s = "0" + s;
        }
        return s;
    }


    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(hex(bytes[i]));
        }
        return sb.toString();
    }

    public static void crcCheck(byte[] fw, int from, int len, int crcAddr) throws VerifyException {
        CRC32 crc = new CRC32();
        crc.update(fw, from, len);

        long expected = readUInt(fw, crcAddr);
        if (expected != crc.getValue()) {
            throw new VerifyException("CRC " + crcAddr + " expected:" + hex(expected) + " actual:" + hex(crc.getValue()));
        }
    }

    public static void crcSet(byte[] buf, int from, int len, int crcAddr) {
        CRC32 crc = new CRC32();
        crc.update(buf, from, len);
        writeUInt(buf, crcAddr, crc.getValue());
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
        if (!f.exists()) {
            System.out.println("FW file " + cfg.getFwFileName() + " not found");
            System.exit(0);
        }
        byte[] fwBytes = FileUtils.readFileToByteArray(f);

        if (cfg.getPreProcessor() != null) {
            PreProcessor preprocessor = (PreProcessor) Class.forName(cfg.getPreProcessor().getClassName()).newInstance();
            preprocessor.withConfig(cfg);
            fwBytes = preprocessor.preprocess(f,fwBytes);
        }


        try {
            List<Verify> verifications = cfg.getVerify();
            performVerifications(fwBytes, verifications);
        }
        catch (VerifyException e) {
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "Firmware and config mismatch",
                    JOptionPane.ERROR_MESSAGE
            );
            throw e;
        }

        //FileUtils.writeByteArrayToFile(new File(f.getName()+".preprocessed"),fwBytes);

        return fwBytes;
    }

    public static void performVerifications(byte[] fwBytes, List<Verify> verifications) throws UnsupportedEncodingException, VerifyException, JsonProcessingException {
        for (Verify verify : verifications) {
            if (verify.getVal() != null) {
                byte[] bytes = verify.getVal().getBytes("ASCII");

                for (int i = 0, addr = verify.getAddr(); i < bytes.length; i++, addr++) {
                    byte b = bytes[i];
                    if (fwBytes[addr] != b) {
                        throw new VerifyException("Config Verify fail addr:" + verify.getAddr() + " : " + verify.getVal());
                    }
                }
            } else if (verify.getInt32val() != null) {
                if (Utils.readUInt(fwBytes, verify.getAddr()) != verify.getInt32val()) {
                    throw new VerifyException("Config Verify fail addr: " + verify.getAddr() + " : " + verify.getInt32val());
                }
            } else if (verify.getCrc() != null) {
                try {
                    crcCheck(fwBytes, verify.getCrc().getFromAddr(), verify.getCrc().getLen(), verify.getAddr());
                    //System.out.println("CRC check OK:" + toJson(verify));
                }
                catch (Exception e) {
                    System.out.println("CRC check FAIL:"+e.getMessage()+"\n" + toJson(verify));
                    JOptionPane.showMessageDialog(null,
                            new ObjectMapper().writer().writeValueAsString(verify),
                            "Firmware CRC check fail",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
            if(!verify.getVerifies().isEmpty()) {
                performVerifications(fwBytes,verify.getVerifies());
            }
        }
    }

    public static void saveFirmware(JFrame tool, FirmwareConfig cfg, byte[] fwBytes) throws Exception {

        updateCRC(cfg.getVerify(),fwBytes);

        File out = null;
        if (cfg.isShowFileDialog() || cfg.getFwFileName() == null) {
            JFileChooser jfc = new JFileChooser(new File("."));
            if (cfg.getFwFileName() != null) {
                jfc.setSelectedFile(new File(cfg.getFwFileName() + ".mod"));
            }
            if (jfc.showSaveDialog(tool) == JFileChooser.APPROVE_OPTION) {
                out = jfc.getSelectedFile();
            }
        } else {
            out = new File(cfg.getFwFileName() + ".mod");
            if (out.exists()) {
                JOptionPane.showMessageDialog(null, "File " + out.getName() + " already exists");
                return;
            }
        }
        if (out != null) {
            if (cfg.getPostProcessor() != null) {
                PostProcessor postprocessor = (PostProcessor) Class.forName(cfg.getPostProcessor().getClassName()).newInstance();
                postprocessor.withConfig(cfg);
                fwBytes = postprocessor.postprocess(out,fwBytes);
            }
            FileUtils.writeByteArrayToFile(out, fwBytes);
        }

    }

    private static void updateCRC(List<Verify> verifies, byte[] fwBytes) {
        for (Verify verify : verifies) {
            if(verify.getVerifies()!=null && !verify.getVerifies().isEmpty()) {
                updateCRC(verify.getVerifies(), fwBytes);
            }
            if (verify.getCrc() != null) {
                crcSet(fwBytes, verify.getCrc().getFromAddr(), verify.getCrc().getLen(), verify.getAddr());
            }
        }
    }


    //------------------


    public static long readUShort(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getShort() & 0xffffL;
    }


    public static void writeUShort(byte[] fw, int addr, long val) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) val);
    }


    public static String hex(long v, int digits) {
        String s = Long.toHexString(v & 0xffffffffL);
        while (s.length() < digits) {
            s = "0" + s;
        }
        return s;
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

    public static long readUByte(byte[] fw, int addr) {
        return fw[addr] & 0xffL;
    }

    public static void writeUByte(byte[] fw, int addr, long val) {
        fw[addr] = (byte) val;
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }


    public static List<Integer> findArray(byte[] fw, byte[] searchFor) {
        List<Integer> addr = new ArrayList<>();
        int subArrayLength = searchFor.length;
        if (subArrayLength != 0) {
            int limit = fw.length - subArrayLength;
            outer:
            for (int i = 0; i <= limit; i++) {
                for (int j = 0; j < subArrayLength; j++) {
                    if (searchFor[j] != fw[i + j]) {
                        continue outer;
                    }
                }
                addr.add(i);
                i += subArrayLength;
            }
        }
        return addr;
    }

    public static Map<Integer,SectionInfo> getSectionInfos(byte[] fwBytes, List<Integer> filesSection, int fnLen) throws UnsupportedEncodingException {
        Map<Integer,SectionInfo> sections = new HashMap<>();
        int addr = 560;
        for (int i = 0, num=1; i < 10; i+=2, num++) {
            int len = (int) readInt(fwBytes,0x30+i*4);
            int  sectionLen = (int) readInt(fwBytes,addr+0xc);
            SectionInfo si = new SectionInfo();
            si.addr = addr;
            si.len = sectionLen;
            si.crc = readInt(fwBytes,addr)&0xffffffffL;
            si.num = num;

            CRC32 crc = new CRC32();
            crc.update(fwBytes, addr+256, sectionLen);
            if(si.crc != crc.getValue()) {
                System.out.println("CRC FAIL : "+ hex(si.crc) + " " + hex(crc.getValue()));
            }
            sections.put(si.num, si);
            if(filesSection.contains(si.num)) {
                int fatAddr = si.addr+ SectionAddr.SECTION_HEADER_LEN+4;
                int n = (int) Utils.readInt(fwBytes,fatAddr);
                fatAddr+=4;
                for(int f=0;f<n;f++) {
                    FileInfo fi = new FileInfo();
                    fi.name = Utils.readString(fwBytes,fatAddr,fnLen, "ASCII");
                    fi.len  = (int) Utils.readInt(fwBytes,fatAddr+fnLen);
                    fi.addr = (int) Utils.readInt(fwBytes,fatAddr+fnLen+0x4);
                    fi.crcAddr  = fatAddr+fnLen+0x8;
                    crc = new CRC32();
                    crc.update(fwBytes,fi.addr+si.addr+SectionAddr.SECTION_HEADER_LEN,fi.len);
                    System.out.println(fi.name+" "+fi.len+" "+(fi.addr+si.addr+SectionAddr.SECTION_HEADER_LEN)+" "+fi.crcAddr );
                    System.out.println(crc.getValue()+" "+(Utils.readInt(fwBytes,fi.crcAddr)&0xffffffffL));
                    si.files.put(fi.name,fi);
                    fatAddr+=fnLen+0xC;
                }
            }
            addr+=len;
        }
        return sections;
    }

    public static String readString(byte[] fw, int addr, int maxLen, String charset) throws UnsupportedEncodingException {
        int end=addr;
        int max = Math.max(addr+maxLen,fw.length);
        for(; end < max; end++) {
            if(fw[end]==0) {
                break;
            }
        }
        return new String(fw,addr,end-addr,charset);
    }

    public static int calcAbsAddr(SectionAddr sectionAddr, Map<Integer, SectionInfo> sections, byte[] fw) throws VerifyException {
        SectionInfo si = sections.get(sectionAddr.getSectionNum());
        if(si == null) {
            throw new VerifyException("No section: "+sectionAddr.getSectionNum());
        }
        int addr = si.addr + SectionAddr.SECTION_HEADER_LEN;
        if(sectionAddr.getFileName()!=null) {
            FileInfo fi = si.files.get(sectionAddr.getFileName());
            if(fi == null) {
                throw new VerifyException("No file:" + sectionAddr.getFileName() + " in section " + sectionAddr.getSectionNum());
            }
            addr+=fi.addr;
        }
        if(sectionAddr.getFindHex()!=null) {
            addr += findHexOffset(fw,sectionAddr.getFindHex(),addr, si.addr+si.len);
        }
        addr+=sectionAddr.getRelAddr();
        return addr;
    }

    private static int findHexOffset(byte[] fw, String findHex, int addr, int maxAddr) throws VerifyException {
        byte[] sample = new byte[findHex.length()/2];
        for(int i=0;i<sample.length;i++) {
            sample[i] = (byte) Integer.parseInt(findHex.substring(i*2,(i+1)*2),16);
        }
        int inx = indexOf(fw,addr,maxAddr,sample);
        inx-=addr;
        if(inx < 0) {
            throw new VerifyException("bytes: "+findHex+" not found");
        }
        return inx;
    }


    public static int indexOf(byte[] array, int from, int maxAddr, byte[] target) {
        if (target.length == 0) {
            return -2;
        }
        int max =  Math.min(array.length,maxAddr) - target.length + 1;
        outer:
        for (int i = from; i < max ; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static String toJson(Object fc) throws JsonProcessingException {
        String s = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .writerWithDefaultPrettyPrinter().writeValueAsString(fc);
        return s;
    }
}
