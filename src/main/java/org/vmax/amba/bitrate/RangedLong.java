package org.vmax.amba.bitrate;

import lombok.NoArgsConstructor;
import org.vmax.amba.cfg.Range;

@NoArgsConstructor
public class RangedLong {

    private Long value;


    public RangedLong(String s, Range range) throws NumberFormatException {
        this.value = Long.valueOf(s);
        if(this.value < range.getMin() || this.value>range.getMax()) {
            throw new NumberFormatException("Out of range:"+range.getMin()+".."+range.getMax());
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Long getValue() {
        return value;
    }
}
