package org.vmax.amba.convolution;

import org.vmax.amba.FirmwareTool;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.MultiFilesTablesConfig;
import org.vmax.amba.cfg.tabledata.TableDataConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;
import org.vmax.amba.generic.ExportAction;
import org.vmax.amba.generic.ImportAction;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConvolutionFilterTool extends FirmwareTool<MultiFilesTablesConfig> {
    private MultiFilesTablesConfig cfg;
    private byte[] fw;
    FilterEditorPanel editor;

    @Override
    public void init(FirmwareConfig config, byte[] fwBytes) throws Exception {
        this.cfg = (MultiFilesTablesConfig)config;
        this.fw = fwBytes;
        editor = new FilterEditorPanel("samples/no_sharp.png");

        List<String> files = cfg.getFilenames();
        JComboBox<String> filesSelect = new JComboBox<>(files.toArray(new String[0]));
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("  File:"));
        topPanel.add(filesSelect);
        Integer[] tableNumbers = new Integer[cfg.getTablesPerFile()];
        for(int i=0;i<tableNumbers.length;i++) tableNumbers[i] = i;
        JComboBox<Integer> tableNumberSelect = new JComboBox<>(tableNumbers);
        topPanel.add(new JLabel("  Table N:"));
        topPanel.add(tableNumberSelect);

        List<String> rowNames = cfg.getRowNames();
        String[] rowItems = rowNames.toArray(new String[0]);
        JComboBox<String> rowSelect = new JComboBox<>(rowItems);
        topPanel.add(new JLabel("  Row:"));
        topPanel.add(rowSelect);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(topPanel,BorderLayout.NORTH);
        p.add(editor,BorderLayout.CENTER);
        getContentPane().add(p);

        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rowSelectionChanged(filesSelect.getSelectedIndex(), tableNumberSelect.getSelectedIndex(),rowSelect.getSelectedIndex());
            }
        };

        filesSelect.addActionListener(l);
        tableNumberSelect.addActionListener(l);
        rowSelect.addActionListener(l);

        rowSelectionChanged(filesSelect.getSelectedIndex(), tableNumberSelect.getSelectedIndex(),rowSelect.getSelectedIndex());

        TableModel applyTableModel = new ApplyTableModel(cfg.getFilenames(),cfg.getTablesPerFile(),cfg.getRowNames().size());
        
    }

    private void rowSelectionChanged(int fileN, int tableN, int rowN) {
        if(fileN<0) fileN=0;
        if(tableN<0) tableN=0;
        if(rowN<0) rowN=0;

        int inx = (fileN*cfg.getTablesPerFile()+tableN);
        TableDataConfig tcfg = cfg.getTableDataConfigs().get(inx);
        System.out.println(tcfg);
        int addr = tcfg.getRowsConfig().getFirstRowAddr();
        addr+=tcfg.getRowsConfig().getRowLenth()*rowN;

        System.out.println(addr);
        int[] data = new int[tcfg.getColumnsConfig().size()];
        int i=0;
        List<Integer> l = new ArrayList<>();
        for(ValueConfig vc : tcfg.getColumnsConfig()) {
             data[i] = (int)Utils.readShort(fw,addr+vc.getAddrOffset());
             l.add(data[i]);
             i++;
        }

        System.out.println(l.toString());
        editor.setRow(data);
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
