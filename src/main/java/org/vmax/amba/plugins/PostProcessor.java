package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface PostProcessor {
    PostProcessor withConfig(FirmwareConfig cfg);
    void postprocess(byte[] fwBytes) throws IOException, NoSuchAlgorithmException;

}
