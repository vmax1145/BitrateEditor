package org.vmax.bitrate.bitrateui;

import org.vmax.bitrate.cfg.Range;

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
            Float v = Float.valueOf(textField.getText());
            if (v < range.getMin() || v>range.getMax()) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            textField.setBorder(red);
            return false;
        }
        return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setBorder(black);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
