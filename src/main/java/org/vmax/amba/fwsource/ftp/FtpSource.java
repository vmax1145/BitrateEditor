package org.vmax.amba.fwsource.ftp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.vmax.amba.cfg.FirmwareConfig;
import org.vmax.amba.cfg.FtpConfig;
import org.vmax.amba.fwsource.FwSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FtpSource extends FwSource {

    private final FtpConfig cfg;

    public FtpSource(FirmwareConfig cfg) {
        super(cfg);
        this.cfg = cfg.getFtpConfig();
    }

    @Override
    public byte[] load() throws Exception {

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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if(ftp.retrieveFile(cfg.getPath(),baos)) {
                return baos.toByteArray();
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
        throw new Exception("Fail to download file: "+cfg.getPath());
    }
}
