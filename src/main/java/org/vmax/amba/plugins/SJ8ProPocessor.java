package org.vmax.amba.plugins;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SJ8ProPocessor implements PreProcessor, PostProcessor {
    FirmwareConfig cfg;
    @Override
    public SJ8ProPocessor withConfig(FirmwareConfig cfg) {
        this.cfg=cfg;
        return this;
    }

    @Override
    public void postprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException {
        File ch = new File(cfg.getPostProcessor().getMd5fileName()+".mod");
        if(ch.exists()) {
            ch.delete();
        }
        byte[] md5 = Utils.calculateDigest(fwBytes);
        try(FileOutputStream fos = new FileOutputStream(ch)) {
            fos.write(md5);
        }
    }

    @Override
    public void preprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException, VerifyException {
        byte[] digest = Utils.calculateDigest(fwBytes);
        //System.out.println("File digest: " + Utils.hex(digest));
        byte[] check = FileUtils.readFileToByteArray(new File(cfg.getPreProcessor().getMd5fileName()));
        if(!Arrays.equals(digest,check)) {
            System.out.println("File md5 digest mismatch");
            return;
        }
    }
}
