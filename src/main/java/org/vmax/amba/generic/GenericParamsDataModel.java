package org.vmax.amba.generic;

import org.vmax.amba.Utils;
import org.vmax.amba.bitrate.RangedFloat;
import org.vmax.amba.bitrate.RangedLong;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;

import javax.swing.table.AbstractTableModel;
import java.awt.*;

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
            case RGB555:
                val=Utils.readUInt(fw,addr);
                val = val >> 1;
                int r = (int) ((val>>10) & 0x1f)<<3;
                int g = (int) ((val>>5) & 0x1f)<<3;
                int b = (int) ((val) & 0x1f)<<3;
                return new Color(r,g,b);
            default:
                val = Utils.readUByte(fw,addr);
                break;
        }
        String ret = vcfg.isHex() ? ("#"+Utils.hex(val)) : Long.toString(val);
        if(!vcfg.getValuesMapping().isEmpty()) {
            return vcfg.getValuesMapping().entrySet().stream()
                    .filter(e->e.getValue().equals(ret))
                    .findFirst()
                    .orElseThrow(()->new IllegalArgumentException("Value "+ret+" for "+vcfg.getLabel()+" can not be mapped, col="+columnIndex+" row="+rowIndex))
                    .getKey();
        }
        return ret;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex==0) {
            return ;
        }
        int addr = cfg.getBaseAddr();
        addr+=cfg.getParams().get(rowIndex).getAddrOffset();
        ValueConfig vcfg = cfg.getParams().get(rowIndex);

        if(!vcfg.getValuesMapping().isEmpty()) {
            aValue = vcfg.getValuesMapping().get((String) aValue);
        }


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
            case RGB555:
                Color c = (Color) aValue;
                int r = c.getRed()>>3;
                int g = c.getGreen()>>3;
                int b = c.getBlue()>>3;
                int v = (r <<11) | (g<<6) | (b<<1) ;
                Utils.writeUInt(fw,addr, v);
                break;
            default:
                Utils.writeUShort(fw,addr, new RangedLong((String) aValue,vcfg.getRange()).getValue());
                break;
        }

    }

}
