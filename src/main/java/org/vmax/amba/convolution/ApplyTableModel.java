package org.vmax.amba.convolution;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;

public class ApplyTableModel implements TableModel {

    private final List<String> fileNames;
    private final int tablesPerFile;
    private final int rowsPerTable;
    private final int[] rowMasks;

    public ApplyTableModel(List<String> filenames, int tablesPerFile, int rowsPerTable) {
        this.fileNames = filenames;
        this.tablesPerFile = tablesPerFile;
        this.rowsPerTable = rowsPerTable;
        rowMasks = new int[fileNames.size()*tablesPerFile];
    }

    @Override
    public int getRowCount() {
        return fileNames.size();
    }

    @Override
    public int getColumnCount() {
        return tablesPerFile;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "Table "+columnIndex;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        int inx = toIndex(rowIndex,columnIndex);
        int mask = rowMasks[inx];
        StringBuilder sb = new StringBuilder();
        for(int i=0 ; i<rowsPerTable; i++) {
            sb.append( (mask&1)==1 ? "+":"-");
            mask = mask>>1;
        }
        return sb.toString();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int inx = toIndex(rowIndex,columnIndex);
        String val = ((String) aValue).replaceAll("\\s","");
        if(val.length()!=rowsPerTable) throw new NumberFormatException("String length must be equal to number of rows in table");
        int mask = 0;
        for(int i=0;i<val.length();i++) {
            char c = val.charAt(i);
            int v;
            if (c=='-') v=0;
            else if (c=='+') v=1;
            else if(Character.isDigit(c)) {
                if((c-'0')!=i) {
                     throw new NumberFormatException("invalid row number");
                }
                v=1;
            }
            else throw new NumberFormatException("Rows update mask must contain only + or - chars");
            mask = mask | (v<<i);
        }

    }

    private int toIndex(int rowIndex, int columnIndex) {
        return rowIndex*fileNames.size() + columnIndex;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
