package org.vmax.amba.tables.ui;

import org.vmax.amba.tables.Table2dModel;
import org.vmax.amba.tables.config.TableConfig;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TableEditorPanel extends JTable {
    Color c1,c2;


    public TableEditorPanel(TableConfig cfg, Table2dModel model, Color color) {
        super(model);
        for (int i = 0; i < cfg.getNcol(); i++) {
            getColumnModel().getColumn(i).setCellEditor(new RangeCellEditor(cfg.getRange()));
        }
        c1 = sumColor(new Color(0xe0e0e0), color);
        c2 = sumColor(new Color(0xf0f0f0), color);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if (row % 2 == 0)
            comp.setBackground(c1);
        else {
            comp.setBackground(c2);
        }
        return comp;
    }


    private Color sumColor(Color c1, Color c2) {
        return new Color(
            Math.min(255,c1.getRed()+c2.getRed()),
            Math.min(255,c1.getGreen()+c2.getGreen()),
            Math.min(255,c1.getBlue()+c2.getBlue())
        );
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
    }
}
