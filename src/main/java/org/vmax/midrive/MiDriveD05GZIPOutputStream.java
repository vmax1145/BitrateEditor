package org.vmax.midrive;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * This class implements a stream filter for writing compressed data in
 * the GZIP file format.
 * @author      David Connelly
 *
 */
public
class MiDriveD05GZIPOutputStream extends DeflaterOutputStream {
    /**
     * CRC-32 of uncompressed data.
     */
    protected CRC32 crc = new CRC32();

    /*
     * GZIP header magic number.
     */
    private final static int GZIP_MAGIC = 0x8b1f;

    /*
     * Trailer size in bytes.
     *
     */
    private final static int TRAILER_SIZE = 8;

    public MiDriveD05GZIPOutputStream(OutputStream out, String fileName) throws IOException {
        super(out, new Deflater(Deflater.DEFAULT_COMPRESSION, true),4096,true);
        writeHeader(fileName);
        crc.reset();
    }

    /**
     * Writes array of bytes to the compressed output stream. This method
     * will block until all the bytes are written.
     * @param buf the data to be written
     * @param off the start offset of the data
     * @param len the length of the data
     * @exception IOException If an I/O error has occurred.
     */
    public synchronized void write(byte[] buf, int off, int len) throws IOException {
        super.write(buf, off, len);
        crc.update(buf, off, len);
    }

    /**
     * Finishes writing compressed data to the output stream without closing
     * the underlying stream. Use this method when applying multiple filters
     * in succession to the same output stream.
     * @exception IOException if an I/O error has occurred
     */
    public void finish() throws IOException {
        if (!def.finished()) {
            def.finish();
            while (!def.finished()) {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE) {
                    // last deflater buffer. Fit trailer at the end
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(buf, 0, len);
                    return;
                }
                if (len > 0)
                    out.write(buf, 0, len);
            }
            // if we can't fit the trailer at the end of the last
            // deflater buffer, we write it separately
            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer);
        }
    }

    private void writeHeader(String fileName) throws IOException {
        out.write(new byte[] {
                (byte) 0x1f,        // Magic number (short)
                (byte) 0x8b,        // Magic number (short)
                Deflater.DEFLATED,  // Compression method (CM)
                0x8,                // Flags (FLG) original filename present
                (byte) 0xAA,        // Modification time MTIME (int)
                (byte) 0x5E,        // Modification time MTIME (int)
                (byte) 0x5A,               // Modification time MTIME (int)
                (byte) 0x5D,               // Modification time MTIME (int)
                0,                  // Extra flags (XFLG)
                0x3                 // Operating system (OS),
        });
        out.write(fileName.getBytes("ASCII"));
        out.write(0);
    }
    /*
     * Writes GZIP member trailer to a byte array, starting at a given
     * offset.
     */
    private void writeTrailer(byte[] buf, int offset) throws IOException {
        writeInt((int)crc.getValue(), buf, offset); // CRC-32 of uncompr. data
        writeInt(def.getTotalIn(), buf, offset + 4); // Number of uncompr. bytes
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte)(s & 0xff);
        buf[offset + 1] = (byte)((s >> 8) & 0xff);
    }


    public void close() throws IOException {
        super.close();
        def.end();
    }
}
