package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.cfg.FirmwareConfig;

import java.io.File;

public class GenericTool extends FirmwareTool<GenericTableDataConfig> {

    private GenericTableDataConfig cfg;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
            return "Generic editor";
    }

    @Override
    public void init(FirmwareConfig cfg, byte[] fwBytes) {
        this.cfg = (GenericTableDataConfig) cfg;
    }

    @Override
    public void exportData(File f) {

    }

    @Override
    public void importData(File f) {

    }

    @Override
    public void updateFW() {

    }

    @Override
    public Class<GenericTableDataConfig> getConfigClz() {
        return GenericTableDataConfig.class;
    }
}
