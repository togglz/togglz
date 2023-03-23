package org.togglz.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;

import static org.togglz.core.util.Preconditions.checkNotNull;

/**
 * Provides utility methods for working with I/O streams.
 * Based on com.google.common.io.* from com.google.guava:17.0
 *
 * @author Chris Nokleberg
 * @author Colin Decker
 * @author Bin Zhu
 */
public class IOUtils {

    private static final int BUF_SIZE = 4_096;

    public static void close(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Copies all bytes from the input stream to the output stream.
     * Does not close or flush either stream.
     *
     * @param from the input stream to read from
     * @param to   the output stream to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(InputStream from, OutputStream to) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    /**
     * Reads all characters from a {@link Readable} object into a {@link String}.
     * Does not close the {@code Readable}.
     *
     * @param r the object to read from
     * @return a string containing all the characters
     * @throws IOException if an I/O error occurs
     */
    public static String toString(Readable r) throws IOException {
        StringBuilder sb = new StringBuilder();
        copy(r, sb);
        return sb.toString();
    }

    /**
     * Copies all characters between the {@link Readable} and {@link Appendable}
     * objects. Does not close or flush either object.
     *
     * @param from the object to read from
     * @param to   the object to write to
     * @return the number of characters copied
     * @throws IOException if an I/O error occurs
     */
    public static long copy(Readable from, Appendable to) throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        CharBuffer buf = CharBuffer.allocate(BUF_SIZE);
        long total = 0;
        while (from.read(buf) != -1) {
            buf.flip();
            to.append(buf);
            total += buf.remaining();
            buf.clear();
        }
        return total;
    }
}
