package org.vmax.amba.bitrate.config;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.bitrate.Bitrate;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.SectionAddr;
import org.vmax.amba.cfg.Validate;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class BitrateEditorConfig extends FirmwareConfig {


    private Integer bitratesTableAddress;
    private Integer gopTableAddress;

    private SectionAddr bitratesTableLocation;
    private SectionAddr gopTableLocation;


    private BitrateName[] videoModes;
    private String[] qualities;
    private Map<Bitrate.Type, Integer> bitrateTypeMapping;

    private Validate validate;



}
