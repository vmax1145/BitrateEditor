package org.vmax.amba.plugins;

import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.fwsource.FwSource;

public interface PreProcessor {
    PreProcessor withConfig(FirmwareConfig cfg);

    byte[] preprocess(FwSource fwSource, byte[] fwBytes ) throws Exception;

}
