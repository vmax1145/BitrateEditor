package org.vmax.bitrate.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CRCverify {
    private int fromAddr;
    private int len;
}