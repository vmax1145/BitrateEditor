package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;

public interface PreProcessor {
    PreProcessor withConfig(FirmwareConfig cfg);

    byte[] preprocess(File file, byte[] fwBytes ) throws Exception;

}
