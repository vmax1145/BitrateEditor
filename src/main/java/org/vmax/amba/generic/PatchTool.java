package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Patch;
import org.vmax.amba.cfg.PatchToolConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public abstract class PatchTool<T extends PatchToolConfig> extends FirmwareTool<T> {

    private T cfg;
    private byte[] fwBytes;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Patch tool";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) throws Exception {
        this.cfg = (T) acfg;
        this.fwBytes = fwBytes;

        JMenuBar bar = buildMenu();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(600,400));
        add(tabs, BorderLayout.CENTER);

        List<Patch> patchList = loadPatches(cfg);

        setJMenuBar(bar);
    }

    protected abstract List<Patch> loadPatches(T cfg) throws Exception;



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

}
