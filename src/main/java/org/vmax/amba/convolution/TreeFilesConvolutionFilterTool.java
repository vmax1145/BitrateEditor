package org.vmax.amba.convolution;

import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.MultiFilesTablesConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.ImportAction;
import org.vmax.amba.generic.MultiFileTreeTableModel;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeFilesConvolutionFilterTool extends FirmwareTool<MultiFilesTablesConfig> {
    private MultiFilesTablesConfig cfg;
    private byte[] fw;
    FilterEditorPanel editor;
    int popupOnRow = -1;
    int popupOnCol = -1;

    @Override
    public void init(FirmwareConfig config, byte[] fwBytes) throws Exception {
        this.cfg = (MultiFilesTablesConfig)config;
        this.fw = fwBytes;
        editor = new FilterEditorPanel("samples/no_sharp.png");

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
                    System.out.println("row="+row+" col="+col);
                    if (treeTable.getValueAt(row,col)!=null) {
                        popupOnRow = row;
                        popupOnCol = col;
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
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
                    for(ValueConfig vc : rn.getValueConfigs()) {
                        treeTableModel.setValueAt((long)(vals[inx]),p.getLastPathComponent(),inx+1);
                        inx++;
                    }
                }
            }
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
