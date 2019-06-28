package org.vmax.amba.cfg.bitrate;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.Validate;

@Getter
@Setter
@NoArgsConstructor
public class BitrateEditorConfig extends FirmwareConfig {


    private int bitratesTableAddress;
    private int gopTableAddress=0;
    private BitrateName[] videoModes;
    private String[] qualities;


    private Validate validate;



}
