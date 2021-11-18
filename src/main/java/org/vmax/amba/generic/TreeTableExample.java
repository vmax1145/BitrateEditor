package org.vmax.amba.generic;

import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import lombok.SneakyThrows;
import org.vmax.amba.Utils;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.MultiFilesTablesConfig;
import org.vmax.amba.fwsource.FwSource;
import org.vmax.amba.fwsource.FwSourceFactory;

import javax.swing.*;
import java.io.IOException;

public class TreeTableExample {
        public static void main(String[] args) throws IOException {

            SwingUtilities.invokeLater(new Runnable()
            {
                @SneakyThrows
                @Override
                public void run()
                {
                    createAndShowGUI();
                }
            });
        }

        private static void createAndShowGUI() throws Exception {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MultiFilesTablesConfig cfg = FirmwareConfig.readConfig(MultiFilesTablesConfig.class,"config-SJ8-SJ9/sharpeness.json");
            FwSource fwSource = FwSourceFactory.createSource(cfg);
            byte[] fwBytes = Utils.loadFirmware(cfg, fwSource);
            TreeTableModel treeTableModel = MultiFileTreeTableModel.create(cfg, fwBytes);
            JTreeTable treeTable = new JTreeTable(treeTableModel);
            f.getContentPane().add(new JScrollPane(treeTable));

            f.setSize(400,400);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }
}
