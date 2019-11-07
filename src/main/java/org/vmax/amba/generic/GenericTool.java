package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.*;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenericTool extends FirmwareTool<GenericTableDataConfig> {

    private GenericTableDataConfig<ImageConfig> cfg;
    private List<Patch> patchList;
    private byte[] fwBytes;

    @Override
    public String getStartMessage(FirmwareConfig cfg) {
            return "Generic editor";
    }

    @Override
    public void init(FirmwareConfig acfg, byte[] fwBytes) throws Exception {
        this.cfg = (GenericTableDataConfig) acfg;
        this.fwBytes = fwBytes;


        JTabbedPane tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(600,400));
        add(tabs, BorderLayout.CENTER);

        for(TableDataConfig tdcfg : this.cfg.getTableDataConfigs()) {
            GenericTableDataModel model = new GenericTableDataModel(tdcfg, fwBytes);
            GenericJTable editorPanel = new GenericJTable(tdcfg, model);
            tabs.add(tdcfg.getLabel(), new JScrollPane(editorPanel));
        }
        for(ParamsConfig pcfg : this.cfg.getParamsTabs()) {
            GenericParamsDataModel model = new GenericParamsDataModel(pcfg, fwBytes);
            GenericParamsTable editorPanel = new GenericParamsTable(pcfg, model);
            tabs.add(pcfg.getLabel(), new JScrollPane(editorPanel));
        }
        for(ImageConfig icfg : this.cfg.getImageTabs()) {
            JComponent p = createImageTab(icfg,fwBytes);
            tabs.add(icfg.getLabel(), new JScrollPane(p));
        }

        if(cfg.getPatchLoader()!=null) {
            patchList = loadPatches(cfg);
            PatchDataModel model = new PatchDataModel(patchList, fwBytes);
            JTable jTable = new JTable(model) {
                public String getToolTipText(MouseEvent e) {
                    java.awt.Point p = e.getPoint();
                    int rowIndex = rowAtPoint(p);
                    if (rowIndex < patchList.size()) {
                        return patchList.get(rowIndex).getDescription();
                    }
                    return super.getToolTipText(e);
                }
            };
            JScrollPane jsp = new JScrollPane(jTable);
            jsp.setPreferredSize(new Dimension(600,400));
            tabs.add("Patches",jsp);
        }

    }

    protected JComponent createImageTab(ImageConfig icfg, byte[] fwBytes) throws Exception {
        throw new Exception("Image tab not implemented in base class");
    }

    protected List<Patch> loadPatches(GenericTableDataConfig cfg) {
        return new ArrayList<>();
    };

    public boolean hasImportExport() {
        return false;
    }

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

            Utils.saveFirmware(this, cfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details");
        }
    }

    @Override
    public Class<? extends GenericTableDataConfig> getConfigClz() {
        return GenericTableDataConfig.class;
    }
}
