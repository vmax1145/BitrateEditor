package org.vmax.bitrate.cfg;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Config {

    private String note = "";

    private int bitratesTableAddress;
    private int gopTableAddress=0;
    private BitrateName[] videoModes;
    private String[] qualities;

    private int sectionStartAddr;
    private int sectionLen;
    private int sectionCrcAddr;

    private String fwFileName;
    private String md5fileName;

    private List<Verify> verify = new ArrayList<>();

    private Validate validate;

    private ProcessorConfig preProcessor;
    private ProcessorConfig postProcessor;

    public static Config readConfig(String arg) throws IOException {
        try(FileInputStream fis = new FileInputStream(arg)) {
            return new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS)
                    .readerFor(Config.class)
                    .readValue(fis);
        }
    }
}
