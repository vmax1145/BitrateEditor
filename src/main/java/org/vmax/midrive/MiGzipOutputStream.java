package org.vmax.midrive;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class MiGzipOutputStream extends GZIPOutputStream {
    public MiGzipOutputStream(OutputStream out, int size) throws IOException {
        super(out, size);
    }

    public MiGzipOutputStream(OutputStream out, int size, boolean syncFlush) throws IOException {
        super(out, size, syncFlush);
    }

    public MiGzipOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public MiGzipOutputStream(OutputStream out, boolean syncFlush) throws IOException {
        super(out, syncFlush);
    }


    private void writeHeader(String fileName) throws IOException {
        out.write(new byte[] {
                (byte) 0x1f,        // Magic number (short)
                (byte) 0x8b,        // Magic number (short)
                Deflater.DEFLATED,  // Compression method (CM)
                0x8,                // Flags (FLG) original filename present
                (byte) 0x5D,        // Modification time MTIME (int)
                (byte) 0x06,        // Modification time MTIME (int)
                (byte) 0xBC,               // Modification time MTIME (int)
                (byte) 0x5D,               // Modification time MTIME (int)
                0,                  // Extra flags (XFLG)
                0x3                 // Operating system (OS),
        });
        out.write(fileName.getBytes("ASCII"));
        out.write(0);
    }

}
