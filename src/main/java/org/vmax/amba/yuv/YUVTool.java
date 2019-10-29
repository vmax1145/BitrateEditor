package org.vmax.amba.yuv;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.ShortValueCfg;
import org.vmax.amba.data.SingleShortData;
import org.vmax.amba.yuv.config.YUVConfig;
import org.vmax.amba.yuv.config.YUVTabCfg;
import org.vmax.amba.yuv.ui.ImageView;
import org.vmax.amba.yuv.ui.SlidersPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

public class YUVTool extends FirmwareTool<YUVConfig> {

    private YUVData data = new YUVData();
    private YUVConfig yuvCfg;
    private byte[] fwBytes;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Saturation editor";
    }

    @Override
    public void init(FirmwareConfig cfg, byte[] fwBytes) {
        this.fwBytes = fwBytes;
        this.yuvCfg = (YUVConfig) cfg;


        JMenuBar bar = buildMenu();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(1400,800));
        add(tabs, BorderLayout.CENTER);
        for(YUVTabCfg tabCfg : yuvCfg.getTabs()) {

            System.out.println(tabCfg.getName());
            YUVTabData tabData = new YUVTabData();
            for(ShortValueCfg slider :  tabCfg.getEditables()) {
                SingleShortData svdata = new SingleShortData();
                svdata.setName(slider.getName());
                svdata.setRange(slider.getRange());
                svdata.setAddr(slider.getAddr());
                svdata.setValue((short) Utils.readUShort(fwBytes,slider.getAddr()));
                svdata.setType(org.vmax.amba.cfg.Type.Int16);
                tabData.add(svdata);
                System.out.println(slider.getName()+" "+svdata.getAddr()+":"+svdata.getValue());
            }
            data.add(tabData);
            tabs.add(tabCfg.getName(),createTab(tabData, tabCfg.getImageSample()));
        }
        setJMenuBar(bar);
    }

    private Component createTab(YUVTabData tabData, String imagePath) {
        JPanel borderP = new JPanel(new BorderLayout());
        JPanel wrap = new JPanel();

        java.util.List<Integer> vals = tabData.stream().map(d->(int)(d.getValue())).collect(Collectors.toList());
        ImageView imageView = new ImageView(imagePath,vals);
        borderP.add(imageView, BorderLayout.CENTER);
        SlidersPanel p = new SlidersPanel(tabData);
        p.addListener(imageView);

        wrap.add(p);
        borderP.add(wrap, BorderLayout.WEST);
        return borderP;
    }


    @Override
    public void exportData(File selectedFile) {
        try {
            try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
                Utils.getObjectMapper().writeValue(fw,data);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void importData(File selectedFile) {
        try (FileInputStream fis = new FileInputStream(selectedFile)){
            YUVData in = Utils.getObjectMapper().readerFor(YUVData.class).readValue(fis);
            if(in.size()!=data.size()) {
                throw new Exception("Invalid data in import");
            }
            for(int i=0;i<in.size();i++) {
                YUVTabData yuvTabData = data.get(i);
                YUVTabData inTabData = in.get(i);
                if(yuvTabData.size()!=inTabData.size()) {
                    throw new Exception("Invalid data in import");
                }
                for (int j = 0; j < yuvTabData.size(); j++) {
                    short val = inTabData.get(j).getValue();
                    yuvTabData.get(j).setValue(val);
                    yuvTabData.get(j).getSlider().setValue(val);
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(YUVTool.this, "Error reading settings","Warning",JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void updateFW() {

        for(YUVTabData tData : data) {
            for(SingleShortData sv : tData) {
                Utils.writeUShort(fwBytes, sv.getAddr(), sv.getValue());
            }
        }
        try {
            Utils.saveFirmware(this, yuvCfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }


    @Override
    public Class<YUVConfig> getConfigClz() {
        return YUVConfig.class;
    }
}
