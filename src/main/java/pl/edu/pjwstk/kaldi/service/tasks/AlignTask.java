package pl.edu.pjwstk.kaldi.service.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.KaldiMain;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

public class AlignTask extends Task {

    private final static Logger logger = LoggerFactory.getLogger(AlignTask.class);

    private File input_audio;
    private File input_text;

    @Override
    public void run() {

        state = State.RUNNING;

        File clean_text = new File(Settings.curr_task_dir, "clean.txt");
        File textgrid = new File(Settings.curr_task_dir, "out.TextGrid");
        File labfile = new File(Settings.curr_task_dir, "out.lab");

        try {
            FileUtils.cleanChars(input_text, clean_text, false, true, Settings.default_encoding);

            KaldiMain.alignFile(input_audio, clean_text, textgrid, labfile);

            state = State.SUCCEEDED;

        } catch (IOException | UnsupportedAudioFileException e) {
            logger.error("Decoding task.", e);
            state = State.FAILED;
        }

    }

    @Override
    public void loadSettings(XPath xpath, Element node) throws XPathExpressionException {

        input_audio = new File((String) xpath.evaluate("input-audio", node, XPathConstants.STRING));
        input_text = new File((String) xpath.evaluate("input-text", node, XPathConstants.STRING));

    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        processFileHash(m, input_audio);
        processFileHash(m, input_text);

    }

}
