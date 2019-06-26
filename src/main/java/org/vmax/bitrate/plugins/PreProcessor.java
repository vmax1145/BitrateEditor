package org.vmax.bitrate.plugins;

import org.vmax.bitrate.cfg.Config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface PreProcessor {
    PreProcessor withConfig(Config cfg);
    void preprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException;

}
