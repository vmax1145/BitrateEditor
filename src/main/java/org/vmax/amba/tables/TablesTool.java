package org.vmax.amba.tables;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.ui.GraphPanel;
import org.vmax.amba.tables.ui.TableEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TablesTool  extends FirmwareTool<TableConfig> {

    private Table2dModel model;
    private TableConfig cfg;
    private byte[] fwBytes;

    public String getStartMessage(FirmwareConfig cfg) {
        return "Tables editor";
    }

    @Override
    public void init(FirmwareConfig fcfg, byte[] fwBytes)  {
        this.fwBytes = fwBytes;
        cfg = (TableConfig) fcfg;
        if(cfg.getNote()!=null) {
            setTitle("Tables Editor : "+cfg.getNote());
        }
        byte[] bytes = loadTable(cfg, fwBytes);
        this.model = new Table2dModel(cfg, bytes);

        TableEditorPanel tableEditorPanel = new TableEditorPanel(cfg, model);

        JScrollPane jsp = new JScrollPane(tableEditorPanel);
        jsp.setPreferredSize(new Dimension(800,500));
        add(jsp, BorderLayout.CENTER);

        JMenuBar bar = buildMenu(cfg,fwBytes);

        setJMenuBar(bar);
    }

    private JMenuBar buildMenu(TableConfig cfg, byte[] fwBytes) {
        JMenuBar bar = super.buildMenu();

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

    @Override
    public void exportData(File selectedFile) {
        try {
            try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
                fw.write(model.getBytes());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }

    @Override
    public void importData(File selectedFile) {
        try {
            byte[] bytes = FileUtils.readFileToByteArray(selectedFile);
            model.setBytes(bytes);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }

    public void updateFW()  {
        try {
            byte[] modelBytes = model.getBytes();
            System.arraycopy(modelBytes, 0, fwBytes, cfg.getTableAddr(), modelBytes.length);
            Utils.saveFirmware(cfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
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
