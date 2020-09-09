package org.vmax.amba.plugins;

import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;

public class Yi4kProcessor implements PreProcessor, PostProcessor {

    private static final int ALL_CRC_POSITION = 0x20;
    private static final String SECRET = "xiaoyi firmware--z18";
    private static final int Z18_SECRET_INIT = 0x01E4;
    private static final int Z18_ENCODED_DATA_OFFSET =0x08AC;


    private FirmwareConfig cfg;

    public Yi4kProcessor() {
    }

    @Override
    public Yi4kProcessor withConfig(FirmwareConfig cfg) {
        this.cfg = cfg;
        return this;
    }

    @Override
    public byte[] preprocess( File file, byte[] fwBytes) throws IOException, VerifyException {
        byte[] secretbytes = SECRET.getBytes("ASCII");
        CRC32 crcH = new CRC32();

        for(int i=0;i<0x8a8;i++) {
            crcH.update(fwBytes[i]);
        }

        Utils.crcCheck(fwBytes, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);

        int secretStart = (int) Utils.readUInt(fwBytes,Z18_SECRET_INIT);
        for( int i=0 ; i<fwBytes.length-Z18_ENCODED_DATA_OFFSET; i++) {
            fwBytes[i+Z18_ENCODED_DATA_OFFSET] = (byte) (fwBytes[i+Z18_ENCODED_DATA_OFFSET]^ secretbytes[(i+secretStart)%secretbytes.length]);
        }
        Utils.crcCheck(fwBytes, Z18_ENCODED_DATA_OFFSET, fwBytes.length-Z18_ENCODED_DATA_OFFSET, ALL_CRC_POSITION);
        Utils.crcCheck(fwBytes, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);
        return fwBytes;
    }


    public byte[] postprocess(File out, byte[] fwBytes) throws IOException {

        int secretStart = (int) Utils.readUInt(fwBytes,Z18_SECRET_INIT);
        byte[] secretbytes = SECRET.getBytes("ASCII");

        Utils.crcSet(fwBytes, Z18_ENCODED_DATA_OFFSET, fwBytes.length-Z18_ENCODED_DATA_OFFSET, ALL_CRC_POSITION);

        for( int i=Z18_ENCODED_DATA_OFFSET ; i<fwBytes.length; i++) {
                fwBytes[i] = (byte) (fwBytes[i]^ secretbytes[(i-Z18_ENCODED_DATA_OFFSET+secretStart)%secretbytes.length]);
        }

        Utils.crcSet(fwBytes, 0, Z18_ENCODED_DATA_OFFSET-4, Z18_ENCODED_DATA_OFFSET-4);
        return fwBytes;
    }
}

