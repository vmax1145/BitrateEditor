package org.vmax.midrive;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.plugins.PostProcessor;
import org.vmax.amba.plugins.PreProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
            FileUtils.writeByteArrayToFile(new File("media_app.unpacked"), unpacked);

//            System.out.println(Utils.findArray(unpacked, new byte[]{
//                    2, 0, 0, 0, (byte)0x80, (byte)0xFD, (byte)0x80,  (byte)0xFD
//            }).stream().map(i->i-8).collect(Collectors.toList()));
            return unpacked;
        }
    }

    @Override
    public byte[] postprocess(byte[] fwBytes) throws Exception {
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
        byte[] packed = baos.toByteArray();
        Utils.writeUInt(packed, 0,packed.length-0x10);
        Utils.writeUInt(packed, 0x4,unpackedLen);
        System.out.println("Packed len="+packed.length);
        return packed;
    }


}
