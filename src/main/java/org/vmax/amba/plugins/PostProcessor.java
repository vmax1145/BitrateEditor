package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.fwsource.FwDestination;

public interface PostProcessor {
    PostProcessor withConfig(FirmwareConfig cfg);
    byte[] postprocess(FwDestination out, byte[] fwBytes) throws Exception;

}
