package org.vmax.amba.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SingleShortData {

    private int addr;
    private short originalValue;
    private short value;

}
