package org.vmax.bitrate.plugins;

import org.vmax.bitrate.Utils;
import org.vmax.bitrate.cfg.Config;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;

public class Yi4kProcessor implements PreProcessor, PostProcessor {

    private static final int ALL_CRC_POSITION = 0x20;
    private static final String SECRET = "xiaoyi firmware--z18";
    private static final int Z18_SECRET_INIT = 0x01E4;
    private static final int Z18_ENCODED_DATA_OFFSET =0x08AC;


    private Config cfg;

    public Yi4kProcessor() {
    }

    @Override
    public Yi4kProcessor withConfig(Config cfg) {
        this.cfg = cfg;
        return this;
    }

    @Override
    public void preprocess() throws IOException, NoSuchAlgorithmException {
        cfg.getPreProcessor().getFwFileName();
        File fin = new File(cfg.getPreProcessor().getFwFileName());
        if (!fin.exists()) {
            throw new IOException("FW file " + fin.getName() + " not found");
        }
        try (RandomAccessFile raf = new RandomAccessFile(fin,"r")){
            System.out.println("infile digest=" + Utils.hex(Utils.calculateDigest(raf)));
        }

        File fout = new File(cfg.getFwFileName());
        if (fout.exists()) {
            throw new IOException("Preprocessed file " + fout.getName() + " already exists");
        } else {
            fout.createNewFile();
            fout.deleteOnExit();
        }

        byte[] secretbytes = SECRET.getBytes("ASCII");


        int totalLen = (int) fin.length();
        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);


        try (FileInputStream is = new FileInputStream(fin);
             FileOutputStream os = new FileOutputStream(fout);
        ) {
            is.getChannel().read(buf);
            buf.flip();
            Utils.crcCheck(buf, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);

            buf.position(Z18_SECRET_INIT);
            int secretStart = buf.getInt();

            byte[] bytes = new byte[totalLen - Z18_ENCODED_DATA_OFFSET];
            buf.position(Z18_ENCODED_DATA_OFFSET);
            buf.get(bytes);
            for( int i=0 ; i<bytes.length; i++) {
                bytes[i] = (byte) (bytes[i]^ secretbytes[(i+secretStart)%secretbytes.length]);
            }
            buf.position(Z18_ENCODED_DATA_OFFSET);
            buf.put(bytes);
            Utils.crcCheck(buf, Z18_ENCODED_DATA_OFFSET, totalLen, ALL_CRC_POSITION);

            FileChannel fc = os.getChannel();
            buf.position(0);
            fc.write(buf);
            fc.close();
        }
    }


    public void postprocess() throws IOException, NoSuchAlgorithmException {
        File fin = new File(cfg.getFwFileName() + ".mod");
        if (!fin.exists()) {
            throw new IOException("FW file " + fin.getName() + " not found");
        }
        File fout = new File(cfg.getPostProcessor().getFwFileName() + ".mod");
        if (fout.exists()) {
            throw new IOException("Mod file " + fout.getName() + " already exists");
        } else {
            fout.createNewFile();
        }
        int totalLen = (int) fin.length();
        ByteBuffer buf = ByteBuffer.allocate(totalLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        try (FileInputStream is = new FileInputStream(fin);
             FileOutputStream os = new FileOutputStream(fout);
        ) {
            is.getChannel().read(buf);
            buf.position(Z18_SECRET_INIT);
            int secretStart = buf.getInt();
            byte[] secretbytes = SECRET.getBytes("ASCII");

            Utils.crcSet(buf, Z18_ENCODED_DATA_OFFSET, totalLen, ALL_CRC_POSITION);

            byte[] bytes = new byte[totalLen - Z18_ENCODED_DATA_OFFSET];
            buf.position(Z18_ENCODED_DATA_OFFSET);
            buf.get(bytes);
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (bytes[i] ^ secretbytes[(i + secretStart) % secretbytes.length]);
            }
            buf.position(Z18_ENCODED_DATA_OFFSET);
            buf.put(bytes);

            Utils.crcSet(buf, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);

            FileChannel fc = os.getChannel();
            buf.position(0);
            fc.write(buf);
            fc.close();
        }
        try (RandomAccessFile raf = new RandomAccessFile(fout, "r")) {
            System.out.println("output digest=" + Utils.hex(Utils.calculateDigest(raf)));
        }
        fin.delete();
    }
}

