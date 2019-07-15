package org.vmax.amba.bitrate;

import org.vmax.amba.bitrate.config.BitrateEditorConfig;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class EditorPanel extends JTable {
    private BitrateEditorConfig cfg;

    public EditorPanel(BitrateEditorConfig cfg, Bitrate[] bitrates) {
        super(new BitratesTableModel(cfg, bitrates));
        this.cfg=cfg;
        adjustColumns();
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
        for(int i=0;i<cfg.getQualities().length;i++) {
            this.getColumnModel().getColumn(i+2).setCellEditor(new RangeCellEditor(cfg.getValidate().getBitrate()));
        }
        this.getColumnModel().getColumn(cfg.getQualities().length+2).setCellEditor(new RangeCellEditor(cfg.getValidate().getMin()));
        this.getColumnModel().getColumn(cfg.getQualities().length+3).setCellEditor(new RangeCellEditor(cfg.getValidate().getMax()));

        TableModel tableModel = getModel();
        TableColumnModel columnModel = getColumnModel();
        int w = 100 ;
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
            w+=width;
        }
        JComboBox<Bitrate.Type> select = new JComboBox<>(Bitrate.Type.values());
        TableColumn column = getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(select));


    }


    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if((row/cfg.getQualities().length)%2 ==0)
            comp.setBackground(new Color(0xe0e0e0));
        else {
            comp.setBackground(new Color(0xf0f0f0));
        }
        return comp;
    }

    public void onDataChange() {
        ((BitratesTableModel)(this.getModel())).fireTableDataChanged();
    }
}
