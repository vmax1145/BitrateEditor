package org.vmax.amba.generic;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.*;
import org.vmax.amba.cfg.tabledata.ByteBlockConfig;
import org.vmax.amba.cfg.tabledata.FileListConfig;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GenericTool extends FirmwareTool<GenericTableDataConfig> {

    private GenericTableDataConfig<ImageConfig> cfg;
    private List<Patch> patchList = new ArrayList<>();
    private List<GenericImageTab> imageTabs = new ArrayList<>();
    private List<GenericTab> allTabs = new ArrayList<>();
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
            GenericJTable editorPanel = new GenericJTable(this, tdcfg, model);
            tabs.add(tdcfg.getLabel(), new JScrollPane(editorPanel));
            allTabs.add(editorPanel);
        }
        for(ParamsConfig pcfg : this.cfg.getParamsTabs()) {
            GenericParamsDataModel model = new GenericParamsDataModel(pcfg, fwBytes);
            GenericParamsTable editorPanel = new GenericParamsTable(pcfg, model);
            tabs.add(pcfg.getLabel(), new JScrollPane(editorPanel));
            allTabs.add(editorPanel);
        }
        for(ImageConfig icfg : this.cfg.getImageTabs()) {
            GenericImageTab imgTab = createImageTab(icfg,fwBytes);
            imageTabs.add(imgTab);
            JPanel p = new JPanel();
            p.add(imgTab.getComponent());
            tabs.add(imgTab.getTabLabel(), new JScrollPane(p));
            allTabs.add(imgTab);
        }
        for(ByteBlockConfig bcfg : this.cfg.getByteBlockTabs()) {
            ByteBlockTab byteBlockTab = createByteBlockTab(bcfg,fwBytes);
            JPanel p = new JPanel();
            p.add(byteBlockTab.getComponent());
            tabs.add(byteBlockTab.getTabLabel(), new JScrollPane(p));
            allTabs.add(byteBlockTab);
        }
        for(FileListConfig flfcg : this.cfg.getFileListTabs()) {
            FileListTab fileTab = createFileListTab(flfcg,fwBytes);
            tabs.add(fileTab.getTabLabel(), new JScrollPane(fileTab.getComponent()));
            allTabs.add(fileTab);
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

    private FileListTab createFileListTab(FileListConfig fcfg, byte[] fwBytes) {
        return new FileListTab(this,fcfg,fwBytes);
    }

    private ByteBlockTab createByteBlockTab(ByteBlockConfig bcfg, byte[] fwBytes) {
        return new ByteBlockTab(this, bcfg,fwBytes);
    }

    protected GenericImageTab createImageTab(ImageConfig icfg, byte[] fwBytes) throws Exception {
        throw new Exception("Image tab not implemented in base class");
    }

    protected List<Patch> loadPatches(GenericTableDataConfig cfg) {
        return new ArrayList<>();
    };


    @Override
    public void updateFW() {
        try {
            applyPatches();
            applyImages();
            Utils.saveFirmware(this, cfg, fwBytes);
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Oooops! See error stream for details");
        }
    }

    private void applyImages() {
        for(GenericImageTab imageTab : imageTabs) {
            imageTab.updateFW();
        }
    }

    private void applyPatches() {
        for(Patch p : patchList) {
            if(p.isApply()) {
                for(PatchEntry e : p.getEntries()) {
                    System.arraycopy(e.getBytes(),0,fwBytes, e.getAddr(),e.getBytes().length);
                }
            }
        }
    }

    @Override
    public Class<? extends GenericTableDataConfig> getConfigClz() {
        return GenericTableDataConfig.class;
    }

    @Override
    protected List<ImportAction> getImportActions() {
        List<ImportAction> ret = new ArrayList<>();
        allTabs.forEach(t->{
            ImportAction action = t.getImportAction();
            if(action!=null) {
                ret.add(action);
            }
        });
        return ret;
    }

    @Override
    protected List<ExportAction> getExportActions() {
        List<ExportAction> ret = new ArrayList<>();
        allTabs.forEach(t->{
            ExportAction action = t.getExportAction();
            if(action!=null) {
                ret.add(action);
            }
        });
        return ret;

    }

    protected List<Action> getOtherActions() {
        List<Action> ret = new ArrayList<>();
        allTabs.forEach(t->{
            List<Action> actions = t.getOtherActions();
            if(actions!=null) {
                ret.addAll(actions);
            }
        });
        return ret;
    }
}
