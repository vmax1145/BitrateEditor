package org.vmax.amba.generic;

import org.vmax.amba.bitrate.RangeCellEditor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class GenericParamsTable extends JTable {


    public GenericParamsTable(GenericParamsDataModel model) {
        super(model);
        this.getColumnModel().getColumn(1).setCellEditor(new RangeCellEditor(null));
    }

    @Override
    public void setModel(TableModel m) {
        super.setModel(m);
    }

    @Override
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
