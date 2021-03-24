package org.vmax.amba.fwsource;

import org.vmax.amba.cfg.FirmwareConfig;

public abstract class FwDestination {
    FirmwareConfig cfg;

    public FwDestination(FirmwareConfig cfg){
        this.cfg = cfg;
    }
    public abstract void  save( byte[] bytes) throws Exception;
}
