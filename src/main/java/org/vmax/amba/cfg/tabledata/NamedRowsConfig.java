package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NamedRowsConfig {
    private int firstRowAddr;
    private int rowLenth;
    private List<String> rowNames = new ArrayList<>();
}
