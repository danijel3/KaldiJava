package pl.edu.pjwstk.kaldi.service.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.programs.FFMPEG;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

public class ConvertEncodingTask extends Task {

    private final static Logger logger = LoggerFactory.getLogger(ConvertEncodingTask.class);

    private File input, output;

    @Override
    public void run() {

        state = State.RUNNING;

        try {

            FFMPEG.convertTo16k(input, output);

            state = State.SUCCEEDED;

        } catch (RuntimeException e) {
            logger.error("FFMPEG task.", e);
            state = State.FAILED;
        }
    }

    @Override
    public void loadSettings(XPath xpath, Element node)
            throws XPathExpressionException {

        input = new File((String) xpath.evaluate("input", node,
                XPathConstants.STRING));
        output = new File((String) xpath.evaluate("output", node,
                XPathConstants.STRING));

    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        processFileHash(m, input);
    }

}
