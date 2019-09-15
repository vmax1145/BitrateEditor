package org.vmax.amba.cfg;

import java.awt.*;

public enum Colors {
    red(0xff0000),
    green(0x00ff00),
    blue(0x0000ff),
    cyan(0x00ffff),
    yellow(0xffff00),
    magenta(0xff00ff),
    white(0xffffff),
    black(0x0),
    gray(0x808080),
    lightgray(0xc0c0c0),
    darkgray(0x404040)
    ;

    private final int v;

    Colors(int v) {
        this.v=v;
    }

    public Color getColor() {
        return new Color(v);
    }
}
