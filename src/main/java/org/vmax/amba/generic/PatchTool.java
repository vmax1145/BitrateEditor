package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Patch;
import org.vmax.amba.cfg.PatchEntry;
import org.vmax.amba.cfg.PatchToolConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public abstract class PatchTool<T extends PatchToolConfig> extends FirmwareTool<T> {

    private T cfg;
    private byte[] fwBytes;
    private List<Patch> patchList;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
        return "Patch tool";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) throws Exception {
        this.cfg = (T) acfg;
        this.fwBytes = fwBytes;

        JMenuBar bar = buildMenu();

        patchList = loadPatches(cfg);
        PatchDataModel model = new PatchDataModel(patchList, fwBytes);
        JTable jTable = new JTable(model) {
                public String getToolTipText(MouseEvent e) {
                    java.awt.Point p = e.getPoint();
                    int rowIndex = rowAtPoint(p);
                    if(rowIndex<patchList.size()) {
                        return patchList.get(rowIndex).getDescription();
                    }
                    return super.getToolTipText(e);
                }
        };

        JScrollPane jsp = new JScrollPane(jTable);
        jsp.setPreferredSize(new Dimension(600,400));
        add(jsp, BorderLayout.CENTER);
        setJMenuBar(bar);
    }

    protected abstract List<Patch> loadPatches(T cfg) throws Exception;



    @Override
    public void exportData(File f) {
        JOptionPane.showMessageDialog(this,"Not implemented yet");
    }

    @Override
    public void importData(File f) {
        JOptionPane.showMessageDialog(this,"Not implemented yet");
    }


    @Override
    public void updateFW() {
            try {
                for(Patch p : patchList) {
                    if(p.isApply()) {
                        for(PatchEntry e : p.getEntries()) {
                            System.arraycopy(e.getBytes(),0,fwBytes, e.getAddr(),e.getBytes().length);
                        }
                    }
                }
                Utils.saveFirmware(this, cfg, fwBytes);
            }
            catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,"Oooops! See error stream for details");
            }
    }


}
