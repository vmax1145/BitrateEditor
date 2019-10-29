package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;


public class AppMain {

    public static void main(String ... args) throws Exception {

        if(args.length<1) {
            System.out.println("config name required");
            return;
        }

        System.out.println("Firmware editor tools ( by v_max )");

        FirmwareConfig basiccfg = FirmwareConfig.readConfig(FirmwareConfig.class, args[0]);
        FirmwareTool tool = (FirmwareTool) Class.forName(basiccfg.getToolClass()).newInstance();

        Class<FirmwareConfig> configClass = tool.getConfigClz();
        FirmwareConfig cfg = FirmwareConfig.readConfig(configClass, args[0]);
        tool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        byte[] fwBytes = null;
        if(cfg.getFwFileName()!=null && !cfg.isShowFileDialog()) {
            fwBytes = Utils.loadFirmware(cfg);
        }
        else {
            JFileChooser jfc = new JFileChooser(new File("."));
            if(cfg.getFwFileName()!=null) {
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().equals(cfg.getFwFileName());
                    }

                    @Override
                    public String getDescription() {
                        return "Firmware file";
                    }
                };
                jfc.addChoosableFileFilter(filter);
            }

            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                if(selectedFile.exists()) {
                    fwBytes = Utils.loadFirmware(cfg,selectedFile);
                }
            }
        }
        if(fwBytes!=null) {
            startTool(cfg, tool, fwBytes);
        }
    }

    private static void startTool(FirmwareConfig cfg, FirmwareTool tool, byte[] fwBytes) {
        System.out.println(tool.getStartMessage(cfg));
        tool.init(cfg, fwBytes);
        tool.pack();
        tool.setVisible(true);
    }

}
