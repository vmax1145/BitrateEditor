package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GenericTool extends FirmwareTool<GenericTableDataConfig> {

    private GenericTableDataConfig cfg;
    private GenericTableDataModel model;
    private GenericJTable editorPanel;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
            return "Generic editor";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) {
        this.cfg = (GenericTableDataConfig) acfg;
        model = new GenericTableDataModel(cfg.getTableDataConfig(), fwBytes);
        editorPanel = new GenericJTable(cfg.getTableDataConfig(),model);

        JMenuBar bar = buildMenu();
        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(600,400));
        add(tabs, BorderLayout.CENTER);

        tabs.add(this.cfg.getTableDataConfig().getLabel(), new JScrollPane(editorPanel));

        setJMenuBar(bar);
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
