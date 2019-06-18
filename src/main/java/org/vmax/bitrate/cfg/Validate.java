package org.vmax.bitrate.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Validate {
    private Range bitrate;
    private Range min;
    private Range max;
}
