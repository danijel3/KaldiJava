package pl.edu.pjwstk.kaldi.utils;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LogStream extends OutputStream {

    private Logger logger;
    private ByteArrayOutputStream buffer;
    private String prefix="";

    public LogStream(Logger logger) {
        this.logger = logger;
        buffer = new ByteArrayOutputStream();
    }

    public LogStream(Logger logger, String prefix) {
        this(logger);
        this.prefix=prefix;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\r') return;
        if (b == '\n') {
            logger.trace(prefix+buffer.toString());
            buffer.reset();
            return;
        }

        buffer.write(b);
    }
}
