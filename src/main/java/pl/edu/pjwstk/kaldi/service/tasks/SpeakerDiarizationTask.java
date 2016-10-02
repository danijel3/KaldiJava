package pl.edu.pjwstk.kaldi.service.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.programs.*;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;

public class SpeakerDiarizationTask extends Task {

    private final static Logger logger = LoggerFactory.getLogger(Julius.class);

    private enum Method {
        shout, lium
    }

    private File input_file;
    private Method method;

    private int max_clusters;

    @Override
    public void run() {

        state = State.RUNNING;

        if (!input_file.canRead()) {
            logger.error("File cannot be read: " + input_file.getAbsolutePath());
            state = State.FAILED;
            return;
        }

        try {

            switch (method) {
                case shout:
                    shout();
                    break;
                case lium:
                    lium();
                    break;
                default:
                    logger.info("Speaker diarization method not implemented!");
                    logger.info("Method: " + method.toString());
                    break;
            }

            state = State.SUCCEEDED;

        } catch (RuntimeException | FileNotFoundException e) {
            logger.error("Speaker diarization task.", e);
            state = State.FAILED;
        }
    }

    private void shout() throws RuntimeException, FileNotFoundException {

        File raw_file = new File(Settings.curr_task_dir, "file.raw");
        File seg_out = new File(Settings.curr_task_dir, "seg.out");
        File out_mo = new File(Settings.curr_task_dir, "out.mo");

        File seg_model = new File(Settings.shout_models, "shout.sad");

        if (!seg_model.canRead()) {
            throw new FileNotFoundException(seg_model.getAbsolutePath());
        }

        Sox.convert(input_file, raw_file);
        Shout.shout_segment(raw_file, seg_model, out_mo);
        Shout.shout_cluster(raw_file, seg_out, out_mo, max_clusters);

    }

    private void lium() throws RuntimeException, FileNotFoundException {

        Lium.test();

        Lium.diarize(input_file);
    }

    @Override
    public void loadSettings(XPath xpath, Element node)
            throws XPathExpressionException {

        input_file = new File((String) xpath.evaluate("input-file", node,
                XPathConstants.STRING));

        String max_clust_string = (String) xpath.evaluate("max-clusters", node,
                XPathConstants.STRING);

        try {
            max_clusters = Integer.parseInt(max_clust_string);
        } catch (NumberFormatException e) {
            throw new XPathExpressionException(e);
        }

        String str_method = (String) xpath.evaluate("method", node,
                XPathConstants.STRING);

        try {
            method = Method.valueOf(str_method);
        } catch (IllegalArgumentException e) {
            String methods = "";
            for (Method m : Method.values())
                methods += m.toString() + ",";

            throw new XPathExpressionException(
                    "Method type unknown! Available methods: " + methods);
        }
    }

    public static void main(String[] args) {
        try {

            Locale.setDefault(Locale.ENGLISH);

            KaldiUtils.init();
            KaldiUtils.test();
            KaldiScripts.init();
            KaldiScripts.test();
            Shout.test();

            SpeakerDiarizationTask task = new SpeakerDiarizationTask();

            Settings.curr_task_dir = new File(Settings.tasks_dir,
                    "SpeakerDiarizartionUnitTest");

            Settings.curr_task_dir.mkdirs();

            task.input_file = new File("/home/guest/Desktop/TEMP/speaker.wav");
            task.method = Method.lium;
            task.max_clusters = 5;

            task.run();

        } catch (Exception e) {
            logger.error("Error running task.", e);
        }
    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {

        String methodname = method.name();

        m.update(methodname.getBytes(Settings.default_encoding));
        processFileHash(m, input_file);

    }
}
