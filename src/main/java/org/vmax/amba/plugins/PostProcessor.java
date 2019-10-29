package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;

public interface PostProcessor {
    PostProcessor withConfig(FirmwareConfig cfg);
    byte[] postprocess(byte[] fwBytes) throws Exception;

}
