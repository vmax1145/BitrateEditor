package org.vmax.midrive;

import org.vmax.amba.cfg.ImageConfig;
import org.vmax.amba.generic.GenericImageTab;

public class MiPatchToolD05 extends MiPatchTool {


    @Override
    protected GenericImageTab createImageTab(ImageConfig icfg, byte[] fwBytes) throws Exception {
        return new MiLogoTab((MiLogoImageConfig) icfg, fwBytes, (byte)8);
    }

}
