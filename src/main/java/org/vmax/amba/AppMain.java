package org.vmax.amba;

import org.vmax.amba.cfg.FirmwareConfig;

import java.io.InputStreamReader;



public class AppMain {

    public static void main(String args[]) throws Exception {

        if(args.length<1) {
            System.out.println("config name required");
            return;
        }

        FirmwareConfig cfg = FirmwareConfig.readConfig(FirmwareConfig.class, args[0]);
        FirmwareTool tool = (FirmwareTool) Class.forName(cfg.getToolClass()).newInstance();

        Class<FirmwareConfig> configClass = tool.getConfigClz();
        cfg = FirmwareConfig.readConfig(configClass, args[0]);

        byte[] fwBytes = Utils.loadFirmware(cfg);



        System.out.println(tool.getStartMessage(cfg));
        System.out.print("Are you sure you want to continue (Y/N):");
        char[] in = new char[1];
        int n = new InputStreamReader(System.in).read(in);
        if(n<=0 || !(in[0]=='Y' || in[0]=='y')) {
            System.exit(0);
        }
        tool.init(cfg, fwBytes);

    }

}
