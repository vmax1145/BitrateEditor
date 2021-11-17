package org.vmax.amba.cfg.tabledata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.SectionAddr;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NamedRowsConfig {
    private SectionAddr firstRowLocation;
    private Integer firstRowAddr;
    private int rowLenth;
    private List<String> rowNames = new ArrayList<>();

    @Override
    public String toString() {
        return "NamedRowsConfig{" +
                "firstRowLocation=" + firstRowLocation +
                ", firstRowAddr=" + firstRowAddr +
                ", rowLenth=" + rowLenth +
                ", rowNames=" + rowNames +
                '}';
    }
}
