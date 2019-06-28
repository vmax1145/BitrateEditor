package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;

public abstract class FirmwareTool<T extends FirmwareConfig> extends JFrame {

    public abstract String getStartMessage();
    public abstract void init(FirmwareConfig cfg, byte[] fwBytes);

    public abstract Class<T> getConfigClz();
}
