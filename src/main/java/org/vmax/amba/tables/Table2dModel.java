package org.vmax.amba.tables;

import lombok.Getter;
import org.vmax.amba.Utils;
import org.vmax.amba.tables.config.TableConfig;

import javax.swing.table.AbstractTableModel;


public class Table2dModel extends AbstractTableModel {



    public enum ViewMode {
        DEC, HEX
    }


    private ViewMode viewMode = ViewMode.DEC;

    @Getter
    private TableConfig cfg;

    @Getter
    private byte[] bytes;

    public Table2dModel(TableConfig cfg, byte bytes[]) {
        this.cfg = cfg;
        this.bytes = bytes;
    }

    @Override
    public int getRowCount() {
        return cfg.getNrow();
    }

    @Override
    public int getColumnCount() {
        return cfg.getNcol();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(viewMode == ViewMode.HEX) {
            switch (cfg.getType()) {
                case UInt32:
                case Int32:
                case Float32:
                    return Utils.hex(Utils.readUInt(bytes, toAddr(rowIndex, columnIndex)), 8);
                case Int16:
                case UInt16:
                    return Utils.hex(Utils.readUShort(bytes, toAddr(rowIndex, columnIndex)), 4);
                case Byte:
                case UByte:
                    return Utils.hex(Utils.readUByte(bytes, toAddr(rowIndex, columnIndex)), 2);
            }
        }
        else {
            switch (cfg.getType()) {
                case UInt32:
                    return Long.toString(Utils.readUInt(bytes, toAddr(rowIndex, columnIndex)));
                case Int32:
                    return Integer.toString((int)Utils.readUInt(bytes, toAddr(rowIndex, columnIndex)));
                case Float32:
                    float f = Utils.readFloat(bytes, toAddr(rowIndex, columnIndex));
                    return Float.toString(f);

                case Int16:
                    return Short.toString((short) Utils.readUShort(bytes, toAddr(rowIndex, columnIndex)));
                case UInt16:
                    return Long.toString(Utils.readUShort(bytes, toAddr(rowIndex, columnIndex)));
                case Byte:
                    return java.lang.Byte.toString((byte) Utils.readUByte(bytes, toAddr(rowIndex, columnIndex)));
                case UByte:
                    return Long.toString(Utils.readUByte(bytes, toAddr(rowIndex, columnIndex)));
            }


        }

        return "unknown type";
    }


    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        setValueAtImpl(aValue, rowIndex, columnIndex);
        fireTableDataChanged();

    }

    public void setValueAtImpl(Object aValue, int rowIndex, int columnIndex) {
        if(viewMode == ViewMode.HEX) {
            long val = Long.parseLong((String) aValue,16);
            switch (cfg.getType()) {
                case Float32:
                case Int32:
                case UInt32:
                    Utils.writeUInt(bytes,toAddr(rowIndex,columnIndex),val);
                    return;
                case Int16:
                case UInt16:
                    Utils.writeUShort(bytes,toAddr(rowIndex,columnIndex),val);
                    return;
                case Byte:
                case UByte:
                    Utils.writeUByte(bytes,toAddr(rowIndex,columnIndex),val);
                    return;

            }
            return;
        }
        switch (cfg.getType()) {
            case UInt32:
            case Int32:
                long val = Long.valueOf((String)aValue);
                Utils.writeUInt(bytes,toAddr(rowIndex,columnIndex),val);
                break;
            case Float32:
                float f = Float.valueOf((String) aValue);
                Utils.writeFloat(bytes,toAddr(rowIndex,columnIndex),f);
                break;
            case UInt16:
            case Int16:
                val = Long.valueOf((String)aValue);
                Utils.writeUShort(bytes,toAddr(rowIndex,columnIndex),val);
                break;
            default:
                throw new IllegalArgumentException("Invalid datatype");
        }
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    private int toAddr(int rowIndex, int columnIndex) {
        return (columnIndex + rowIndex*cfg.getNcol())*cfg.getType().getByteLen();
    }

    private int toAddr(int inx) {
        return (inx*cfg.getType().getByteLen());
    }





    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        fireTableDataChanged();
    }

    public void setViewDecimal() {
        this.viewMode = ViewMode.DEC;
        fireTableDataChanged();
    }

    public void setViewHex() {
        this.viewMode = ViewMode.HEX;
        fireTableDataChanged();
    }


    public Number getValueAtInx(int inx) {
        switch (cfg.getType()) {
            case UInt32:
                return Utils.readUInt(bytes, toAddr(0, inx));
            case Int32:
                return (int) Utils.readUInt(bytes, toAddr(0, inx));
            case Float32:
                return Utils.readFloat(bytes, toAddr(0, inx));
            case Int16:
                return (short) Utils.readUShort(bytes, toAddr(0, inx));
            case UInt16:
                return Utils.readUShort(bytes, toAddr(0, inx));
            case Byte:
                return (byte) Utils.readUByte(bytes, toAddr(0, inx));
            case UByte:
                return Utils.readUByte(bytes, toAddr(0, inx));
        }
        return 0;
    }

    public void setValueAtInx(int inx, Number v) {
        switch (cfg.getType()) {
            case UInt32:
            case Int32:
                long val = v.longValue();
                Utils.writeUInt(bytes,toAddr(inx),val);
                break;
            case Float32:
                float f = v.floatValue();
                Utils.writeFloat(bytes,toAddr(inx),f);
                break;
            case UInt16:
            case Int16:
                val = v.longValue();
                Utils.writeUShort(bytes,toAddr(inx),val);
                break;
            default:
                throw new IllegalArgumentException("Invalid datatype");
        }

    }

}
