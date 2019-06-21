package org.vmax.bitrate.plugins;

import org.vmax.bitrate.cfg.Config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface PostProcessor {
    PostProcessor withConfig(Config cfg);
    void postprocess() throws IOException, NoSuchAlgorithmException;

}
