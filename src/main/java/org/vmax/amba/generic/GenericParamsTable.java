package org.vmax.amba.generic;

import org.vmax.amba.bitrate.RangeCellEditor;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class GenericParamsTable extends JTable {
    private ParamsConfig cfg;

    public GenericParamsTable(ParamsConfig cfg, GenericParamsDataModel model) {
        super(model);
        this.cfg = cfg;
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


    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if(column==1) {
            ValueConfig vcfg = cfg.getParams().get(row);
            if(!vcfg.getValuesMapping().isEmpty()) {
                String[] vals = vcfg.getValuesMapping().keySet().toArray(new String[]{});
                JComboBox<String> select = new JComboBox<>(vals);
                return new DefaultCellEditor(select);
            }
        }
        return super.getCellEditor(row,column);
    }
}
