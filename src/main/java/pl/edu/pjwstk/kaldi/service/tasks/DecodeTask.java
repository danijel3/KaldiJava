package pl.edu.pjwstk.kaldi.service.tasks;

import org.w3c.dom.Element;
import pl.edu.pjwstk.kaldi.files.CTM;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

public class DecodeTask extends Task {

    private File input_file;
    private File mfcc_config;
    private File mdl_file;
    private File hclg_file;
    private File words_table;
    private File phones_table;
    private File word_boundaries;

    private File lda_matrix = null;

    @Override
    public void run() {

        state = State.RUNNING;

        boolean fail = false;
        File files[] = {input_file, mfcc_config, mdl_file, hclg_file, words_table, phones_table, word_boundaries,
                lda_matrix};
        for (File f : files)
            if (f != null && !f.exists()) {
                Log.error("Missing file: " + f.getAbsolutePath());
                fail = true;
            }

        if (fail) {
            Log.error("Some files are missing!");
            state = State.FAILED;
            return;
        }

        File scp_file = new File(Settings.curr_task_dir, "wav.scp");
        File mfcc = new File(Settings.curr_task_dir, "mfcc");
        File cmvn_stats = new File(Settings.curr_task_dir, "cmvn_stats");
        File cmvn = new File(Settings.curr_task_dir, "cmvn");
        File deltas = new File(Settings.curr_task_dir, "deltas");
        File splice = new File(Settings.curr_task_dir, "splice");
        File trans = new File(Settings.curr_task_dir, "trans");
        File lattice = new File(Settings.curr_task_dir, "lattice");
        File words = new File(Settings.curr_task_dir, "words");
        File alignment = new File(Settings.curr_task_dir, "alignment");
        File words_int = new File(Settings.curr_task_dir, "words.int");
        File words_txt = new File(Settings.curr_task_dir, "words.txt");
        File aligned_lattice = new File(Settings.curr_task_dir, "aligned_lattice");
        File ctm_int = new File(Settings.curr_task_dir, "ctm.int");
        File ctm_txt = new File(Settings.curr_task_dir, "ctm.txt");
        File tg_out = new File(Settings.curr_task_dir, "out.TextGrid");

        try {

            FileUtils.makeSCPFile(scp_file, new File[]{input_file}, true);

            KaldiUtils.compute_mfcc_feats(mfcc_config, scp_file, mfcc);

            KaldiUtils.compute_cmvn_stats(mfcc, cmvn_stats);

            KaldiUtils.apply_cmvn(cmvn_stats, mfcc, cmvn);

            File data;

            if (lda_matrix != null) {

                KaldiUtils.splice_feats(cmvn, splice);

                KaldiUtils.transform_feats(lda_matrix, false, splice, trans);

                data = trans;

            } else {

                KaldiUtils.add_deltas(cmvn, deltas);

                data = deltas;
            }

            KaldiUtils.gmm_latgen_faster(mdl_file, hclg_file, data, lattice, words, alignment);

            KaldiUtils.copy_int_vector("ark", words, "ark,t", words_int);

            KaldiUtils.int2sym("2-", words_table, words_int, words_txt);

            KaldiUtils.lattice_align_words(word_boundaries, mdl_file, lattice, aligned_lattice);

            KaldiUtils.lattice_to_ctm_conf(aligned_lattice, ctm_int);

            KaldiUtils.int2sym("5", words_table, ctm_int, ctm_txt);

            CTM ctm = new CTM();

            ctm.read(ctm_txt);

            TextGrid tg = new TextGrid(ctm);

            tg.write(tg_out);

            state = State.SUCCEEDED;

        } catch (Exception e) {
            Log.error("Decoding task.", e);
            state = State.FAILED;
        }
    }

    @Override
    public void loadSettings(XPath xpath, Element node) throws XPathExpressionException {

        input_file = new File((String) xpath.evaluate("input-file", node, XPathConstants.STRING));
        mfcc_config = new File((String) xpath.evaluate("mfcc-config", node, XPathConstants.STRING));
        mdl_file = new File((String) xpath.evaluate("mdl", node, XPathConstants.STRING));
        hclg_file = new File((String) xpath.evaluate("hclg", node, XPathConstants.STRING));
        words_table = new File((String) xpath.evaluate("words-table", node, XPathConstants.STRING));
        phones_table = new File((String) xpath.evaluate("phones-table", node, XPathConstants.STRING));
        word_boundaries = new File((String) xpath.evaluate("word-boundaries", node, XPathConstants.STRING));

        String str = (String) xpath.evaluate("lda-matrix", node, XPathConstants.STRING);
        if (str != null && !str.isEmpty())
            lda_matrix = new File(str);

    }

    @Override
    public void updateHash(MessageDigest m) throws IOException {
        processFileHash(m, input_file);
    }

}
