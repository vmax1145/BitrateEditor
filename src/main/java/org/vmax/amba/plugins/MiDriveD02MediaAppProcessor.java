package org.vmax.amba.plugins;

import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MiDriveD02MediaAppProcessor implements PreProcessor, PostProcessor {
    FirmwareConfig cfg;

    @Override
    public MiDriveD02MediaAppProcessor withConfig(FirmwareConfig cfg) {
        this.cfg = cfg;
        return this;
    }


    @Override
    public byte[] preprocess(byte[] fw) throws Exception {
        int packedLen   = (int) Utils.readUInt(fw,0);
        int unpackedLen = (int) Utils.readUInt(fw, 0x4);
        if(packedLen+0x10!=fw.length) {
            throw new Exception("Packed length not equals expected len");
        }
        try (
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(fw,0x10,fw.length-0x10));
        ) {
            byte[] unpacked = new byte[unpackedLen];
            if(unpackedLen != gzipInputStream.read(unpacked)) {
                throw new Exception("Unpacked length not equals expected len");
            }
            return unpacked;
        }
    }

    @Override
    public byte[] postprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException {
        int unpackedLen = fwBytes.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] head = {
                0,0,0,0, 0,0,0,0,
                0x67,0x7A,0x69,0x70,0x68,0x65,0x61,0x64 //gziphead
        };
        baos.write(head);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
        gzipOutputStream.write(fwBytes);
        fwBytes = baos.toByteArray();
        Utils.writeUInt(fwBytes, 0,fwBytes.length-0x10);
        Utils.writeUInt(fwBytes, 0x4,unpackedLen);
        return fwBytes;
    }


}
