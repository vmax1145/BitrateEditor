package org.vmax.amba.tables;

import org.apache.commons.io.FileUtils;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.ui.GraphPanel;
import org.vmax.amba.tables.ui.TableEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TablesTool  extends FirmwareTool<TableConfig> {

    private java.util.List<Table2dModel> models = new ArrayList<>();
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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(800,500));

        for(SingleTableConf stcfg : cfg.getTables()) {
            byte[] bytes = loadTable(cfg, stcfg, fwBytes);
            Table2dModel model = new Table2dModel(cfg, bytes);
            this.models.add(new Table2dModel(cfg, bytes));
            TableEditorPanel tableEditorPanel = new TableEditorPanel(cfg, model);
            JScrollPane jsp = new JScrollPane(tableEditorPanel);
            jsp.setPreferredSize(new Dimension(800,500));
            tabbedPane.add(stcfg.getColor().name(),jsp);
        }
        add(tabbedPane, BorderLayout.CENTER);

        JMenuBar bar = buildMenu(cfg,fwBytes);

        setJMenuBar(bar);
    }

    private JMenuBar buildMenu(TableConfig cfg, byte[] fwBytes) {
        JMenuBar bar = super.buildMenu();

        JMenu view = new JMenu("View");
        view.add(new AbstractAction("Decimal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                models.forEach(Table2dModel::setViewDecimal);
            }
        });
        view.add(new AbstractAction("Hex") {
            @Override
            public void actionPerformed(ActionEvent e) {
                models.forEach(Table2dModel::setViewHex);
            }
        });
        bar.add(view);

        JFrame curveFrame = new JFrame();
        curveFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
        GraphPanel graphPanel = GraphPanel.create(cfg,models);
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
                for (Table2dModel model : models) {
                    fw.write(model.getBytes());
                }
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
            int len = bytes.length/models.size();
            for (int i = 0; i < models.size(); i++) {
                Table2dModel model = models.get(i);
                byte[] mbytes = Arrays.copyOfRange(bytes,i*len,(i+1)*len);
                model.setBytes(mbytes);
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }

    public void updateFW()  {
        try {

            SingleTableConf[] tables = cfg.getTables();
            for (int i = 0; i < tables.length; i++) {
                SingleTableConf stcfg = tables[i];
                byte[] modelBytes = models.get(i).getBytes();
                System.arraycopy(modelBytes, 0, fwBytes, stcfg.getAddr(), modelBytes.length);
            }
            Utils.saveFirmware(cfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }


    public static byte[] loadTable(TableConfig cfg, SingleTableConf stcfg, byte[] fwBytes)  {
        int len = cfg.getNcol() * cfg.getNrow() * cfg.getType().getByteLen();
        return Arrays.copyOfRange(fwBytes, stcfg.getAddr(),stcfg.getAddr()+len);
    }
    @Override
    public Class<TableConfig> getConfigClz() {
        return TableConfig.class;
    }

}
