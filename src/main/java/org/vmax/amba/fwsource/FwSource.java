package org.vmax.amba.fwsource;

import org.vmax.amba.cfg.FirmwareConfig;

public abstract class FwSource {
    FirmwareConfig cfg;

    public FwSource(FirmwareConfig cfg){
        this.cfg = cfg;
    }
    public abstract byte[] load( ) throws Exception;
}
