package org.vmax.amba.cfg;

import java.awt.*;

public enum Colors {
    red(0xff0000),
    green(0x00ff00),
    blue(0x0000ff)
    ;

    private final int v;

    Colors(int v) {
        this.v=v;
    }

    public Color getColor() {
        return new Color(v);
    }
}
