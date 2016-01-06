package net.umpay.mailbill.util.security;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class IoUtils {

    public static void close(OutputStream out) {
        if (out != null) {
            try {
                out.flush();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return;
    }

    public static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return;
    }

    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
        return;
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static final boolean mkdirs(String dir) {
        File f = new File(dir);
        return f.mkdirs();
    }
}
