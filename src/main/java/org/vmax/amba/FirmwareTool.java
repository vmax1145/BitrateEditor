package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public abstract class FirmwareTool<T extends FirmwareConfig> extends JFrame {

    public abstract String getStartMessage(FirmwareConfig cfg);
    public abstract void init(FirmwareConfig cfg, byte[] fwBytes);


    public abstract void exportData(File f);
    public abstract void importData(File f);
    public abstract void updateFW();
    public abstract Class<T> getConfigClz();


    public JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        fileMenu.add(new AbstractAction("Export settings data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File("."));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showSaveDialog(FirmwareTool.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(selectedFile.exists()) {
                        int dialogResult = JOptionPane.showConfirmDialog (jfc, "Owerwrite existing file?","Warning",JOptionPane.YES_NO_OPTION);
                        if(dialogResult != JOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    exportData(selectedFile);
                }
            }
        });

        fileMenu.add(new AbstractAction("Import settings data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File("."));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showOpenDialog(FirmwareTool.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(!selectedFile.exists()) {
                        JOptionPane.showMessageDialog(jfc, "File not exists","Warning",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    importData(selectedFile);
                }
            }
        });



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



}
