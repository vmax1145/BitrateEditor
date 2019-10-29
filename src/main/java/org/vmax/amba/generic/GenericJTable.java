package org.vmax.amba.generic;

import org.vmax.amba.bitrate.RangeCellEditor;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class GenericJTable extends JTable {
    private TableDataConfig cfg;

    public GenericJTable(TableDataConfig cfg, GenericTableDataModel model) {
        super(model);
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

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if(row%2 ==0)
            comp.setBackground(new Color(0xe0e0e0));
        else {
            comp.setBackground(new Color(0xf0f0f0));
        }
        return comp;
    }


}