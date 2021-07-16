package org.vmax.midrive;

import org.vmax.amba.Utils;
import org.vmax.amba.cfg.Dimension;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.GenericTableDataConfig;
import org.vmax.amba.cfg.ImageConfig;
import org.vmax.amba.generic.GenericImageTab;
import org.vmax.amba.generic.GenericTool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class A800ResImageTool extends GenericTool {

    public void init(FirmwareConfig acfg, byte[] fwBytes) throws Exception {
        GenericTableDataConfig cfg = (GenericTableDataConfig) acfg;
        long nres = readHeaderInt(fwBytes,0x4);
        System.out.println("N="+nres);
        int addr = 0x10 + 24*3;
        cfg.getImageTabs().clear();
        for(int i =3 ; i<nres; i++) {
            int id  = readHeaderInt(fwBytes,addr);
            int len = readHeaderInt(fwBytes,addr+4);
            int offset = readHeaderInt(fwBytes,addr+8);
            addr+=24;
            MiLogoImageConfig icfg = new MiLogoImageConfig();
            icfg.setLabel("Res "+id);
            icfg.setAddr(offset);
            int w = (int) Utils.readInt(fwBytes,offset+4);
            int h = (int) Utils.readInt(fwBytes,offset+8);
            System.out.println(id+" "+Integer.toHexString(offset)+" "+len+" "+w+"*"+h);
            MiLogoImageConfig imcfg = new MiLogoImageConfig();
            imcfg.setLabel("img"+id+" ");
            imcfg.setAddr(offset);
            Dimension d = new Dimension();
            d.setWidth(w);
            d.setHeight(h);
            imcfg.setDimension(d);
            cfg.getImageTabs().add(imcfg);
        }



        super.init(acfg,fwBytes);
    }
    @Override
    protected GenericImageTab createImageTab(ImageConfig icfg, byte[] fwBytes) throws Exception {
        return new A800LogoTab((MiLogoImageConfig) icfg, fwBytes, (byte) 2);
    }
    @Override
    public Class<MiPatchToolConfig> getConfigClz() {
        return MiPatchToolConfig.class;
    }

    public static int readHeaderInt(byte[] fw, int addr) {
        ByteBuffer bb = ByteBuffer.wrap(fw, addr, Integer.BYTES);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getInt();
    }
}
