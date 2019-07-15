package org.vmax.amba.tables.ui;

import org.vmax.amba.tables.Table2dModel;
import org.vmax.amba.tables.config.TableConfig;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TableEditorPanel extends JTable {



    public TableEditorPanel(TableConfig cfg, Table2dModel model) {
        super(model);
        for (int i = 0; i < cfg.getNcol(); i++) {
            getColumnModel().getColumn(i).setCellEditor(new RangeCellEditor(cfg.getRange()));
        }
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if (row % 2 == 0)
            comp.setBackground(new Color(0xe0e0e0));
        else {
            comp.setBackground(new Color(0xf0f0f0));
        }
        return comp;
    }

    public void onDataChange() {
        ((AbstractTableModel) (this.getModel())).fireTableDataChanged();
    }


}
