package org.vmax.amba.yuv;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.yuv.config.YUVConfig;

public class YUVTool extends FirmwareTool<YUVConfig> {
    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Saturation editor";
    }

    @Override
    public void init(FirmwareConfig cfg, byte[] fwBytes) {
        System.out.println("123");
    }

    @Override
    public Class<YUVConfig> getConfigClz() {
        return YUVConfig.class;
    }
}
