package org.vmax.amba.generic;

import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.RangeCellEditor;
import org.vmax.amba.cfg.Range;
import org.vmax.amba.cfg.Type;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GenericJTable extends JTable implements GenericTab, PopupMenuListener {
    private final Frame frame;
    private final JPopupMenu popupMenu;
    private TableDataConfig cfg;
    int colAtPoint=-1;
    int rowAtPoint=-1;

    public GenericJTable(Frame frame, TableDataConfig cfg, GenericTableDataModel model) {
        super(model);
        this.cfg=cfg;
        this.frame = frame;
        this.popupMenu = new JPopupMenu();
        adjustColumns();

        TableColumnModel columnModel = getColumnModel();
        columnModel.setColumnSelectionAllowed(true);
        ListSelectionModel columnSelectionModel = columnModel.getSelectionModel();
        columnSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        ListSelectionModel rowSelectionModel = getSelectionModel();
        rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);


        JMenuItem copyItem = popupMenu.add(new JMenuItem(new AbstractAction("Copy selection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copySelection();
            }
        }));
        JMenuItem pasteItem = popupMenu.add(new JMenuItem(new AbstractAction("Paste selection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteSelection();
            }
        }));
        setComponentPopupMenu(popupMenu);
        popupMenu.addPopupMenuListener(this);
    }



    @Override
    public void setModel(TableModel m) {
        super.setModel(m);
        if(cfg!=null) {
            adjustColumns();
        }

    }

    private void adjustColumns() {
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        for(int i=0;i<cfg.getColumnsConfig().size();i++) {
            this.getColumnModel().getColumn(i+1).setCellEditor(new RangeCellEditor(cfg.getColumnsConfig().get(i).getRange()));
        }

        TableModel tableModel = getModel();
        TableColumnModel columnModel = getColumnModel();
        for (int column = 0; column < getColumnCount(); column++) {

            TableCellRenderer renderer = getCellRenderer(0, 0);
            int width = renderer.getTableCellRendererComponent(this ,tableModel.getColumnName(column),false, false,0,column).getPreferredSize().width;

            for (int row = 0; row < getRowCount(); row++) {
                renderer = getCellRenderer(row, column);
                Component comp = prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }
            if(width > 300)
                width=300;
            columnModel.getColumn(column).setPreferredWidth(width);
        }

    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        boolean isSelected = isCellSelected(row, col);
        Component comp = super.prepareRenderer(renderer, row, col);
        if(row%2 ==0)
            comp.setBackground(isSelected ? new Color(0xe0e0ff) : new Color(0xe0e0e0));
        else {
            comp.setBackground(isSelected ? new Color(0xe0e0ff) : new Color(0xf0f0f0));
        }
        if(col>0) {
            Range r = cfg.getColumnsConfig().get(col-1).getRange();
            if(r!=null && comp instanceof JComponent) {
                if(!Type.Float32.equals(cfg.getColumnsConfig().get(col-1).getType())) {
                    ((JComponent) comp).setToolTipText("min:" + r.getMin().intValue() + " max:" + r.getMax().intValue());
                }
                else {
                    ((JComponent) comp).setToolTipText("min:" + r.getMin() + " max:" + r.getMax());
                }
            }
        }

        return comp;
    }


    @Override
    public String getTabLabel() {
        return cfg.getLabel();
    }

    @Override
    public ImportAction getImportAction() {
        return new ImportAction("Import "+cfg.getLabel()+" data", frame, new FileNameExtensionFilter("JSON files", "json")) {
            @Override
            protected void importData(File selectedFile) throws IOException {
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    GenericTableDataDto dto = Utils.getObjectMapper().readerFor(GenericTableDataDto.class).readValue(fis);
                    int nrows = dataModel.getRowCount();
                    int ncols = dataModel.getColumnCount();
                    if(dto.getNrow()!=nrows || dto.getNcol()!=ncols) {
                        throw new IOException("Data structure mismatch required:"+nrows+"x"+ncols+" actual:"+dto.getNrow()+"х"+dto.getNcol());
                    }
                    for(int row = 0; row < nrows; row++) {
                        java.util.List<String> rowdata = dto.getValues().get(row);
                        for(int col = 0; col < ncols; col++) {
                            if(dataModel.isCellEditable(row,col)) {
                                dataModel.setValueAt(rowdata.get(col),row,col);
                            }
                        }
                        dto.getValues().add(rowdata);
                    }
                    ((GenericTableDataModel)dataModel).fireTableDataChanged();
                }
            }
        };
    }

    @Override
    public ExportAction getExportAction() {
        return new ExportAction("Export "+cfg.getLabel()+" data", frame, new FileNameExtensionFilter("JSON files", "json")){
            public void exportData(File selectedFile) throws IOException {
                try(FileWriter fw = new FileWriter(selectedFile)) {
                    GenericTableDataDto dto = new GenericTableDataDto();
                    dto.setNcol(dataModel.getColumnCount());
                    dto.setNrow(dataModel.getRowCount());
                    for(int row = 0; row < dataModel.getRowCount(); row++) {
                        ArrayList<String> rowdata = new ArrayList<>();
                        for(int col = 0; col < dataModel.getColumnCount(); col++) {
                            rowdata.add(dataModel.getValueAt(row,col).toString());
                        }
                        dto.getValues().add(rowdata);
                    }
                    Utils.getObjectMapper().writeValue(fw,dto);
                }
            }
        };
    }

    private void copySelection() {
        System.out.println("copy");
    }
    private void pasteSelection() {
        System.out.println("Paste");
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point where = SwingUtilities.convertPoint(popupMenu, new Point(0, 0), GenericJTable.this);
                rowAtPoint = GenericJTable.this.rowAtPoint(where);
                colAtPoint = GenericJTable.this.columnAtPoint(where);
                if(rowAtPoint>-1 && colAtPoint>-1 && getSelectionModel().isSelectionEmpty()){
                    if(!isCellSelected(rowAtPoint,colAtPoint)) {
                        changeSelection(rowAtPoint, colAtPoint, true, false);
                    }
                }
            }
        });
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
    }

}
