package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShortValueCfg {
    private Integer addr;
    private SectionAddr location;
    private String name;
    private Type   type;
    private IntRange range;
}
