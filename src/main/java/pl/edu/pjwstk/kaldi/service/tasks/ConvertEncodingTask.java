package pl.edu.pjwstk.kaldi.service.tasks;

import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.programs.FFMPEG;
import pl.edu.pjwstk.kaldi.utils.Log;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

public class ConvertEncodingTask extends Task {

    private File input, output;

    @Override
    public void run() {

        state = State.RUNNING;

        try {

            FFMPEG.convertTo16k(input, output);

            state = State.SUCCEEDED;

        } catch (RuntimeException e) {
            Log.error("FFMPEG task.", e);
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
