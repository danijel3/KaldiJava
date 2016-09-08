package pl.edu.pjwstk.kaldi.service.tasks;

import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.programs.KaldiKWS;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils;
import pl.edu.pjwstk.kaldi.programs.Transcriber;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Vector;

public class KeywordSpottingTask extends Task {

    private File input_keywords;
    private File words_table;

    @Override
    public void run() {

        state = State.RUNNING;

        try {
            File lattice = new File(Settings.curr_task_dir, "aligned_lattice");
            File lattice_int = new File(Settings.curr_task_dir, "kws_lattice.int");
            File lattice_txt = new File(Settings.curr_task_dir, "kws_lattice.txt");
            File kw_clean = new File(Settings.curr_task_dir, "kws_clean_words");
            File lat_vocab = new File(Settings.curr_task_dir, "kws_lat_vocab");
            File vocab = new File(Settings.curr_task_dir, "kws_vocab");
            File dict = new File(Settings.curr_task_dir, "kws_dict");
            File kws_out = new File(Settings.curr_task_dir, "kws.txt");
            File tg_out = new File(Settings.curr_task_dir, "out.TextGrid");

            if (!lattice.canRead()) {
                Log.error("Cannot read lattice for task: " + Settings.curr_task_dir);
                Log.error("Keyword spotting HAS to be run after decoding the file first!");
                state = State.FAILED;
                return;
            }

            cleanKeywords(input_keywords, kw_clean, Settings.default_encoding);

            KaldiUtils.lattice_copy("ark", lattice, "ark,t", lattice_int, true);

            KaldiUtils.int2sym("3", words_table, lattice_int, lattice_txt);

            KaldiKWS.get_vocab(lattice_txt, lat_vocab);

            FileUtils.mergeFiles(new File[]{lat_vocab, kw_clean}, vocab, Settings.default_encoding, true);

            FileUtils.sort_uniq(vocab, vocab, Settings.default_encoding);

            Transcriber.transcribe(vocab, Settings.default_encoding, dict, Settings.default_encoding, false);

            KaldiKWS.detect(lattice_txt, kw_clean, dict, kws_out);

            convertKWSToTG(kws_out, tg_out);

            state = State.SUCCEEDED;

        } catch (Exception e) {
            Log.error("KWS task.", e);
            state = State.FAILED;
        }

    }

    private static void cleanKeywords(File input, File output, String encoding) throws IOException {

        Vector<String> keywords = FileUtils.readLines(input, Settings.default_encoding);

        PrintWriter writer = new PrintWriter(output, encoding);

        for (String kw : keywords) {
            kw = kw.toLowerCase().trim();

            kw = kw.replaceAll("[-_,]", " ");

            kw = kw.replaceAll("[^\\s\\w\\.ĄĆĘŁŃÓŚŹŻąćęłńóśźż]", "");

            if (kw.isEmpty())
                continue;

            String[] kws = kw.split("\\s+");
            for (String w : kws)
                writer.println(w);
        }

        writer.close();

    }

    private static void convertKWSToTG(File kws, File tg) throws IOException {
        Vector<String> kw_lines = FileUtils.readLines(kws, Settings.default_encoding);

        TextGrid gridfile = new TextGrid();

        for (String line : kw_lines) {
            String[] ss = line.split("\\s+");
            double start = Double.parseDouble(ss[1]);
            double len = Double.parseDouble(ss[2]);
            gridfile.addSegment(0, start, start + len, ss[0] + " <" + ss[3] + ">");
        }

        gridfile.write(tg);
    }

    @Override
    public void loadSettings(XPath xpath, Element node) throws XPathExpressionException {
        input_keywords = new File((String) xpath.evaluate("input-keywords", node, XPathConstants.STRING));
        words_table = new File((String) xpath.evaluate("words-table", node, XPathConstants.STRING));
    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        processFileHash(m, input_keywords);
    }

}
