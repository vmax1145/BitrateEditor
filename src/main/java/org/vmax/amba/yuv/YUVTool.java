package org.vmax.amba.yuv;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.yuv.config.YUVConfig;
import org.vmax.amba.yuv.config.YUVTabCfg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class YUVTool extends FirmwareTool<YUVConfig> {
    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Saturation editor";
    }

    @Override
    public void init(FirmwareConfig cfg, byte[] fwBytes) {
        YUVConfig yuvCfg = (YUVConfig) cfg;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar bar = buildMenu(yuvCfg);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(800,500));
        add(tabs, BorderLayout.CENTER);
        for(YUVTabCfg tabCfg : yuvCfg.getTabs()) {
            tabs.add(tabCfg.getName(),createTab(tabCfg,fwBytes));

        }
        setJMenuBar(bar);
        pack();
        setVisible(true);
    }

    private Component createTab(YUVTabCfg tabCfg, byte[] fwBytes) {
        return new JPanel();
    }


    private JMenuBar buildMenu(YUVConfig cfg) {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

//        fileMenu.add(new AbstractAction("Export settings data") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser jfc = new JFileChooser(new File(".\\"));
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
//                jfc.addChoosableFileFilter(filter);
//                int returnValue = jfc.showSaveDialog(YUVTool.this);
//                if (returnValue == JFileChooser.APPROVE_OPTION) {
//                    File selectedFile = jfc.getSelectedFile();
//                    if(selectedFile.exists()) {
//                        int dialogResult = JOptionPane.showConfirmDialog (jfc, "Owerwrite existing file?","Warning",JOptionPane.YES_NO_OPTION);
//                        if(dialogResult != JOptionPane.YES_OPTION){
//                            return;
//                        }
//                    }
//                    try {
//                        try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
//                            //todo write data
//                        }
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        fileMenu.add(new AbstractAction("Import settings data") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser jfc = new JFileChooser(new File(".\\"));
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
//                jfc.addChoosableFileFilter(filter);
//                int returnValue = jfc.showOpenDialog(TablesTool.this);
//                if (returnValue == JFileChooser.APPROVE_OPTION) {
//                    File selectedFile = jfc.getSelectedFile();
//                    if(!selectedFile.exists()) {
//                        JOptionPane.showConfirmDialog (jfc, "File not exists","Warning",JOptionPane.OK_OPTION);
//                        return;
//                    }
//                    try {
//                        //todo
//                        //byte[] bytes = FileUtils.readFileToByteArray(selectedFile);
//                        //model.setBytes(bytes);
//                    }
//                    catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }
//        });
//
//
//
//        fileMenu.add(new AbstractAction("Save") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try(RandomAccessFile raf = new RandomAccessFile(new File(cfg.getFwFileName()),"rw")) {
//                    raf.seek(cfg.getTableAddr());
//                    raf.write(model.getBytes());
//                    JOptionPane.showMessageDialog(TablesTool.this,"File updated" );
//                } catch (IOException e1) {
//                    JOptionPane.showMessageDialog(TablesTool.this,"Oooops! error saving data" );
//                }
//            }
//        });

        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        return bar;
    }



    @Override
    public Class<YUVConfig> getConfigClz() {
        return YUVConfig.class;
    }
}
