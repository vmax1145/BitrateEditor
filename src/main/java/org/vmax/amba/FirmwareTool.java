package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.ImportAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public abstract class FirmwareTool<T extends FirmwareConfig> extends JFrame {

    public abstract String getStartMessage(FirmwareConfig cfg);
    public abstract void init(FirmwareConfig cfg, byte[] fwBytes) throws Exception;

    public abstract void updateFW();
    public abstract Class<? extends T> getConfigClz();


    public JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        JMenu exportM = new JMenu("Export");

        getExportActions().forEach(
                exportM::add
        );
        fileMenu.add(exportM);

        JMenu importM = new JMenu("Import");
        getImportActions().forEach(
                importM::add
        );
        fileMenu.add(importM);

        fileMenu.add(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateFW();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FirmwareTool.this, "Error svaing firmware","Error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        return bar;
    }

    protected abstract List<ImportAction> getImportActions();

    protected abstract List<ExportAction> getExportActions();


}
