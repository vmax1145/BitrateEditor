package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ParamsConfig {
    private String label;
    private int baseAddr = 0;
    private List<ValueConfig> params = new ArrayList<>();
}
