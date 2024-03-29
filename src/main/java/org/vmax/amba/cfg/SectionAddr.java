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
    ValueFinder findValue;

    public SectionAddr(SectionAddr location) {
        this.sectionNum = location.sectionNum;
        this.fileName = location.fileName;
        this.findHex = location.findHex;
        this.findSkip = location.findSkip;
        this.relAddr = location.relAddr;
        this.findValue = location.findValue;
    }

    @Override
    public String toString() {
        return "SectionAddr{" +
                "sectionNum=" + sectionNum +
                ", fileName='" + fileName + '\'' +
                ", findHex='" + findHex + '\'' +
                ", findSkip=" + findSkip +
                ", relAddr=" + relAddr +
                ", findValue=" + findValue.toString()+
                '}';
    }

}
