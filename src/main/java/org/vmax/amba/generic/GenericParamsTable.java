package org.vmax.amba.generic;

import org.vmax.amba.bitrate.RangeCellEditor;
import org.vmax.amba.cfg.Range;
import org.vmax.amba.cfg.Type;
import org.vmax.amba.cfg.tabledata.ParamsConfig;
import org.vmax.amba.cfg.tabledata.ValueConfig;
import org.vmax.amba.generic.color.ColorEditor;
import org.vmax.amba.generic.color.ColorRenderer;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class GenericParamsTable extends JTable implements GenericTab{
    private ParamsConfig cfg;

    public GenericParamsTable(ParamsConfig cfg, GenericParamsDataModel model) {
        super(model);
        this.cfg = cfg;
        this.getColumnModel().getColumn(1).setCellEditor(new RangeCellEditor(null));
    }

    @Override
    public void setModel(TableModel m) {
        super.setModel(m);
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        Component comp = super.prepareRenderer(renderer, row, col);
        if(!Type.RGB555.equals(cfg.getParams().get(row).getType())) {
            if (row % 2 == 0)
                comp.setBackground(new Color(0xe0e0e0));
            else {
                comp.setBackground(new Color(0xf0f0f0));
            }
        }
        if(col==1) {
            Range r = cfg.getParams().get(row).getRange();
            if(r!=null && comp instanceof JComponent) {
                if(!Type.Float32.equals(cfg.getParams().get(row).getType())) {
                    ((JComponent) comp).setToolTipText("min:" + r.getMin().intValue() + " max:" + r.getMax().intValue());
                }
                else {
                    ((JComponent) comp).setToolTipText("min:" + r.getMin() + " max:" + r.getMax());
                }
            }
        }
        return comp;
    }


    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        if(column==1 && Type.RGB555.equals(cfg.getParams().get(row).getType())) {
            return new ColorRenderer();
        }
        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if(column==1) {
            ValueConfig vcfg = cfg.getParams().get(row);
            if(!vcfg.getValuesMapping().isEmpty()) {
                String[] vals = vcfg.getValuesMapping().keySet().toArray(new String[]{});
                JComboBox<String> select = new JComboBox<>(vals);
                return new DefaultCellEditor(select);
            }
            if(Type.RGB555.equals(vcfg.getType())) {
                return new ColorEditor();
            }
        }
        return super.getCellEditor(row,column);
    }

    @Override
    public String getTabLabel() {
        return cfg.getLabel();
    }

    @Override
    public ImportAction getImportAction() {
        return null;
    }

    @Override
    public ExportAction getExportAction() {
        return null;
    }
}
