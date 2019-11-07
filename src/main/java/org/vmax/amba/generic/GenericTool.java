package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.tabledata.ImageConfig;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GenericTool extends FirmwareTool<GenericTableDataConfig> {

    private GenericTableDataConfig cfg;

    private byte[] fwBytes;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
            return "Generic editor";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) {
        this.cfg = (GenericTableDataConfig) acfg;
        this.fwBytes = fwBytes;

        JMenuBar bar = buildMenu();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(600,400));
        add(tabs, BorderLayout.CENTER);

        for(TableDataConfig tdcfg : this.cfg.getTableDataConfigs()) {
            GenericTableDataModel model = new GenericTableDataModel(tdcfg, fwBytes);
            GenericJTable editorPanel = new GenericJTable(tdcfg, model);
            tabs.add(tdcfg.getLabel(), new JScrollPane(editorPanel));
        }
        for(ParamsConfig pcfg : this.cfg.getParamsTabs()) {
            GenericParamsDataModel model = new GenericParamsDataModel(pcfg, fwBytes);
            GenericParamsTable editorPanel = new GenericParamsTable(pcfg, model);
            tabs.add(pcfg.getLabel(), new JScrollPane(editorPanel));
        }
        for(ImageConfig icfg : this.cfg.getImageTabs()) {
            JPanel editorPanel = new JPanel();
            tabs.add(icfg.getLabel(), new JScrollPane(editorPanel));
        }

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
    public Class<GenericTableDataConfig> getConfigClz() {
        return GenericTableDataConfig.class;
    }
}
