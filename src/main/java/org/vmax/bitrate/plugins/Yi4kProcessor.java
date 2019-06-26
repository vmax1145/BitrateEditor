package org.vmax.bitrate.plugins;

import org.vmax.bitrate.Utils;
import org.vmax.bitrate.cfg.Config;

import java.io.IOException;

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
    public void preprocess(byte[] fwBytes) throws IOException {
        byte[] secretbytes = SECRET.getBytes("ASCII");


        Utils.crcCheck(fwBytes, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);

        int secretStart = (int) Utils.readUInt(fwBytes,Z18_SECRET_INIT);
        for( int i=0 ; i<fwBytes.length-Z18_ENCODED_DATA_OFFSET; i++) {
            fwBytes[i+Z18_ENCODED_DATA_OFFSET] = (byte) (fwBytes[i+Z18_ENCODED_DATA_OFFSET]^ secretbytes[(i+secretStart)%secretbytes.length]);
        }
        Utils.crcCheck(fwBytes, Z18_ENCODED_DATA_OFFSET, fwBytes.length-Z18_ENCODED_DATA_OFFSET, ALL_CRC_POSITION);

    }


    public void postprocess(byte[] fwBytes) throws IOException {

        int secretStart = (int) Utils.readUInt(fwBytes,Z18_SECRET_INIT);
        byte[] secretbytes = SECRET.getBytes("ASCII");

        Utils.crcSet(fwBytes, Z18_ENCODED_DATA_OFFSET, fwBytes.length-Z18_ENCODED_DATA_OFFSET, ALL_CRC_POSITION);

        for( int i=Z18_ENCODED_DATA_OFFSET ; i<fwBytes.length; i++) {
                fwBytes[i] = (byte) (fwBytes[i]^ secretbytes[(i-Z18_ENCODED_DATA_OFFSET+secretStart)%secretbytes.length]);
        }

        Utils.crcSet(fwBytes, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);
    }
}

