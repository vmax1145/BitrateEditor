package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Verify {
    private Integer addr;
    private String val;
    private Integer int32val;
    private CRCverify crc;
    private Integer section;
    private List<String> files = new ArrayList<>();
    private List<Verify> verifies = new ArrayList<>();
}
