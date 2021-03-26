package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.fwsource.FwSource;
import org.vmax.amba.fwsource.FwSourceFactory;

import javax.swing.*;


public class AppMain {

    public static void main(String ... args) throws Exception {

        if(args.length<1) {
            System.out.println("config name required");
            return;
        }

        System.out.println("Firmware editor tools ( by v_max )");

        FirmwareConfig basiccfg = FirmwareConfig.readConfig(FirmwareConfig.class, args[0]);

        if(basiccfg.getWarning()!=null) {
            JOptionPane.showMessageDialog(null,
                    basiccfg.getWarning(),"WARNING",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        FirmwareTool tool = (FirmwareTool) Class.forName(basiccfg.getToolClass()).newInstance();

        Class<FirmwareConfig> configClass = tool.getConfigClz();
        FirmwareConfig cfg = FirmwareConfig.readConfig(configClass, args[0]);
        tool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);



        FwSource fwSource = FwSourceFactory.createSource(cfg);


        if(fwSource!=null) {
            try {
                byte[] fwBytes = Utils.loadFirmware(cfg, fwSource);
                startTool(cfg, tool, fwBytes);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "Error loading source file",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        else {
            JOptionPane.showMessageDialog(
                    null,
                    "Error loading firmware source",
                    "",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void startTool(FirmwareConfig cfg, FirmwareTool tool, byte[] fwBytes) throws Exception {
        System.out.println(tool.getStartMessage(cfg));
        tool.init(cfg, fwBytes);
        tool.setJMenuBar(tool.buildMenu());
        tool.pack();
        tool.setVisible(true);
    }

}
