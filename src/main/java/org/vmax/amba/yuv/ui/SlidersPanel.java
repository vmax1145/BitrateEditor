package org.vmax.amba.yuv.ui;

import org.vmax.amba.data.SingleShortData;
import org.vmax.amba.yuv.YUVTabData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SlidersPanel extends JPanel {
    private List<JSlider> sliders = new ArrayList<>();
    private List<SliderValuesListener> listeners = new ArrayList<>();

    public SlidersPanel(YUVTabData tabData) {
        super(new SpringLayout());
        for(SingleShortData e : tabData) {
            JLabel label = new JLabel(e.getName(), JLabel.TRAILING);
            add(label);
            JSlider sliderField = new JSlider(e.getRange().getMin(),e.getRange().getMax(),Math.round(e.getValue()));
            sliders.add(sliderField);
            label.setLabelFor(sliderField);
            add(sliderField);
            JTextField val=new JTextField(4);
            val.setEditable(false);
            add(val);
            val.setText(Short.toString(e.getValue()));

            sliderField.addChangeListener(e1 -> {
                int v = sliderField.getValue();
                val.setText(Short.toString((short) v));
                valuesChanged();
            });
        }

        SpringUtilities.makeCompactGrid(this,
                tabData.size(), 3, //rows, cols
                6, 6,        //initX, initY
                6, 6);

    }

    private void valuesChanged() {
        List<Integer> vals = sliders.stream().map(JSlider::getValue).collect(Collectors.toList());
        for(SliderValuesListener l : listeners) {
            l.valuesChanged(vals);
        }
    }

    public void addListener(SliderValuesListener listener) {
        this.listeners.add(listener);
    }
    public void removeListener(SliderValuesListener listener) {
        this.listeners.remove(listener);
    }

}
