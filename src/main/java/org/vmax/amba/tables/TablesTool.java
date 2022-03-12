package org.vmax.amba.tables;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.GenericParamsDataModel;
import org.vmax.amba.generic.GenericParamsTable;
import org.vmax.amba.generic.ImportAction;
import org.vmax.amba.tables.config.SingleTableConf;
import org.vmax.amba.tables.config.TableConfig;
import org.vmax.amba.tables.config.TableSetConfig;
import org.vmax.amba.tables.ui.GraphPanel;
import org.vmax.amba.tables.ui.TableEditorPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TablesTool  extends FirmwareTool<TableConfig> {

    private TableConfig cfg;
    private java.util.List<TableSet> tableSets = new ArrayList<>();


    private byte[] fwBytes;
    @Getter
    private Table2dModel selectedModel=null;
    @Getter
    private TableSet selectedTableSet=null;

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
            p.setPreferredSize(new Dimension(900,900));

            TableSet tableSet = new TableSet();
            tableSet.setTableSetConfig(tableSetConfig);
            tableSets.add(tableSet);
            for (SingleTableConf stcfg : tableSetConfig.getTables()) {
                byte[] bytes = loadTable(cfg, stcfg, fwBytes);
                Table2dModel model = new Table2dModel(cfg, stcfg.getAddr(), bytes);
                tableSet.getModels().add(model);
                TableEditorPanel tableEditorPanel = new TableEditorPanel(this, cfg, tableSet, model, stcfg.getColor().getColor());
                tableEditorPanel.setPreferredSize(new Dimension(900, 300));
                p.add(tableEditorPanel);

            }
            JScrollPane jsp = new JScrollPane(p);
            tabbedPane.add(tableSetConfig.getLabel() ,jsp);
        }

        for(ParamsConfig pcfg : this.cfg.getParamsTabs()) {
            GenericParamsDataModel model = new GenericParamsDataModel(pcfg, fwBytes);
            GenericParamsTable editorPanel = new GenericParamsTable(pcfg, model);
            tabbedPane.add(pcfg.getLabel(), new JScrollPane(editorPanel));
            //allTabs.add(editorPanel);
        }

    }

    @Override
    public JMenuBar buildMenu() {
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


        if(cfg.isCurves()) {
            JMenu graphs = new JMenu("Graphs");
            for (TableSet ts : tableSets) {
                JFrame curveFrame = new JFrame(ts.getTableSetConfig().getLabel());
                curveFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
                GraphPanel graphPanel = GraphPanel.create(cfg, ts.getTableSetConfig(), ts.getModels());
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
                graphs.add(new AbstractAction(ts.getTableSetConfig().getLabel()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        curveFrame.setVisible(true);
                    }
                });
            }
            bar.add(graphs);
        }


        return bar;
    }

    @Override
    public List<ExportAction> getExportActions() {
        return Collections.singletonList(
                new ExportAction("Export settings data", this, new FileNameExtensionFilter("JSON files", "json")){
                    @Override
                    public void exportData(File selectedFile) throws IOException {
                        try(FileOutputStream fw = new FileOutputStream(selectedFile,false)) {
                            for(TableSet ts : tableSets) {
                                for (Table2dModel model : ts.getModels()) {
                                    fw.write(model.getBytes());
                                }
                            }
                        }
                    }
                }
        );
    }

    @Override
    public List<ImportAction> getImportActions() {
        return Collections.singletonList(
                new ImportAction("Import settings data", this,new FileNameExtensionFilter("JSON files", "json")) {

                    @Override
                    protected void importData(File selectedFile) throws IOException {
                        byte[] bytes = FileUtils.readFileToByteArray(selectedFile);
                        int from = 0;
                        for(TableSet ts : tableSets) {
                            for (Table2dModel model : ts.getModels()) {
                                int len = model.getBytes().length;
                                byte[] mbytes = new byte[len];
                                System.arraycopy(bytes,from,mbytes,0,len);
                                from+=len;
                                model.setBytes(mbytes);
                            }
                        }
                    }
                }
        );
    }

    public void updateFW()  {
        try {
            for(TableSet ts : tableSets) {
                for (Table2dModel model : ts.getModels()) {
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


    public void setSelectedModel(Table2dModel model) {
        this.selectedModel = model;
        this.selectedTableSet = null;
    }
    public void setSelectedTableSet(TableSet tableSet) {
        this.selectedTableSet = tableSet;
        this.selectedModel = null;
    }

    public void pasteToModel(Table2dModel toModel) {
        if(selectedModel!=null && selectedModel!=toModel) {
            System.arraycopy(selectedModel.getBytes(),0,toModel.getBytes(),0,selectedModel.getBytes().length);
            toModel.fireTableDataChanged();
        }
    }

    public void pasteToTableSet(TableSet toSet) {
        if(selectedTableSet!=null && selectedTableSet!=toSet) {
            List<Table2dModel> models = selectedTableSet.getModels();
            for (int i = 0; i < models.size(); i++) {
                Table2dModel src = models.get(i);
                Table2dModel dst = toSet.getModels().get(i);
                System.arraycopy(src.getBytes(),0,dst.getBytes(),0,src.getBytes().length);
                dst.fireTableDataChanged();
            }
        }
    }

}
