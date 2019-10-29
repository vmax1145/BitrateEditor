package org.vmax.amba.plugins;

import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.midrive.MiGzipOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class MiDriveD02MediaAppProcessor implements PreProcessor, PostProcessor {
    FirmwareConfig cfg;

    @Override
    public MiDriveD02MediaAppProcessor withConfig(FirmwareConfig cfg) {
        this.cfg = cfg;
        return this;
    }


    @Override
    public byte[] preprocess(byte[] fw) throws Exception {
        System.out.println("Packed len="+fw.length);
        int packedLen   = (int) Utils.readUInt(fw,0);
        int unpackedLen = (int) Utils.readUInt(fw, 0x4);
        if(packedLen+0x10!=fw.length) {
            throw new Exception("Packed length not equals expected len");
        }
        try (
             GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(fw,0x10,fw.length-0x10));
        ) {
            byte[] unpacked = new byte[unpackedLen];

            int nread;
            int offset = 0;
            while( (nread = gzipInputStream.read(unpacked, offset, unpackedLen)) > 0) {
                unpackedLen-=nread;
                offset+=nread;
            }
            if(unpackedLen!=0) {
                throw new Exception("Unpacked length mismatch");
            }
            return unpacked;
        }
    }

    @Override
    public byte[] postprocess(byte[] fwBytes) throws IOException {
        int unpackedLen = fwBytes.length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] head = {
                0,0,0,0, 0,0,0,0,
                0x67,0x7A,0x69,0x70,0x68,0x65,0x61,0x64 //gziphead
        };
        baos.write(head);
        MiGzipOutputStream gzipOutputStream = new MiGzipOutputStream(baos,4096, true);

        gzipOutputStream.write(fwBytes);
        gzipOutputStream.finish();
        fwBytes = baos.toByteArray();
        Utils.writeUInt(fwBytes, 0,fwBytes.length-0x10);
        Utils.writeUInt(fwBytes, 0x4,unpackedLen);
        System.out.println("Packed len="+fwBytes.length);
        return fwBytes;
    }


}
