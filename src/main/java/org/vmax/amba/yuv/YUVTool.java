package org.vmax.amba.yuv;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.ShortValueCfg;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.data.SingleShortData;
import org.vmax.amba.yuv.config.YUVConfig;
import org.vmax.amba.yuv.config.YUVTabCfg;
import org.vmax.amba.yuv.ui.SpringUtilities;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class YUVTool extends FirmwareTool<YUVConfig> {

    YUVData data = new YUVData();

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Saturation editor";
    }

    @Override
    public void init(FirmwareConfig cfg, byte[] fwBytes) {

        YUVConfig yuvCfg = (YUVConfig) cfg;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar bar = buildMenu(yuvCfg, fwBytes);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(800,500));
        add(tabs, BorderLayout.CENTER);
        for(YUVTabCfg tabCfg : yuvCfg.getTabs()) {

            System.out.println(tabCfg.getName());
            YUVTabData tabData = new YUVTabData();
            for(ShortValueCfg slider :  tabCfg.getEditables()) {
                SingleShortData svdata = new SingleShortData();
                svdata.setName(slider.getName());
                svdata.setRange(slider.getRange());
                svdata.setAddr(slider.getAddr());
                svdata.setOriginalValue((short) Utils.readUShort(fwBytes,slider.getAddr()));
                svdata.setValue(svdata.getOriginalValue());
                tabData.add(svdata);
                System.out.println(slider.getName()+" "+svdata.getAddr()+":"+svdata.getOriginalValue()+"->"+svdata.getValue());
            }
            data.add(tabData);
            tabs.add(tabCfg.getName(),createTab(tabData));
        }
        setJMenuBar(bar);
        pack();
        setVisible(true);
    }

    private Component createTab(YUVTabData tabData) {
        JPanel borderP = new JPanel(new BorderLayout());
        JPanel wrap = new JPanel();
        JPanel p = new JPanel(new SpringLayout());
        for(SingleShortData e : tabData) {
            JLabel label = new JLabel(e.getName(), JLabel.TRAILING);
            p.add(label);
            JSlider sliderField = new JSlider(e.getRange().getMin(),e.getRange().getMax(),Math.round(e.getValue()));
            label.setLabelFor(sliderField);
            p.add(sliderField);
            JTextField val=new JTextField(4);
            val.setEditable(false);
            p.add(val);
            val.setText(Short.toString(e.getValue()));
            sliderField.addChangeListener(e1 -> {
                int v = sliderField.getValue();
                val.setText(Short.toString((short) v));
            });

        }

        SpringUtilities.makeCompactGrid(p,
                tabData.size(), 3, //rows, cols
                6, 6,        //initX, initY
                6, 6);

        wrap.add(p);
        borderP.add(wrap, BorderLayout.WEST);
        return borderP;
    }


    private JMenuBar buildMenu(YUVConfig cfg, byte[] fwBytes) {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        fileMenu.add(new AbstractAction("Export settings data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showSaveDialog(YUVTool.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(selectedFile.exists()) {
                        int dialogResult = JOptionPane.showConfirmDialog (jfc, "Owerwrite existing file?","Warning",JOptionPane.YES_NO_OPTION);
                        if(dialogResult != JOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    try {
                        try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
                            new ObjectMapper().writeValue(fw,data);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        fileMenu.add(new AbstractAction("Import settings data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showOpenDialog(YUVTool.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(!selectedFile.exists()) {
                        JOptionPane.showMessageDialog(jfc, "File not exists","Warning",JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try (FileInputStream fis = new FileInputStream(selectedFile)){
                        YUVData in = new ObjectMapper().readerFor(YUVData.class).readValue(fis);
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
                                yuvTabData.get(j).setValue(inTabData.get(j).getValue());
                            }
                        }
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(jfc, "Error reading settings","Warning",JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });



        fileMenu.add(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateFirmware(cfg, fwBytes);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(YUVTool.this, "Error svaing firmware","Error",JOptionPane.ERROR_MESSAGE);
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

    private void updateFirmware(YUVConfig cfg, byte[] fwBytes) throws Exception {
        for(YUVTabData tData : data) {
            for(SingleShortData sv : tData) {
                Utils.writeUShort(fwBytes, sv.getAddr(), sv.getValue());
            }
        }
        Utils.saveFirmware(cfg,fwBytes);
    }


    @Override
    public Class<YUVConfig> getConfigClz() {
        return YUVConfig.class;
    }
}
