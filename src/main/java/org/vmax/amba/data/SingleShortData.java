package org.vmax.amba.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.ShortValueCfg;

@NoArgsConstructor
@Getter
@Setter
public class SingleShortData extends ShortValueCfg {
    private short originalValue;
    private short value;
}
