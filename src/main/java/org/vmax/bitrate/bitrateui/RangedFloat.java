package org.vmax.bitrate.bitrateui;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RangedFloat{
    Float value;


    public RangedFloat(float value) {
        this.value = value;
    }

    public RangedFloat(double value) {
        this.value = (float) value;
    }

    public RangedFloat(String s) throws NumberFormatException {
        this.value = Float.valueOf(s);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Float getValue() {
        return value;
    }
}
