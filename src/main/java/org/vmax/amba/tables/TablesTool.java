package org.vmax.amba.tables;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.config.TableSetConfig;
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
import java.util.stream.Collectors;

public class TablesTool  extends FirmwareTool<TableConfig> {

    private TableConfig cfg;
    private java.util.List<TableSet> tableSets = new ArrayList<>();


    private class TableSet {
        @Getter
        private TableSetConfig tableSetConfig;
        @Getter
        private java.util.List<Table2dModel> models = new ArrayList<>();
    }




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
        getContentPane().add(tabbedPane);

        for(TableSetConfig tableSetConfig : cfg.getTableSets()) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setPreferredSize(new Dimension(800,900));

            TableSet tableSet = new TableSet();
            tableSet.tableSetConfig = tableSetConfig;
            tableSets.add(tableSet);
            for (SingleTableConf stcfg : tableSetConfig.getTables()) {
                byte[] bytes = loadTable(cfg, stcfg, fwBytes);
                Table2dModel model = new Table2dModel(cfg, stcfg.getAddr(), bytes);
                tableSet.models.add(model);
                TableEditorPanel tableEditorPanel = new TableEditorPanel(cfg, model, stcfg.getColor().getColor());
                tableEditorPanel.setPreferredSize(new Dimension(800, 300));
                p.add(tableEditorPanel);

            }
            JScrollPane jsp = new JScrollPane(p);
            tabbedPane.add(tableSetConfig.getLabel() ,jsp);
        }

        JMenuBar bar = buildMenu(cfg,fwBytes);
        setJMenuBar(bar);



        int diff = 274432;
        java.util.List<TableSetConfig> configs =  Arrays.stream(cfg.getTableSets())
                .map( tsc-> {
                    TableSetConfig tsc2 = new TableSetConfig();
                    tsc2.setLabel("4K60");
                    tsc2.setTables(

                    Arrays.stream(tsc.getTables())
                               .map( stc-> {
                                   SingleTableConf stc2 = new SingleTableConf();
                                   stc2.setColor(stc.getColor());
                                   stc2.setLabel(stc.getLabel());
                                   stc2.setAddr(stc.getAddr()+diff);
                                   return stc2;
                                   }
                               ).collect(Collectors.toList()).toArray(new SingleTableConf[0])
                    );
                    return tsc2;
                    }
                ).collect(Collectors.toList());
        try {
            System.out.println(new ObjectMapper().writer().writeValueAsString(configs));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private JMenuBar buildMenu(TableConfig cfg, byte[] fwBytes) {
        JMenuBar bar = super.buildMenu();

        JMenu view = new JMenu("View");
        view.add(new AbstractAction("Decimal") {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableSets.forEach(ts->ts.getModels().forEach(Table2dModel::setViewDecimal));
            }
        });
        view.add(new AbstractAction("Hex") {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableSets.forEach(ts->ts.getModels().forEach(Table2dModel::setViewHex));
            }
        });
        bar.add(view);

        JMenu graphs = new JMenu("Graphs");
        for(TableSet ts : tableSets) {
            JFrame curveFrame = new JFrame(ts.tableSetConfig.getLabel());
            curveFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
            GraphPanel graphPanel = GraphPanel.create(cfg, ts.tableSetConfig, ts.getModels());
            JScrollPane sp = new JScrollPane(graphPanel);
            curveFrame.getContentPane().add(sp);
            JMenuBar curveBar = new JMenuBar();
            JMenu curveMenu = new JMenu("Update");
            curveBar.add(curveMenu);
            curveFrame.setJMenuBar(curveBar);
            curveMenu.add(new AbstractAction("Update table from spline") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    graphPanel.updateTableFromSpline();
                }
            });
            curveMenu.add(new AbstractAction("Recalculate spline") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    graphPanel.updateSplineFromTable();
                }
            });
            curveFrame.pack();
            graphs.add(new AbstractAction(ts.tableSetConfig.getLabel()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    curveFrame.setVisible(true);
                }
            });
        }




        bar.add(graphs);
        return bar;
    }

    @Override
    public void exportData(File selectedFile) {
        try {
            try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
                for(TableSet ts : tableSets) {
                    for (Table2dModel model : ts.models) {
                        fw.write(model.getBytes());
                    }
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
            int from = 0;
            for(TableSet ts : tableSets) {
                for (Table2dModel model : ts.models) {
                    int len = model.getBytes().length;
                    byte[] mbytes = new byte[len];
                    System.arraycopy(bytes,from,mbytes,0,len);
                    from+=len;
                    model.setBytes(mbytes);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details" );
        }
    }

    public void updateFW()  {
        try {
            for(TableSet ts : tableSets) {
                for (Table2dModel model : ts.models) {
                    byte[] modelBytes = model.getBytes();
                    System.arraycopy(modelBytes, 0, fwBytes, model.getAddr(), modelBytes.length);
                }
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
