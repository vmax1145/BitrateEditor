package org.vmax.amba.generic;

import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.RangedFloat;
import org.vmax.amba.bitrate.RangedLong;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;

import javax.swing.table.AbstractTableModel;

public class GenericParamsDataModel extends AbstractTableModel {

    private ParamsConfig cfg;
    private byte[] fw;

    public GenericParamsDataModel(ParamsConfig cfg, byte[] fw) {
        this.cfg = cfg;
        this.fw = fw;
    }

    @Override
    public int getRowCount() {
        return cfg.getParams().size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if(columnIndex==0) {
            return "Parameter";
        }
        else {
            return "Value";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex>=1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex==0) {
            return cfg.getParams().get(rowIndex).getLabel();
        }
        int addr = cfg.getBaseAddr();
        addr+=cfg.getParams().get(rowIndex).getAddrOffset();
        ValueConfig vcfg = cfg.getParams().get(rowIndex);
        Long val;
        switch (vcfg.getType()) {
            case Float32:
                return Float.toString(Utils.readFloat(fw,addr));
            case UInt32:
            case Int32:
                val=Utils.readUInt(fw,addr);
                break;
            case Int16:
            case UInt16:
                val = Utils.readUShort(fw,addr);
                break;
            default:
                val = Utils.readUByte(fw,addr);
                break;
        }
        return vcfg.isHex() ? ("#"+Utils.hex(val)) : Long.toString(val);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex==0) {
            return ;
        }
        columnIndex--;
        int addr = cfg.getBaseAddr();
        addr+=cfg.getParams().get(rowIndex).getAddrOffset();
        ValueConfig vcfg = cfg.getParams().get(rowIndex);
        switch (vcfg.getType()) {
            case Float32:
                Utils.writeFloat(fw,addr, new RangedFloat((String) aValue,vcfg.getRange()).getValue());
                break;
            case UInt32:
            case Int32:
                Utils.writeUInt(fw,addr, new RangedLong((String) aValue,vcfg.getRange()).getValue());
                break;
            case Int16:
            case UInt16:
                Utils.writeUShort(fw,addr, new RangedLong((String) aValue,vcfg.getRange()).getValue());
                break;
            default:
                Utils.writeUShort(fw,addr, new RangedLong((String) aValue,vcfg.getRange()).getValue());
                break;
        }

    }

}
