package org.vmax.amba.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TableDataConfig {
    private NamedRowsConfig rowsConfig;
    private List<ColumnConfig> columnsConfig = new ArrayList<>();
}
