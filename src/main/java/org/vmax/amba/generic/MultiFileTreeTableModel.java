package org.vmax.amba.generic;

import de.javagl.treetable.AbstractTreeTableModel;
import de.javagl.treetable.TreeTableModel;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.MultiFilesTablesConfig;
import org.vmax.amba.cfg.tabledata.NamedRowsConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;

import java.util.ArrayList;
import java.util.List;

public class MultiFileTreeTableModel extends AbstractTreeTableModel {


    private final byte[] fw;

    public static MultiFileTreeTableModel create(MultiFilesTablesConfig cfg, byte[] fw) {

        RootNode root = new RootNode();
        List<String> filenames = cfg.getFilenames();
        for (int k = 0, filenamesSize = filenames.size(); k < filenamesSize; k++) {
            String f = filenames.get(k);
            FileNode fn = new FileNode();
            root.files.add(fn);
            fn.name = f;
            for (int i = 0; i < cfg.getTablesPerFile(); i++) {
                TableNode tn = new TableNode();
                fn.tables.add(tn);
                tn.name = "Table " + i;
                NamedRowsConfig namedRowsConfig = cfg.getTableDataConfigs().get(k*cfg.getTablesPerFile()+i).getRowsConfig();
                for (int j = 0; j < cfg.getRowNames().size(); j++) {
                    RowNode rn = new RowNode();
                    rn.name = cfg.getRowNames().get(j);
                    rn.addr = namedRowsConfig.getFirstRowAddr()+cfg.getRowLength()*j;
                    rn.len  = cfg.getRowLength();
                    rn.vc = cfg.getColumnsConfig();
                    tn.rows.add(rn);
                }
            }
        }
        return new MultiFileTreeTableModel(root, fw);
    }

    public MultiFileTreeTableModel(Object root, byte[] fw) {
        super(root);
        this.fw = fw;
    }

    @Override
    public int getColumnCount() {
        return 26;
    }

    @Override
    public String getColumnName(int i) {
        return i==0 ? "": "#"+(i-1);
    }

    @Override
    public Class<?> getColumnClass(int i) {
        if(i==0) {
            return TreeTableModel.class;
        }
        else {
            return Long.class;
        }
    }

    @Override
    public Object getValueAt(Object o, int column) {
        if(o instanceof RowNode && column > 0 ) {
            int inx = column-1;
            ValueConfig vc = ((RowNode) o).vc.get(inx);
            int addrOffset = vc.getAddrOffset();
            switch (vc.getType()) {
                case Int16:
                    return Utils.readShort(fw,((RowNode) o).addr+addrOffset);
                case Int32:
                    return Utils.readInt(fw,((RowNode) o).addr+addrOffset);
                case Byte:
                case UByte:
                    return Utils.readUByte(fw,((RowNode) o).addr+addrOffset);
                case UInt16:
                    return Utils.readUShort(fw,((RowNode) o).addr+addrOffset);
                case UInt32:
                    return Utils.readUShort(fw,((RowNode) o).addr+addrOffset);
                default:
                    throw new IllegalArgumentException(vc.getType()+ " type is not supported by this table model");
            }

        }
        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if(parent instanceof RootNode) {
            return ((RootNode) parent).files.get(index);
        }
        if(parent instanceof FileNode) {
            return ((FileNode) parent).tables.get(index);
        }
        if(parent instanceof TableNode) {
            return ((TableNode) parent).rows.get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if(parent instanceof RootNode) {
            return ((RootNode) parent).files.size();
        }
        if(parent instanceof FileNode) {
            return ((FileNode) parent).tables.size();
        }
        if(parent instanceof TableNode) {
            return ((TableNode) parent).rows.size();
        }
        return 0;
    }

    public boolean isCellEditable(Object node, int column) {
        if(this.getColumnClass(column) == TreeTableModel.class) {
            return true;
        }
        else if(node instanceof RowNode) {
            return true;
        }
        return false;
    }

    public void setValueAt(Object aValue, Object o, int column) {
        if(o instanceof RowNode && column > 0 ) {
            Long v = (Long) aValue;
            int inx = column-1;
            ValueConfig vc = ((RowNode) o).vc.get(inx);
            int addrOffset = vc.getAddrOffset();
            switch (vc.getType()) {
                case Int16:
                case UInt16:
                    Utils.writeUShort(fw,((RowNode) o).addr+addrOffset,v);
                    break;
                case Int32:
                case UInt32:
                    Utils.writeUInt(fw,((RowNode) o).addr+addrOffset,v);
                    break;
                case Byte:
                case UByte:
                    Utils.writeUByte(fw,((RowNode) o).addr+addrOffset, v);
                    break;
                default:
                    throw new IllegalArgumentException(vc.getType()+ " type is not supported by this table model");
            }

        }

    }



    static class RootNode {
        List<FileNode> files = new ArrayList<>();
        public String toString() {
            return "/";
        }
    }

    static class FileNode {
        String name;
        List<TableNode> tables = new ArrayList<>();
        public String toString() {
            return name;
        }
    }

    static class TableNode {
        String name;
        List<RowNode> rows = new ArrayList<>();
        public String toString() {
            return name;
        }

    }

    static class RowNode {
        String name;
        int addr;
        int len;
        List<ValueConfig> vc;
        public String toString() {
            return name;
        }
    }

}
