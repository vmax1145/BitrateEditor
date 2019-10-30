package org.vmax.amba.generic;

import org.vmax.amba.cfg.Patch;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PatchDataModel extends AbstractTableModel {

    private static final String[] COL_NAMES = {"Patch","Apply"};

    private List<Patch> cfg;
    private byte[] fw;

    public PatchDataModel(List<Patch> cfg, byte[] fw) {
        this.cfg = cfg;
        this.fw = fw;
    }

    @Override
    public int getRowCount() {
        return cfg.size();
    }

    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COL_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex==COL_NAMES.length-1 ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex==COL_NAMES.length-1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0 : return cfg.get(rowIndex).getLabel();
            default: return cfg.get(rowIndex).isApply();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex==COL_NAMES.length-1) {
            cfg.get(rowIndex).setApply((Boolean) aValue);
        }
    }

}
