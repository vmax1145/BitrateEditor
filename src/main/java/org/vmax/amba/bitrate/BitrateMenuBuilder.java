package org.vmax.amba.bitrate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vmax.amba.DetectGenerator;
import org.vmax.amba.cfg.bitrate.BitrateEditorConfig;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class BitrateMenuBuilder {

    private final BitrateTool bitrateEditor;
    private EditorPanel editorPanel;
    private Bitrate[] bitrates;
    private Bitrate[] bitratesFiltered;
    private BitrateEditorConfig cfg;
    private BitrateCalcDialog bitrateCalcDialog;

    public BitrateMenuBuilder(BitrateTool bitrateEditor) {
        this.bitrateEditor = bitrateEditor;
    }
    public BitrateMenuBuilder with(EditorPanel editorPanel) {
        this.editorPanel = editorPanel;
        return this;
    }

    public BitrateMenuBuilder with(BitrateEditorConfig cfg) {
        this.cfg = cfg;
        return this;
    }

    public BitrateMenuBuilder with(Bitrate[] bitrates, Bitrate[] bitratesFiltered) {
        this.bitrates = bitrates;
        this.bitratesFiltered =bitratesFiltered;
        return this;
    }


    public BitrateMenuBuilder with(BitrateCalcDialog bitrateCalcDialog) {
        this.bitrateCalcDialog = bitrateCalcDialog;
        return this;
    }

    public JMenuBar build() {
        JMenuBar bar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        bar.add(fileMenu);

        fileMenu.add(new AbstractAction("Export bitrates") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showSaveDialog(bitrateEditor);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(selectedFile.exists()) {
                        int dialogResult = JOptionPane.showConfirmDialog (jfc, "Owerwrite existing file?","Warning",JOptionPane.YES_NO_OPTION);
                        if(dialogResult != JOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    try {
                        try(FileWriter fw = new FileWriter(selectedFile)) {
                            new ObjectMapper().writer().writeValue(fw,bitrates);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        fileMenu.add(new AbstractAction("Import bitrates") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(new File(".\\"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files", "json");
                jfc.addChoosableFileFilter(filter);
                int returnValue = jfc.showOpenDialog(bitrateEditor);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    if(!selectedFile.exists()) {
                        JOptionPane.showConfirmDialog (jfc, "File not exists","Warning",JOptionPane.OK_OPTION);
                        return;
                    }
                    try {
                        try (FileInputStream fis = new FileInputStream(selectedFile)) {
                            Bitrate[] bitratesLoaded = new ObjectMapper().readerFor(Bitrate[].class).readValue(fis);
                            if(bitrates.length != bitratesLoaded.length) {
                                JOptionPane.showConfirmDialog (jfc, "File not match configuration","Warning",JOptionPane.OK_OPTION);
                            }
                            if(cfg.getQualities().length != bitratesLoaded[0].getMbps().length) {
                                JOptionPane.showConfirmDialog (jfc, "File not match configuration","Warning",JOptionPane.OK_OPTION);
                            }
                            for (int i = 0; i < bitrates.length; i++) {
                                Bitrate b = bitrates[i];
                                b.fillFrom(bitratesLoaded[i]);
                            }
                            editorPanel.onDataChange();
                        }
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });



        fileMenu.add(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                bitrateEditor.updateFW(cfg,bitrates);
            }
        });

        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });


        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(new AbstractAction("Calculate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                bitrateCalcDialog.setVisible(true);
            }
        });
        bar.add(toolsMenu);


        JMenu advancedMenu = new JMenu("Advanced");

        JCheckBoxMenuItem showActive = new JCheckBoxMenuItem("Show active only");
        showActive.setSelected(true);
        showActive.addActionListener(e -> {
            boolean selected = showActive.getModel().isSelected();
            if(!selected) {
                editorPanel.setModel(new BitratesTableModel(cfg,bitrates));
            }
            else {
                editorPanel.setModel(new BitratesTableModel(cfg,bitratesFiltered));
            }
            editorPanel.onDataChange();
        });

        advancedMenu.add(showActive);
        advancedMenu.addSeparator();

        advancedMenu.add(new AbstractAction("Generate test bitrates") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DetectGenerator.generate(bitrates);
                editorPanel.onDataChange();
            }
        });
        bar.add(advancedMenu);
        return bar;
    }
}
