package org.vmax.bitrate.bitrateui;

import org.vmax.bitrate.Bitrate;
import org.vmax.bitrate.cfg.Config;

import javax.swing.table.AbstractTableModel;

public class BitratesTableModel extends AbstractTableModel {
    private final Config cfg;
    private final Bitrate[] bitrates;

    public BitratesTableModel(Config cfg, Bitrate[] bitrates) {
        this.cfg = cfg;
        this.bitrates = bitrates;
    }

    @Override
    public int getRowCount() {
        return bitrates.length;
    }

    @Override
    public int getColumnCount() {
        return cfg.getQualities().length+5;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if(columnIndex == 0) {
            return "mode";
        }
        else if(columnIndex == 1) {
            return "type";
        }
        else if(columnIndex-2 < cfg.getQualities().length) {
            return cfg.getQualities()[columnIndex-2];
        }
        else if(columnIndex == cfg.getQualities().length+2) {
            return "min";
        }
        else if(columnIndex == cfg.getQualities().length+3) {
            return "max";
        }
        else  {
            return "active";
        }

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0 : return String.class;
            case 1 : return Bitrate.Type.class;
            default: {
                if(columnIndex == cfg.getQualities().length+4) {
                    return Boolean.class;
                }
                else {
                    return RangedFloat.class;
                }
            }
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex>=1 && columnIndex<cfg.getQualities().length+4
               && bitrates[rowIndex].isInUse();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if(columnIndex == 0) {
            return bitrates[rowIndex].getName() ;
        }
        else if(columnIndex == 1) {
            return bitrates[rowIndex].getType();
        }
        else if(columnIndex-2 < cfg.getQualities().length) {
            return bitrates[rowIndex].getMbps()[columnIndex-2];
        }
        else if(columnIndex == cfg.getQualities().length+2) {
            return bitrates[rowIndex].getMin();
        }
        else if(columnIndex == cfg.getQualities().length+3) {
            return bitrates[rowIndex].getMax();
        }
        else {
            return bitrates[rowIndex].isInUse() ;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex-2 >=0 && columnIndex-2<cfg.getQualities().length) {
            Float val = new RangedFloat((String) aValue).getValue();
            if(cfg.getValidate().getBitrate().getMin().compareTo(val)>0) {
                throw new IllegalArgumentException("Invalid bitrate");
            }
            bitrates[rowIndex].getMbps()[columnIndex-2]=val;
        }
        System.out.println(aValue+" "+aValue.getClass());
    }

}
