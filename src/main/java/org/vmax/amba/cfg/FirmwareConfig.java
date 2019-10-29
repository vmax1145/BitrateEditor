package org.vmax.amba.cfg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vmax.amba.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FirmwareConfig {

    private String note = "";
    private String toolClass;
    private String fwFileName;
    private List<Verify> verify = new ArrayList<>();
    private ProcessorConfig preProcessor;
    private ProcessorConfig postProcessor;
    private boolean showFileDialog = false;

    public static <T extends FirmwareConfig> T readConfig(Class<T> clz,String arg) throws IOException {
        try(FileInputStream fis = new FileInputStream(arg)) {
            return Utils.getObjectMapper()
                    .enable(JsonParser.Feature.ALLOW_COMMENTS)
                    .readerFor(clz)
                    .readValue(fis);
        } catch (Exception e) {
            throw new IOException("Error parsing config: "+arg, e);
        }
    }

}
