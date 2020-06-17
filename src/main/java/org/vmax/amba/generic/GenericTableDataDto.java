package org.vmax.amba.generic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class GenericTableDataDto {
    int ncol;
    int nrow;
    List<List<String>> values = new ArrayList<>();
}
