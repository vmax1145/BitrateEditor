package org.vmax.amba.generic;

import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.RangedFloat;
import org.vmax.amba.bitrate.RangedLong;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.table.AbstractTableModel;

public class GenericTableDataModel extends AbstractTableModel {

    private TableDataConfig cfg;
    private byte[] fw;

    public GenericTableDataModel(TableDataConfig cfg, byte[] fw) {
        this.cfg = cfg;
        this.fw = fw;
    }

    @Override
    public int getRowCount() {
        return cfg.getRowsConfig().getRowNames().size();
    }

    @Override
    public int getColumnCount() {
        return cfg.getColumnsConfig().size()+1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if(columnIndex==0) {
            return "mode";
        }
        columnIndex--;
        return cfg.getColumnsConfig().get(columnIndex).getLabel();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex==0) {
            return String.class;
        }
        columnIndex--;
        switch(cfg.getColumnsConfig().get(columnIndex).getType()) {
            case Float32:
                return Float.class;
            case Int32:
                return Integer.class;
            default:
                return Long.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex>=1 && cfg.getColumnsConfig().get(columnIndex-1).isEditable();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex==0) {
            return cfg.getRowsConfig().getRowNames().get(rowIndex);
        }
        columnIndex--;
        int addr = cfg.getRowsConfig().getFirstRowAddr();
        addr+=cfg.getRowsConfig().getRowLenth()*rowIndex;
        addr+=cfg.getColumnsConfig().get(columnIndex).getAddrOffset();
        switch (cfg.getColumnsConfig().get(columnIndex).getType()) {
            case Float32:
                return Utils.readFloat(fw,addr);
            case Int32:
                return Utils.readInt(fw,addr);
            case UInt32:
                return Utils.readUInt(fw,addr);
            case Int16:
                return Utils.readShort(fw,addr);
            case UInt16:
                return Utils.readUShort(fw,addr);
            default:
                return Utils.readUByte(fw,addr);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex==0) {
            return ;
        }
        columnIndex--;
        int addr = cfg.getRowsConfig().getFirstRowAddr();
        addr+=cfg.getRowsConfig().getRowLenth()*rowIndex;
        addr+=cfg.getColumnsConfig().get(columnIndex).getAddrOffset();
        switch (cfg.getColumnsConfig().get(columnIndex).getType()) {
            case Float32:
                Utils.writeFloat(fw,addr, new RangedFloat((String) aValue,cfg.getColumnsConfig().get(columnIndex).getRange()).getValue());
                break;
            case UInt32:
            case Int32:
                Utils.writeUInt(fw,addr, new RangedLong((String) aValue,cfg.getColumnsConfig().get(columnIndex).getRange()).getValue());
                break;
            case Int16:
            case UInt16:
                Utils.writeUShort(fw,addr, new RangedLong((String) aValue,cfg.getColumnsConfig().get(columnIndex).getRange()).getValue());
                break;
            default:
                Utils.writeUShort(fw,addr, new RangedLong((String) aValue,cfg.getColumnsConfig().get(columnIndex).getRange()).getValue());
                break;
        }

    }
}
