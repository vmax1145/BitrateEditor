package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.tabledata.ValueConfig;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MultiFilesTablesConfig<I> extends GenericTableDataConfig<I> {
    Integer sectionNum;
    private List<String> filenames = new ArrayList<>();
    private int  tableLen;
    private int  tablesPerFile;
    private String findHex;
    private int findSkip = 0;
    private int relAddr;
    private int rowLength;

    List<String> rowNames = new ArrayList<>();

    private List<ValueConfig> columnsConfig;

}
