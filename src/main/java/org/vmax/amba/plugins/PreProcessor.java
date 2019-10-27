package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;

public interface PreProcessor {
    PreProcessor withConfig(FirmwareConfig cfg);
    byte[] preprocess(byte[] fwBytes) throws Exception;

}
