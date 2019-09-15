package org.vmax.amba.tables.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TableSetConfig {
    private String label;
    private SingleTableConf[] tables;
}
