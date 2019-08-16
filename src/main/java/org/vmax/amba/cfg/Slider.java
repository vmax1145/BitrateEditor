package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Slider {
    private int addr;
    private String name;
    private Type   type;
    private Range  range;
}
