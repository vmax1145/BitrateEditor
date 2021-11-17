package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TableDataConfig  {
    private String label;
    private NamedRowsConfig rowsConfig;
    private List<ValueConfig> columnsConfig = new ArrayList<>();

    @Override
    public String toString() {
        return "TableDataConfig{" +
                "label='" + label + '\'' +
                ", rowsConfig=" + rowsConfig +
                ", columnsConfig=" + columnsConfig +
                '}';
    }
}
