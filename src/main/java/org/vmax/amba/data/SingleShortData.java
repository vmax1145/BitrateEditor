package org.vmax.amba.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.EditableValueCfg;

@NoArgsConstructor
@Getter
@Setter
public class SingleShortData extends EditableValueCfg {
    private short originalValue;
    private short value;
}
