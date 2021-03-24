package org.vmax.amba.fwsource.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.FtpConfig;
import org.vmax.amba.fwsource.FwDestination;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FtpDestination extends FwDestination {

        private final FtpConfig cfg;

        public FtpDestination(FirmwareConfig cfg) {
            super(cfg);
            this.cfg = cfg.getFtpConfig();
        }

    @Override
    public void save(byte[] bytes) throws Exception {
        FTPClient ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        int reply;
        ftp.connect(cfg.getHost());
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }
        ftp.login(cfg.getLogin(), cfg.getPassword());
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();

        try {
            if (ftp.storeFile(cfg.getPath(), new ByteArrayInputStream(bytes))) {
                return;
            }
        }
        finally {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException ignored) {
                    // do nothing as file is already saved to server
                }
            }
        }
        throw new Exception("Error saving file to ftp:"+cfg.getPath());
    }


}
