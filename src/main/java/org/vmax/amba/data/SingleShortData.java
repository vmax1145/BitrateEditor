package org.vmax.amba.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.ShortValueCfg;

import javax.swing.*;

@NoArgsConstructor
@Getter
@Setter
public class SingleShortData extends ShortValueCfg {
    private short value;

    @JsonIgnore
    private JSlider slider;
}
