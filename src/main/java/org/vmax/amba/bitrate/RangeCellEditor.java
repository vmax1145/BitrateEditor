package org.vmax.amba.bitrate;

import org.vmax.amba.cfg.Range;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RangeCellEditor extends DefaultCellEditor {

    private final JTextField textField;
    private Range range;
    private static final Border red = new LineBorder(Color.red);
    private static final Border black = new LineBorder(Color.black);

    public RangeCellEditor(Range range) {
        super(new JTextField());
        this.textField = (JTextField) editorComponent;
        this.range = range;
    }


    @Override
    public boolean stopCellEditing() {
        try {
            String s = textField.getText();
            Float v;
            if(s.startsWith("#")) {
                v = new Long(Long.parseLong(s.substring(1),16)).floatValue();
            }
            else {
                v = Float.valueOf(s);
            }
            if(range!=null) {
                if (v < range.getMin() || v > range.getMax()) {
                    throw new NumberFormatException();
                }
            }
            return super.stopCellEditing();
        } catch (NumberFormatException e) {
            textField.setBorder(red);
            return false;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setBorder(black);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
