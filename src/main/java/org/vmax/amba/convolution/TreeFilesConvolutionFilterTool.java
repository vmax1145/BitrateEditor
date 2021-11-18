package org.vmax.amba.convolution;

import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.MultiFilesTablesConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.ImportAction;
import org.vmax.amba.generic.MultiFileTreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

public class TreeFilesConvolutionFilterTool extends FirmwareTool<MultiFilesTablesConfig> {
    private MultiFilesTablesConfig cfg;
    private byte[] fw;
    FilterEditorPanel editor;
    int popupOnRow = -1;
    int popupOnCol = -1;
    TreePath popupOnPath = null;

    int[] rowClipboard = null;
    int[][] tableClipboard = null;

    @Override
    public void init(FirmwareConfig config, byte[] fwBytes) throws Exception {
        this.cfg = (MultiFilesTablesConfig)config;
        this.fw = fwBytes;
        editor = new FilterEditorPanel("samples/dubai.jpg");

        TreeTableModel treeTableModel = MultiFileTreeTableModel.create(cfg, fwBytes);
        JTreeTable treeTable = new JTreeTable(treeTableModel);
        treeTable.getColumnModel().getColumn(0).setPreferredWidth(350);
        JScrollPane treePanel = new JScrollPane(treeTable);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(treePanel,BorderLayout.NORTH);
        p.add(editor,BorderLayout.CENTER);
        getContentPane().add(p);


        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem copyRowItem = new JMenuItem(new AbstractAction("Copy row") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyRow(treeTableModel, popupOnPath);
            }
        });
        popupMenu.add(copyRowItem);
        JMenuItem pasteRowItem = new JMenuItem(new AbstractAction("Paste row") {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteRow(treeTable,treeTableModel,popupOnPath);
            }
        });
        popupMenu.add(pasteRowItem);
        popupMenu.add(new JPopupMenu.Separator());

        JMenuItem copyTableItem = new JMenuItem(new AbstractAction("Copy table") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyTable(treeTableModel,popupOnPath);
            }
        });
        popupMenu.add(copyTableItem);
        JMenuItem pasteTableItem = new JMenuItem(new AbstractAction("Paste table") {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteTable(treeTable, treeTableModel,popupOnPath);
            }
        });
        popupMenu.add(pasteTableItem);
        popupMenu.add(new JPopupMenu.Separator());


        JMenuItem toPreview = new JMenuItem(new AbstractAction("copy row to Preview") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToPreview(treeTable, popupOnRow);
            }
        });
        JMenuItem toFirmware = new JMenuItem(new AbstractAction("update selected rows from Preview") {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToFirmware(treeTable,treeTableModel, popupOnRow);
            }
        });

        popupMenu.add(toPreview);
        popupMenu.add(toFirmware);

        treeTable.addMouseListener( new MouseAdapter(){
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = treeTable.rowAtPoint(e.getPoint());
                    int col = treeTable.columnAtPoint(e.getPoint());
                    if (treeTable.getValueAt(row,col)!=null) {
                        popupOnRow = row;
                        popupOnCol = col;
                    }
                    TreePath p = treeTable.getTree().getPathForLocation(e.getX(),e.getY());
                    if(p==null) {
                        return;
                    }
                    popupOnPath = p;
                    System.out.println(p.toString());
                    Object last = p.getLastPathComponent();
                    if(last instanceof MultiFileTreeTableModel.RowNode) {
                        copyRowItem.setEnabled(true);
                        pasteRowItem.setEnabled(rowClipboard!=null);
                        toPreview.setEnabled(true);
                        copyTableItem.setEnabled(true);
                        pasteTableItem.setEnabled(tableClipboard!=null);
                    }
                    else {
                        copyRowItem.setEnabled(false);
                        pasteRowItem.setEnabled(false);
                        toPreview.setEnabled(false);
                        if(last instanceof MultiFileTreeTableModel.TableNode){
                            copyTableItem.setEnabled(true);
                            pasteTableItem.setEnabled(tableClipboard!=null);
                        }
                        else {
                            copyTableItem.setEnabled(false);
                            pasteTableItem.setEnabled(false);
                        }
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }

    private void copyToFirmware(JTreeTable treeTable, TreeTableModel treeTableModel, int popupOnRow) {
        TreePath[] paths = treeTable.getTree().getSelectionPaths();
        if(paths!=null) {
            int[] vals = editor.getRow();
            for (TreePath p : paths) {
                if(p.getLastPathComponent() instanceof MultiFileTreeTableModel.RowNode) {
                    MultiFileTreeTableModel.RowNode rn = (MultiFileTreeTableModel.RowNode) p.getLastPathComponent();
                    int inx=0;
                    List<ValueConfig> valueConfigs = rn.getValueConfigs();
                    for (int i = 0; i<valueConfigs.size();i++) {
                        treeTableModel.setValueAt((long) (vals[inx]), rn, i+1);
                        inx++;
                    }
                    MultiFileTreeTableModel.FileNode  fn = (MultiFileTreeTableModel.FileNode) p.getParentPath().getParentPath().getLastPathComponent();
                    MultiFileTreeTableModel.TableNode tn = (MultiFileTreeTableModel.TableNode) p.getParentPath().getLastPathComponent();
                    System.out.println("Row: "+rn.getName()+"  in table: "+tn.getName()+" in file: "+fn.getName()+" updated");

                }
                if(p.getLastPathComponent() instanceof MultiFileTreeTableModel.TableNode) {
                    MultiFileTreeTableModel.TableNode tn = (MultiFileTreeTableModel.TableNode) p.getLastPathComponent();

                    for(MultiFileTreeTableModel.RowNode rn : tn.getRows()) {
                        List<ValueConfig> valueConfigs = rn.getValueConfigs();
                        int inx=0;
                        for (int i = 0; i < valueConfigs.size(); i++) {
                            treeTableModel.setValueAt((long) (vals[inx]), rn, i + 1);
                            inx++;
                        }
                    }
                    MultiFileTreeTableModel.FileNode fn = (MultiFileTreeTableModel.FileNode) p.getParentPath().getLastPathComponent();
                    System.out.println("All rows in table: "+tn.getName()+" in file:"+fn.getName()+" updated");
                }
                if(p.getLastPathComponent() instanceof MultiFileTreeTableModel.FileNode) {
                    MultiFileTreeTableModel.FileNode fn = (MultiFileTreeTableModel.FileNode) p.getLastPathComponent();
                    for(MultiFileTreeTableModel.TableNode tn : fn.getTables()) {
                        for (MultiFileTreeTableModel.RowNode rn : tn.getRows()) {
                            List<ValueConfig> valueConfigs = rn.getValueConfigs();
                            int inx = 0;
                            for (int i = 0; i < valueConfigs.size(); i++) {
                                treeTableModel.setValueAt((long) (vals[inx]), rn, i + 1);
                                inx++;
                            }
                        }
                    }
                    System.out.println("All rows of all tables in file: "+fn.getName()+" updated");
                }


            }
        }
        treeTable.updateUI();
    }


    private void copyTable(TreeTableModel model, TreePath path) {
        this.tableClipboard = null;
        if(path == null) return;
        while (path!=null) {
            Object last = path.getLastPathComponent();
            int[][] ret = new int[cfg.getRowNames().size()][];
            if(last instanceof MultiFileTreeTableModel.TableNode) {
                MultiFileTreeTableModel.TableNode tn = (MultiFileTreeTableModel.TableNode) last;
                for(int i=0; i< tn.getRows().size(); i++) {
                    MultiFileTreeTableModel.RowNode rn = tn.getRows().get(i);
                    int[] rowData = new int[rn.getValueConfigs().size()];
                    for(int j=0; j< rn.getValueConfigs().size();j++) {
                        rowData[j] = ((Long) model.getValueAt(rn,j+1)).intValue();
                    }
                    ret[i]=rowData;
                }
                this.tableClipboard = ret;
                return;
            }
            path = path.getParentPath();
        }
    }

    private void pasteTable(JTreeTable treeTable, TreeTableModel model, TreePath path) {
        if(path == null || tableClipboard == null ) return;
        while (path!=null) {
            Object last = path.getLastPathComponent();
            int[][] ret = new int[cfg.getRowNames().size()][];
            if(last instanceof MultiFileTreeTableModel.TableNode) {
                MultiFileTreeTableModel.TableNode tn = (MultiFileTreeTableModel.TableNode) last;
                for(int i=0; i< tn.getRows().size(); i++) {
                    MultiFileTreeTableModel.RowNode rn = tn.getRows().get(i);
                    int[] rowData = this.tableClipboard[i];
                    for(int j=0; j< rn.getValueConfigs().size();j++) {
                         model.setValueAt((long)rowData[j], rn,j+1);
                    }
                    ret[i]=rowData;
                }
                this.tableClipboard = ret;
                treeTable.updateUI();
                return;
            }
            path = path.getParentPath();
        }
    }


    private void copyRow(TreeTableModel model, TreePath popupOnPath) {
        this.rowClipboard = null;
        if(popupOnPath==null) {
            return;
        }
        Object node = popupOnPath.getLastPathComponent();
        if(!(node instanceof MultiFileTreeTableModel.RowNode)) return;

        int n = cfg.getColumnsConfig().size();
        int[] vals = new int[n];
        for(int i =0;i<n; i++) {
            Integer v = ((Long) model.getValueAt(node, i + 1)).intValue();
            vals[i] = v;
        }
        this.rowClipboard = vals;
    }

    private void pasteRow(JTreeTable treeTable, TreeTableModel model, TreePath path) {
        if(path == null || this.rowClipboard==null) return;
        Object o = path.getLastPathComponent();
        if(!(o instanceof MultiFileTreeTableModel.RowNode)) return;

        for(int i=0;i<rowClipboard.length;i++) {
            model.setValueAt((long)rowClipboard[i],o,i+1);
        }

        treeTable.updateUI();
    }

    private void copyToPreview(JTreeTable treeTable, int popupOnRow) {
        if(popupOnRow<0) return;
        int n = cfg.getColumnsConfig().size();
        int[] vals = new int[n];

        for(int i =0;i<n; i++) {
            Integer v = ((Long) treeTable.getValueAt(popupOnRow, i + 1)).intValue();
            vals[i] = v;
        }
        editor.setRow(vals);
    }


    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return null;
    }

    @Override
    public void updateFW() {
        try {
            Utils.saveFirmware(cfg, fw);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details");
        }
    }

    @Override
    public Class<? extends MultiFilesTablesConfig> getConfigClz() {
        return MultiFilesTablesConfig.class;
    }

    @Override
    protected List<ImportAction> getImportActions() {
        return Collections.emptyList();
    }

    @Override
    protected List<ExportAction> getExportActions() {
        return  Collections.emptyList();
    }
}
