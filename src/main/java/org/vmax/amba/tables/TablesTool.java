package org.vmax.amba.tables;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.ui.GraphPanel;
import org.vmax.amba.tables.ui.TableEditorPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class TablesTool  extends FirmwareTool<TableConfig> {

    private Table2dModel model;

    public String getStartMessage(FirmwareConfig cfg) {
        return "Tables editor";
    }

    @Override
    public void init(FirmwareConfig fcfg, byte[] fwBytes)  {
        TableConfig cfg = (TableConfig) fcfg;
        if(cfg.getNote()!=null) {
            setTitle("Tables Editor : "+cfg.getNote());
        }
        byte[] bytes = loadTable(cfg, fwBytes);
        this.model = new Table2dModel(cfg, bytes);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TableEditorPanel tableEditorPanel = new TableEditorPanel(cfg, model);

        JScrollPane jsp = new JScrollPane(tableEditorPanel);
        jsp.setPreferredSize(new Dimension(800,500));
        add(jsp, BorderLayout.CENTER);

        JMenuBar bar = buildMenu(cfg);

        setJMenuBar(bar);
        pack();
        setVisible(true);
    }

    private JMenuBar buildMenu(TableConfig cfg) {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        fileMenu.add(new AbstractAction("Export table data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showSaveDialog(TablesTool.this);
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
                            fw.write(model.getBytes());
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        fileMenu.add(new AbstractAction("Import table data") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showOpenDialog(TablesTool.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(!selectedFile.exists()) {
                        JOptionPane.showConfirmDialog (jfc, "File not exists","Warning",JOptionPane.OK_OPTION);
                        return;
                    }
                    try {
                        byte[] bytes = FileUtils.readFileToByteArray(selectedFile);
                        model.setBytes(bytes);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });



        fileMenu.add(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try(RandomAccessFile raf = new RandomAccessFile(new File(cfg.getFwFileName()),"rw")) {
                    raf.seek(cfg.getTableAddr());
                    raf.write(model.getBytes());
                    JOptionPane.showMessageDialog(TablesTool.this,"File updated" );
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(TablesTool.this,"Oooops! error saving data" );
                }
            }
        });

        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenu view = new JMenu("View");
        view.add(new AbstractAction("Decimal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setViewDecimal();
            }
        });
        view.add(new AbstractAction("Hex") {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setViewHex();
            }
        });
        bar.add(view);

        JFrame curveFrame = new JFrame();
        curveFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
        GraphPanel graphPanel = GraphPanel.create(cfg,model);
        JScrollPane sp = new JScrollPane(graphPanel);
        curveFrame.getContentPane().add(sp);
        JMenuBar curveBar = new JMenuBar();
        JMenu curveMenu = new JMenu("Update");
        curveBar.add(curveMenu);
        curveFrame.setJMenuBar(curveBar);
        curveMenu.add(new AbstractAction("Update table from spline") {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphPanel.updateTable();
            }
        });
        curveFrame.pack();


        JMenu graphs = new JMenu("Graphs");
        graphs.add(new AbstractAction("Curve") {
            @Override
            public void actionPerformed(ActionEvent e) {
                curveFrame.setVisible(true);
            }
        });
        bar.add(graphs);
        return bar;
    }


    public static byte[] loadTable(TableConfig cfg, byte[] fwBytes)  {
        int len = cfg.getNcol() * cfg.getNrow() * cfg.getType().getByteLen();
        return Arrays.copyOfRange(fwBytes,cfg.getTableAddr(),cfg.getTableAddr()+len);
    }
    @Override
    public Class<TableConfig> getConfigClz() {
        return TableConfig.class;
    }

}
