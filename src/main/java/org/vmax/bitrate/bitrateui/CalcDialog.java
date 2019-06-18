package org.vmax.bitrate.bitrateui;

import org.vmax.bitrate.Bitrate;
import org.vmax.bitrate.BitrateEditor;
import org.vmax.bitrate.cfg.Config;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CalcDialog extends JDialog {

    private static long FHD_FLOW = 60*1920*1080;
    private static long UHD_FLOW = 60*3840*2160;

    private static final Border red = new LineBorder(Color.red);
    private static final Border black = new LineBorder(Color.black);

    private Bitrate[] bitrates;
    private Config cfg;

    public CalcDialog(BitrateEditor editor, EditorPanel editorPanel, Config cfg, Bitrate[] bitrates) {
        super(editor, "Calculate bitrates", true);
        this.bitrates = bitrates;
        this.cfg = cfg;
        Container contentPane = getContentPane();
        GridLayout layout = new GridLayout(3,2,5,5);
        contentPane.setLayout(layout);
        contentPane.add(new JLabel("Bitrate for 4K60 : "));
        JTextField uhd60tf = new JTextField(cfg.getValidate().getBitrate().getMax().intValue()+"", 6);
        contentPane.add(uhd60tf);
        contentPane.add(new JLabel("Bitrate for 1080@60 : "));
        JTextField fhd60tf = new JTextField("60", 6);
        contentPane.add(fhd60tf);

        JButton calc = new JButton("Calculate");
        calc.setActionCommand("calc");

        contentPane.add(calc);

        JButton cancel = new JButton("Cancel");
        calc.setActionCommand("cancel");

        calc.addActionListener(e -> calculate(editorPanel, uhd60tf, fhd60tf));
        cancel.addActionListener(e -> setVisible(false));

        contentPane.add(calc);
        contentPane.add(cancel);
        pack();
    }

    private void calculate(EditorPanel editor, JTextField uhd60tf, JTextField fhd60tf) {
        try {
            double uhd60 = getValue(uhd60tf);
            double fhd60 = getValue(fhd60tf);

            double dy = uhd60 - fhd60;
            double dx = UHD_FLOW - FHD_FLOW;
            double k = dy/dx;
            double a = fhd60 - dy/dx*FHD_FLOW;

            if(a<0) {
                fhd60tf.setBorder(red);
            }

            for(Bitrate b : bitrates) {
                if(b.isInUse() && b.getWidth()!=null) {
                    double value = b.calculateFlow()*k+a;
                    for( int i=0; i<b.getMbps().length;i++) {
                        b.getMbps()[i] = (int) Math.round(value*(1-0.2*i));
                    }
                }
            }
            setVisible(false);
            ((BitratesTableModel)(editor.getModel())).fireTableDataChanged();
        }
        catch (Exception ignored) {
        }
    }

    private int getValue(JTextField tf) throws Exception {
        try {
            tf.setBorder(black);
            int ret = Integer.valueOf(tf.getText());
            if(ret > cfg.getValidate().getBitrate().getMax().intValue() || ret < 5) {
                throw new Exception();
            }
            return ret;
        }
        catch (Exception e) {
            tf.setBorder(red);
            throw e;
        }
    }
}
