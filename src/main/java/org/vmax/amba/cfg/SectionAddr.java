package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SectionAddr {

    public static int SECTION_HEADER_LEN = 0x100;

    Integer sectionNum;
    String fileName;
    String findHex;
    int findSkip = 0;
    int relAddr;
}
