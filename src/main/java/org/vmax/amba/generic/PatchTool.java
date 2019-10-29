package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PatchTool extends FirmwareTool<PatchToolConfig> {

    private PatchToolConfig cfg;
    private byte[] fwBytes;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Patch tool";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) {
        this.cfg = (PatchToolConfig) acfg;
        this.fwBytes = fwBytes;

        JMenuBar bar = buildMenu();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(600,400));
        add(tabs, BorderLayout.CENTER);


        setJMenuBar(bar);
    }


    @Override
    public void exportData(File f) {
        JOptionPane.showMessageDialog(this,"Not implemented yet");
    }

    @Override
    public void importData(File f) {
        JOptionPane.showMessageDialog(this,"Not implemented yet");
    }


    @Override
    public void updateFW() {
            try {
                Utils.saveFirmware(this, cfg, fwBytes);
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Oooops! See error stream for details");
            }
    }

    @Override
    public Class<PatchToolConfig> getConfigClz() {
        return PatchToolConfig.class;
    }
}
