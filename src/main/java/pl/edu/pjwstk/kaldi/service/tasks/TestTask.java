package pl.edu.pjwstk.kaldi.service.tasks;

import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;

public class TestTask extends Task {

    private File input;
    private String output_name;

    @Override
    public void run() {

        state = State.RUNNING;
        Log.info("Starting test task...");

        try {

            File output = new File(Settings.curr_task_dir, output_name);

            Files.copy(input.toPath(), output.toPath());

        } catch (Exception e) {
            Log.error("Running Test Task", e);
            state = State.FAILED;
            return;
        }

        Log.info("Completed succesfully!");
        state = State.SUCCEEDED;
    }

    @Override
    public void loadSettings(XPath xpath, Element node)
            throws XPathExpressionException {

        String input_file = (String) xpath.evaluate("input", node,
                XPathConstants.STRING);
        input = new File(input_file);

        output_name = (String) xpath.evaluate("output-name", node,
                XPathConstants.STRING);
    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        processFileHash(m, input);
        m.digest(output_name.getBytes(Settings.default_encoding));
    }
}
