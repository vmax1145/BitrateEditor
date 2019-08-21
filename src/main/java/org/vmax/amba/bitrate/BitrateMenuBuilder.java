package org.vmax.amba.bitrate;

import org.vmax.amba.bitrate.config.BitrateEditorConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BitrateMenuBuilder {

    private final JMenuBar bar;
    private EditorPanel editorPanel;
    private Bitrate[] bitrates;
    private Bitrate[] bitratesFiltered;
    private BitrateEditorConfig cfg;
    private BitrateCalcDialog bitrateCalcDialog;

    public BitrateMenuBuilder(JMenuBar jMenuBar) {
        this.bar = jMenuBar;
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
                BitrateGenerator.generate(bitrates);
                editorPanel.onDataChange();
            }
        });
        bar.add(advancedMenu);
        return bar;
    }


}
