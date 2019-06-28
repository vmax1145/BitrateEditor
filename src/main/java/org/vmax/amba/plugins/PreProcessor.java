package org.vmax.amba.plugins;

import org.vmax.amba.bitrate.VerifyException;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface PreProcessor {
    PreProcessor withConfig(FirmwareConfig cfg);
    void preprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException, VerifyException;

}
