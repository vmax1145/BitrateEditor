package org.vmax.amba.cfg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FtpConfig {
    private String host;
    private int port = 21;
    private String login;
    private String password;
    private String path;
}
