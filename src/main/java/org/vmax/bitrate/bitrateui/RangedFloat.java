package org.vmax.bitrate.bitrateui;

import lombok.NoArgsConstructor;
import org.vmax.bitrate.cfg.Range;

@NoArgsConstructor
public class RangedFloat{
    Float value;


    public RangedFloat(String s, Range range) throws NumberFormatException {
        this.value = Float.valueOf(s);
        if(this.value < range.getMin() || this.value>range.getMax()) {
            throw new NumberFormatException("Out of range:"+range.getMin()+".."+range.getMax());
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Float getValue() {
        return value;
    }
}
