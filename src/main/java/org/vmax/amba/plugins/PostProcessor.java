package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;

public interface PostProcessor {
    PostProcessor withConfig(FirmwareConfig cfg);
    byte[] postprocess(File out, byte[] fwBytes) throws Exception;

}
