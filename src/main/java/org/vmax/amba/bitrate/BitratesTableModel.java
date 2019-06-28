package org.vmax.amba.bitrate;

import org.vmax.amba.cfg.bitrate.BitrateEditorConfig;

import javax.swing.table.AbstractTableModel;

public class BitratesTableModel extends AbstractTableModel {
    private final BitrateEditorConfig cfg;
    private final Bitrate[] bitrates;

    public BitratesTableModel(BitrateEditorConfig cfg, Bitrate[] bitrates) {
        this.cfg = cfg;
        this.bitrates = bitrates;
    }

    @Override
    public int getRowCount() {
        return bitrates.length;
    }

    @Override
    public int getColumnCount() {
        return cfg.getQualities().length+7;
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
        else if(columnIndex == cfg.getQualities().length+4) {
            return "GOP M";
        }
        else if(columnIndex == cfg.getQualities().length+5) {
            return "GOP N";
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
                    return Integer.class;
                } else if(columnIndex == cfg.getQualities().length+5) {
                    return Integer.class;
                } else if(columnIndex == cfg.getQualities().length+6) {
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
        return columnIndex>=1 && columnIndex<cfg.getQualities().length+6
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
        else if(columnIndex == cfg.getQualities().length+4) {
            return bitrates[rowIndex].getGop()[0];
        }
        else if(columnIndex == cfg.getQualities().length+5) {
            return bitrates[rowIndex].getGop()[1];
        }
        else {
            return bitrates[rowIndex].isInUse() ;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex-2 >=0 && columnIndex-2<cfg.getQualities().length) {
            Float val = new RangedFloat((String) aValue, cfg.getValidate().getBitrate()).getValue();
            if(cfg.getValidate().getBitrate().getMin().compareTo(val)>0) {
                throw new IllegalArgumentException("Invalid bitrate");
            }
            bitrates[rowIndex].getMbps()[columnIndex-2]=val;
        }
        if(columnIndex == 1) {
            bitrates[rowIndex].setType((Bitrate.Type) aValue);
        }
        if(columnIndex == cfg.getQualities().length+2) {
            Float val = new RangedFloat((String) aValue, cfg.getValidate().getMin()).getValue();
            bitrates[rowIndex].setMin(val);
        }
        if(columnIndex == cfg.getQualities().length+3) {
            Float val = new RangedFloat((String) aValue, cfg.getValidate().getMax()).getValue();
            bitrates[rowIndex].setMax(val);
        }
        if(columnIndex == cfg.getQualities().length+4) {
            int v = (Integer)aValue;
            if(v<0 || v>256) {
                throw new IllegalArgumentException("Invalid GOP N");
            }
            bitrates[rowIndex].getGop()[0]=v;
        }
        if(columnIndex == cfg.getQualities().length+5) {
            int v = (Integer)aValue;
            if(v<0 || v>256) {
                throw new IllegalArgumentException("Invalid GOP M");
            }
            bitrates[rowIndex].getGop()[1]=v;
            bitrates[rowIndex].getGop()[2]=v;
        }

    }


}
