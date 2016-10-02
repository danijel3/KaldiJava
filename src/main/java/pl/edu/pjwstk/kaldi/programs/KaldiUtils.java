package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.LogStream;
import pl.edu.pjwstk.kaldi.utils.ProgramLauncher;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Vector;

public class KaldiUtils {

    private final static Logger logger = LoggerFactory.getLogger(KaldiUtils.class);
    private final static LogStream logger_stdout = new LogStream(logger);
    private final static LogStream logger_stderr = new LogStream(logger, "ERR>> ");

    private static File utils_dir;
    private static File openfst_dir;

    private static File compute_mfcc_feats;
    private static File compute_cmvn_stats;
    private static File splice_feats;
    private static File transform_feats;
    private static File apply_cmvn;
    private static File add_deltas;
    private static File gmm_decode_faster;
    private static File gmm_latgen_faster;
    private static File gmm_align;
    private static File copy_int_vector;
    private static File lattice_copy;
    private static File lattice_oracle;
    private static File lattice_align_words;
    private static File lattice_to_ctm_conf;
    private static File lattice_to_fst;
    private static File lattice_best_path;
    private static File lattice_1best;
    private static File nbest_to_ctm;
    private static File show_alignments;
    private static File lattice_to_phone_lattice;
    private static File linear_to_nbest;

    private static File lattice_to_post;
    private static File weight_silence_post;
    private static File gmm_post_to_gpost;
    private static File gmm_est_fmllr_gpost;
    private static File lattice_determinize_pruned;
    private static File gmm_est_fmllr;
    private static File compose_transforms;
    private static File gmm_rescore_lattice;

    private static File arpa2fst;
    private static File fstcompile;
    private static File fstarcsort;
    private static File fstrmepsilon;
    private static File fstrandgen;
    private static File fstprint;
    private static File fstdraw;
    private static File fstcompose;
    private static File fstaddselfloops;
    private static File fsttablecompose;
    private static File fstdeterminizestar;
    private static File fstminimizeencoded;
    private static File fstisstochastic;
    private static File fstcomposecontext;
    private static File make_h_transducer;
    private static File fstrmsymbols;
    private static File fstrmepslocal;
    private static File add_self_loops;
    private static File online_wav_gmm_decode_faster;

    private static File fst_lib;

    private static File eps2disambig;
    private static File s2eps;
    private static File int2sym;
    private static File sym2int;
    private static File add_lex_disambig;
    private static File make_lexicon_fst;

    private static File prepare_lang;

    private static ArrayList<File> fstlibs;

    public static void init() {
        utils_dir = new File(Settings.kaldi_root, "egs/wsj/s5/utils");
        openfst_dir = new File(Settings.kaldi_root, "tools/openfst-1.3.4");

        compute_mfcc_feats = new File(Settings.kaldi_root, "src/featbin/compute-mfcc-feats");
        compute_cmvn_stats = new File(Settings.kaldi_root, "src/featbin/compute-cmvn-stats");
        splice_feats = new File(Settings.kaldi_root, "src/featbin/splice-feats");
        transform_feats = new File(Settings.kaldi_root, "src/featbin/transform-feats");
        apply_cmvn = new File(Settings.kaldi_root, "src/featbin/apply-cmvn");
        add_deltas = new File(Settings.kaldi_root, "src/featbin/add-deltas");
        gmm_decode_faster = new File(Settings.kaldi_root, "src/gmmbin/gmm-decode-faster");
        gmm_latgen_faster = new File(Settings.kaldi_root, "src/gmmbin/gmm-latgen-faster");
        gmm_align = new File(Settings.kaldi_root, "src/gmmbin/gmm-align");
        copy_int_vector = new File(Settings.kaldi_root, "src/bin/copy-int-vector");
        lattice_copy = new File(Settings.kaldi_root, "src/latbin/lattice-copy");
        lattice_oracle = new File(Settings.kaldi_root, "src/latbin/lattice-oracle");
        lattice_align_words = new File(Settings.kaldi_root, "src/latbin/lattice-align-words");
        lattice_to_ctm_conf = new File(Settings.kaldi_root, "src/latbin/lattice-to-ctm-conf");
        lattice_to_fst = new File(Settings.kaldi_root, "src/latbin/lattice-to-fst");
        lattice_1best = new File(Settings.kaldi_root, "src/latbin/lattice-1best");
        lattice_best_path = new File(Settings.kaldi_root, "src/latbin/lattice-best-path");
        nbest_to_ctm = new File(Settings.kaldi_root, "src/latbin/nbest-to-ctm");
        show_alignments = new File(Settings.kaldi_root, "src/bin/show-alignments");
        lattice_to_phone_lattice = new File(Settings.kaldi_root, "src/latbin/lattice-to-phone-lattice");
        linear_to_nbest = new File(Settings.kaldi_root, "src/latbin/linear-to-nbest");
        lattice_to_post = new File(Settings.kaldi_root, "src/latbin/lattice-to-post");
        weight_silence_post = new File(Settings.kaldi_root, "src/bin/weight-silence-post");
        gmm_post_to_gpost = new File(Settings.kaldi_root, "src/gmmbin/gmm-post-to-gpost");
        gmm_est_fmllr_gpost = new File(Settings.kaldi_root, "src/gmmbin/gmm-est-fmllr-gpost");
        lattice_determinize_pruned = new File(Settings.kaldi_root, "src/latbin/lattice-determinize-pruned");
        gmm_est_fmllr = new File(Settings.kaldi_root, "src/gmmbin/gmm-est-fmllr");
        compose_transforms = new File(Settings.kaldi_root, "src/featbin/compose-transforms");
        gmm_rescore_lattice = new File(Settings.kaldi_root, "src/gmmbin/gmm-rescore-lattice");
        arpa2fst = new File(Settings.kaldi_root, "src/bin/arpa2fst");
        fstcompile = new File(openfst_dir, "bin/fstcompile");
        fstarcsort = new File(openfst_dir, "bin/fstarcsort");
        fstrmepsilon = new File(openfst_dir, "bin/fstrmepsilon");
        fstprint = new File(openfst_dir, "bin/fstprint");
        fstrandgen = new File(openfst_dir, "bin/fstrandgen");
        fstdraw = new File(openfst_dir, "bin/fstdraw");
        fstcompose = new File(openfst_dir, "bin/fstcompose");
        fstaddselfloops = new File(Settings.kaldi_root, "src/fstbin/fstaddselfloops");
        fsttablecompose = new File(Settings.kaldi_root, "src/fstbin/fsttablecompose");
        fstdeterminizestar = new File(Settings.kaldi_root, "src/fstbin/fstdeterminizestar");
        fstminimizeencoded = new File(Settings.kaldi_root, "src/fstbin/fstminimizeencoded");
        fstisstochastic = new File(Settings.kaldi_root, "src/fstbin/fstisstochastic");
        fstcomposecontext = new File(Settings.kaldi_root, "src/fstbin/fstcomposecontext");
        make_h_transducer = new File(Settings.kaldi_root, "src/bin/make-h-transducer");
        fstrmsymbols = new File(Settings.kaldi_root, "src/fstbin/fstrmsymbols");
        fstrmepslocal = new File(Settings.kaldi_root, "src/fstbin/fstrmepslocal");
        add_self_loops = new File(Settings.kaldi_root, "src/bin/add-self-loops");
        online_wav_gmm_decode_faster = new File(Settings.kaldi_root, "src/onlinebin/online-wav-gmm-decode-faster");

        fst_lib = new File(openfst_dir, "lib");

        eps2disambig = new File(utils_dir, "eps2disambig.pl");
        s2eps = new File(utils_dir, "s2eps.pl");
        int2sym = new File(utils_dir, "int2sym.pl");
        sym2int = new File(utils_dir, "sym2int.pl");
        make_lexicon_fst = new File(utils_dir, "make_lexicon_fst.pl");
        add_lex_disambig = new File(utils_dir, "add_lex_disambig.pl");

        prepare_lang = new File(utils_dir, "prepare_lang.sh");

        fstlibs = new ArrayList<>();
        fstlibs.add(fst_lib);
    }

    public static void test() throws FileNotFoundException {

        Field fields[] = KaldiUtils.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().getName().equals("java.io.File")) {

                try {

                    File file = (File) field.get(null);

                    if (file == null || !file.exists())
                        throw new FileNotFoundException("" + file);

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.error("Internal error", e);
                }
            }
        }

        for (File file : fstlibs) {
            if (!file.exists())
                throw new FileNotFoundException(file.getAbsolutePath());
        }

    }

    public static void generateWordsMapping(File vocab, String vocab_enc, File mapping, String mapping_enc)
            throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(vocab), vocab_enc));
        PrintWriter writer = new PrintWriter(mapping, mapping_enc);

        writer.println("<eps> 0");
        writer.println("!SIL 1");
        writer.println("SIL 2");
        writer.println("<UNK> 3");

        int i = 4;
        String line;
        while ((line = reader.readLine()) != null) {

            if (line.equals("<s>"))
                continue;
            if (line.equals("</s>"))
                continue;

            writer.println(line + " " + i);
            i++;
        }

        writer.println("#0 " + i);

        reader.close();
        writer.close();

    }

    public static void compute_mfcc_feats(File config, File scp, File out) {

        String[] cmd = new String[]{compute_mfcc_feats.getAbsolutePath(), "--config=" + config.getAbsolutePath(),
                "scp:" + scp.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("compute_mfcc_feats: " + scp.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void compute_cmvn_stats(File in, File out) {

        String[] cmd = new String[]{compute_cmvn_stats.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("compute_cmvn_stats: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void apply_cmvn(File stats, File in, File out) {

        String[] cmd = new String[]{apply_cmvn.getAbsolutePath(), "ark:" + stats.getAbsolutePath(),
                "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("apply_cmvn: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void add_deltas(File in, File out) {

        String[] cmd = new String[]{add_deltas.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("add_deltas: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void splice_feats(File in, File out) {

        String[] cmd = new String[]{splice_feats.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("splice_feats: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void transform_feats(File trans, boolean trans_ark, File in, File out) {

        String trans_ark_str = "";
        if (trans_ark)
            trans_ark_str = "ark:";

        String[] cmd = new String[]{transform_feats.getAbsolutePath(), trans_ark_str + trans.getAbsolutePath(),
                "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("transform_feats: " + in.getName() + "->{" + trans.getName() + "}->" + out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void gmm_decode_faster(File word_symbol_table, File model, File fst, File feat, File out)
            throws RuntimeException {

        String[] cmd = new String[]{gmm_decode_faster.getAbsolutePath(),
                "--word-symbol-table=" + word_symbol_table.getAbsolutePath(), model.getAbsolutePath(),
                fst.getAbsolutePath(), "ark:" + feat.getAbsolutePath(), "ark:" + out};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_decode_faster: " + feat.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());

    }

    public static void gmm_latgen_faster(File model, File fst, File feat, File lattice, File words, File alignment)
            throws RuntimeException {

        /*String[] cmd = new String[]{gmm_latgen_faster.getAbsolutePath(), model.getAbsolutePath(),
                fst.getAbsolutePath(), "ark:" + feat.getAbsolutePath(), "ark:" + lattice.getAbsolutePath(),
                "ark:" + words.getAbsolutePath(), "ark:" + alignment.getAbsolutePath()};*/

        String[] cmd = new String[]{gmm_latgen_faster.getAbsolutePath(),
                /*"--verbose=100", "--max-active=7000", "--beam=64.0", "--lattice-beam=36.0", "--acoustic-scale=0.083333",*/
                //"--allow-partial=true",
                //"--determinize-lattice=false",
                model.getAbsolutePath(), fst.getAbsolutePath(), "ark:" + feat.getAbsolutePath(), "ark:" + lattice.getAbsolutePath(),
                "ark:" + words.getAbsolutePath(), "ark:" + alignment.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_latgen_faster: " + feat.getName() + "->" + lattice.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());

    }

    public static void gmm_align(double beam, double retry_beam, File tree, File model, File lexicon, File feat,
                                 File transcription, File alignment) throws RuntimeException {

        String[] cmd = new String[]{gmm_align.getAbsolutePath(), "--beam=" + beam, "--retry-beam=" + retry_beam,
                tree.getAbsolutePath(), model.getAbsolutePath(), lexicon.getAbsolutePath(),
                "ark:" + feat.getAbsolutePath(), "ark:" + transcription.getAbsolutePath(),
                "ark:" + alignment.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_align: " + feat.getName() + "->" + alignment.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());

    }

    public static void copy_int_vector(String in_type, File in, String out_type, File out) {

        String[] cmd = new String[]{copy_int_vector.getAbsolutePath(), in_type + ":" + in.getAbsolutePath(),
                out_type + ":" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("copy_int_vector: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_1best(File in, File out) {

        String[] cmd = new String[]{lattice_1best.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_1best: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_copy(String input_opts, File input_lattice, String output_opts, File output_lattice,
                                    boolean write_compact) {

        String wc_str = "false";
        if (write_compact)
            wc_str = "true";

        String[] cmd = new String[]{lattice_copy.getAbsolutePath(), "--write-compact=" + wc_str,
                input_opts + ":" + input_lattice.getAbsolutePath(),
                output_opts + ":" + output_lattice.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_copy: " + input_opts + ":" + input_lattice.getAbsolutePath() + " -> " + output_opts + ":"
                + output_lattice.getAbsolutePath());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_oracle(File lattice, File input_txt, File output_lattice, File word_symbol_table) {

        String[] cmd = new String[]{lattice_oracle.getAbsolutePath(),
                "--write-lattices=ark:" + output_lattice.getAbsolutePath(),
                "--word-symbol-table=" + word_symbol_table.getAbsolutePath(), "ark:" + lattice.getAbsolutePath(),
                "ark:" + input_txt.getAbsolutePath(), "ark,t:-"};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_oracle: " + lattice.getName() + "->" + output_lattice.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_align_words(File word_boundaries, File model, File in, File out) {

        String[] cmd = new String[]{lattice_align_words.getAbsolutePath(), word_boundaries.getAbsolutePath(),
                model.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_align_words: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void nbest_to_ctm(File in, File out) {

        String[] cmd = new String[]{nbest_to_ctm.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("nbest_to_ctm: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_to_ctm_conf(File in, File out) {

        String[] cmd = new String[]{lattice_to_ctm_conf.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_to_ctm_conf: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_to_fst(File in, File out_txt) {

        String[] cmd = new String[]{lattice_to_fst.getAbsolutePath(), "ark:" + in.getAbsolutePath(),
                "ark,t:" + out_txt.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_to_fst: " + in.getName() + "->" + out_txt.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void show_alignments(File phone_sym_table, File model, File alignment, File out)
            throws FileNotFoundException {

        String[] cmd = new String[]{show_alignments.getAbsolutePath(), phone_sym_table.getAbsolutePath(),
                model.getAbsolutePath(), "ark:" + alignment.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutFile(out);

        logger.trace("show_alignments: " + alignment.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_to_phone_lattice(File model, File in, File out) {

        String[] cmd = new String[]{lattice_to_phone_lattice.getAbsolutePath(), model.getAbsolutePath(),
                "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_to_phone_lattice: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void linear_to_nbest(File alignment, File transcription, File nbest) {

        String[] cmd = new String[]{linear_to_nbest.getAbsolutePath(), "ark:" + alignment.getAbsolutePath(),
                "ark:" + transcription.getAbsolutePath(), "", "", "ark:" + nbest.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("linear_to_nbest: " + alignment.getName() + "->" + nbest.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void lattice_best_path(File lat, File tra, File ali) {

        String[] cmd = new String[]{lattice_best_path.getAbsolutePath(), "ark:" + lat.getAbsolutePath(),
                "ark:" + tra.getAbsolutePath(), "ark:" + ali.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_best_path: " + lat.getName() + "->" + tra.getName());

        launcher.run();
        logger.trace("Done.");
    }

    public static void lattice_to_post(double acoustic_scale, File in, File out) throws RuntimeException {

        String[] cmd = new String[]{lattice_to_post.getAbsolutePath(), "--acoustic-scale=" + acoustic_scale,
                "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_to_post: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void weight_silence_post(double silence_weight, String silence_list, File mdl, File in, File out)
            throws RuntimeException {

        String[] cmd = new String[]{weight_silence_post.getAbsolutePath(), "" + silence_weight, silence_list,
                mdl.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("weight_silence_post: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void gmm_post_to_gpost(File model, File feat, File in, File out) throws RuntimeException {

        String[] cmd = new String[]{gmm_post_to_gpost.getAbsolutePath(), model.getAbsolutePath(),
                "ark:" + feat.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_post_to_gpost: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public enum FMLLRUpdateType {
        full, diag, offset, none
    }

    public static void gmm_est_fmllr_gpost(FMLLRUpdateType update_type, File spk2utt, File model, File feat, File in,
                                           File out) throws RuntimeException {

        String updTypStr = "--fmllr-update-type=";

        switch (update_type) {
            case diag:
                updTypStr += "diag";
                break;
            default:
            case full:
                updTypStr += "full";
                break;
            case none:
                updTypStr += "none";
                break;
            case offset:
                updTypStr += "offset";
                break;
        }

        String[] cmd = new String[]{gmm_est_fmllr_gpost.getAbsolutePath(), updTypStr, model.getAbsolutePath(),
                "ark:" + feat.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        if (spk2utt != null) {
            cmd = new String[]{gmm_est_fmllr_gpost.getAbsolutePath(), updTypStr,
                    "--spk2utt=ark:" + spk2utt.getAbsolutePath(), model.getAbsolutePath(),
                    "ark:" + feat.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};
        }

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_est_fmllr_gpost: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void lattice_determinize_pruned(double acoustic_scale, double beam, File in, File out)
            throws RuntimeException {

        String[] cmd = new String[]{lattice_determinize_pruned.getAbsolutePath(),
                "--acoustic-scale=" + acoustic_scale, "--beam=" + beam, "ark:" + in.getAbsolutePath(),
                "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("lattice_determinize_pruned: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void gmm_est_fmllr(FMLLRUpdateType update_type, File spk2utt, File model, File feat, File in,
                                     File out) throws RuntimeException {

        String updTypStr = "--fmllr-update-type=";

        switch (update_type) {
            case diag:
                updTypStr += "diag";
                break;
            default:
            case full:
                updTypStr += "full";
                break;
            case none:
                updTypStr += "none";
                break;
            case offset:
                updTypStr += "offset";
                break;
        }

        String[] cmd = new String[]{gmm_est_fmllr.getAbsolutePath(), updTypStr, model.getAbsolutePath(),
                "ark:" + feat.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        if (spk2utt != null) {
            cmd = new String[]{gmm_est_fmllr.getAbsolutePath(), updTypStr,
                    "--spk2utt=ark:" + spk2utt.getAbsolutePath(), model.getAbsolutePath(),
                    "ark:" + feat.getAbsolutePath(), "ark:" + in.getAbsolutePath(), "ark:" + out.getAbsolutePath()};
        }

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_est_fmllr: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void compose_transforms(boolean bIsAffine, File in_a, File in_b, File out) throws RuntimeException {

        String bIsAffineStr = "--b-is-affine=";
        if (bIsAffine)
            bIsAffineStr += "true";
        else
            bIsAffineStr += "false";

        String[] cmd = new String[]{compose_transforms.getAbsolutePath(), bIsAffineStr,
                "ark:" + in_a.getAbsolutePath(), "ark:" + in_b.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("compose_transforms: " + in_a.getName() + "+" + in_b.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void gmm_rescore_lattice(File model, File in, File feat, File out) throws RuntimeException {

        String[] cmd = new String[]{gmm_rescore_lattice.getAbsolutePath(), model.getAbsolutePath(),
                "ark:" + in.getAbsolutePath(), "ark:" + feat.getAbsolutePath(), "ark:" + out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("gmm_rescore_lattice: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());
    }

    public static void arpa2fst(File arpa, File fst) {

        String[] cmd = new String[]{arpa2fst.getAbsolutePath(), arpa.getAbsolutePath(), fst.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("arpa2fst: " + arpa.getName() + "->" + fst.getName());
        launcher.run();
        logger.trace("Done.");

    }

    public static void fstprint(File fst, File txt) {

        String[] cmd = new String[]{fstprint.getAbsolutePath(), fst.getAbsolutePath(), txt.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstprint: " + fst.getName() + "->" + txt.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstprint(File fst, File txt, File input_symbols, File output_symbols) {

        String[] cmd = new String[]{fstprint.getAbsolutePath(),
                "--isymbols=" + input_symbols.getAbsolutePath(), "--osymbols=" + output_symbols.getAbsolutePath(),
                fst.getAbsolutePath(), txt.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstprint: " + fst.getName() + "->" + txt.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstrandgen(File fst, File rand, int seed) {

        String[] cmd = new String[]{fstrandgen.getAbsolutePath(), "--seed=" + seed, fst.getAbsolutePath(), rand.getAbsolutePath(),};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstrandgen: " + fst.getName() + "->" + rand.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstdraw(File fst, File dot, File isyms, File osyms, boolean acceptor) {

        String[] cmd = new String[]{fstdraw.getAbsolutePath(),
                "--isymbols=" + isyms.getAbsolutePath(), "--osymbols=" + osyms.getAbsolutePath(),
                fst.getAbsolutePath(), dot.getAbsolutePath()};

        if (acceptor) {
            cmd = new String[]{fstdraw.getAbsolutePath(), "--acceptor",
                    "--isymbols=" + isyms.getAbsolutePath(), "--osymbols=" + osyms.getAbsolutePath(),
                    fst.getAbsolutePath(), dot.getAbsolutePath()};
        }

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstdraw: " + fst.getName() + "->" + dot.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstcompose(File fsta, File fstb, File fstout) {

        String[] cmd = new String[]{fstcompose.getAbsolutePath(),
                fsta.getAbsolutePath(), fstb.getAbsolutePath(), fstout.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstcompose: " + fsta.getName() + " + " + fstb.getAbsolutePath() + "->" + fstout.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void eps2disambig(File fst_in, File fst_out) throws FileNotFoundException {
        String[] cmd = new String[]{Settings.perl_bin.getAbsolutePath(), eps2disambig.getAbsolutePath(),
                fst_in.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutFile(fst_out);

        logger.trace("eps2disambig: " + fst_in.getName() + "->" + fst_out.getName());
        logger.trace("<SUPRESSING OUTPUT>");
        launcher.run();
        logger.trace("Done.");

    }

    public static void s2eps(File fst_in, File fst_out) throws FileNotFoundException {
        String[] cmd = new String[]{Settings.perl_bin.getAbsolutePath(), s2eps.getAbsolutePath(),
                fst_in.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutFile(fst_out);

        logger.trace("s2eps: " + fst_in.getName() + "->" + fst_out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void int2sym(String field, File string_table, File in, File out) throws FileNotFoundException {
        String[] cmd = new String[]{Settings.perl_bin.getAbsolutePath(), int2sym.getAbsolutePath(), "-f", field,
                string_table.getAbsolutePath(), in.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutFile(out);

        logger.trace("int2sym: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void sym2int(String field, File string_table, File in, File out) throws FileNotFoundException {
        String[] cmd = new String[]{Settings.perl_bin.getAbsolutePath(), sym2int.getAbsolutePath(), "-f", field, "--map-oov", "<UNK>",
                string_table.getAbsolutePath(), in.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutFile(out);
        launcher.setStderrStream(logger_stderr);

        logger.trace("sym2int: " + in.getName() + "->" + out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstcompile(File isymbols, File osymbols, boolean keep_isymbols, boolean keep_osymbols,
                                  File fst_in, File fst_out) {

        String keep_isymbols_str = "true";
        if (!keep_isymbols)
            keep_isymbols_str = "false";
        String keep_osymbols_str = "true";
        if (!keep_osymbols)
            keep_osymbols_str = "false";

        String[] cmd = new String[]{fstcompile.getAbsolutePath(), "--isymbols=" + isymbols.getAbsolutePath(),
                "--osymbols=" + osymbols.getAbsolutePath(), "--keep_isymbols=" + keep_isymbols_str,
                "--keep_osymbols=" + keep_osymbols_str, fst_in.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstcompile: " + fst_in.getName() + "->" + fst_out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstarcsort(String sort_type, File fst_in, File fst_out) {

        String[] cmd = new String[]{fstarcsort.getAbsolutePath(), "--sort_type=" + sort_type, fst_in.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstarcsort: " + fst_in.getName() + "->" + fst_out.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstaddselfloops(File in_disambig_list, File out_disambig_list, File fst_in, File fst_out) {

        String[] cmd = new String[]{fstaddselfloops.getAbsolutePath(), in_disambig_list.getAbsolutePath(), out_disambig_list.getAbsolutePath(),
                fst_in.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstaddselfloops: " + fst_in.getName() + "->" + fst_out.getName());
        launcher.run();
        logger.trace("Done.");
    }


    public static void prepareLexicon(File dict_dir, File lang_dir) {

        String[] cmd = new String[]{Settings.bash_bin.getAbsolutePath(), prepare_lang.getAbsolutePath(),
                dict_dir.getAbsolutePath(), "<UNK>", lang_dir.getAbsolutePath(), dict_dir.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setCwd(utils_dir.getParentFile());
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("prepareLexicon...");
        launcher.run();
        logger.trace("Done.");
    }

    public static int add_lex_disambig(boolean sil_probs, boolean pron_probs, File lex_in, File lex_out) {
        Vector<String> cmd_vec = new Vector<>();
        cmd_vec.add(Settings.perl_bin.getAbsolutePath());
        cmd_vec.add(add_lex_disambig.getAbsolutePath());
        if (sil_probs)
            cmd_vec.add("--sil-probs");
        if (pron_probs)
            cmd_vec.add("--pron-probs");
        cmd_vec.add(lex_in.getAbsolutePath());
        cmd_vec.add(lex_out.getAbsolutePath());

        String[] cmd = cmd_vec.toArray(new String[]{});

        ByteArrayOutputStream ostr = new ByteArrayOutputStream();

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutStream(ostr);
        launcher.setStderrStream(logger_stderr);

        logger.trace("add_lex_disambig: " + lex_in.getName() + " -> " + lex_out.getName());
        launcher.run();
        logger.trace("Done.");

        String ret = new String(ostr.toByteArray()).trim();
        return Integer.parseInt(ret);
    }

    public static void make_lexicon_fst(boolean pron_prob, File lex, double silprob, String silphone, int ndisambig, File fst) throws FileNotFoundException {
        Vector<String> cmd_vec = new Vector<>();
        cmd_vec.add(Settings.perl_bin.getAbsolutePath());
        cmd_vec.add(make_lexicon_fst.getAbsolutePath());
        if (pron_prob)
            cmd_vec.add("--pron-probs");
        cmd_vec.add(lex.getAbsolutePath());
        cmd_vec.add("" + silprob);
        cmd_vec.add(silphone);
        if (ndisambig > 0)
            cmd_vec.add("#" + ndisambig);

        String[] cmd = cmd_vec.toArray(new String[]{});

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setStdoutFile(fst);
        launcher.setStderrStream(logger_stderr);

        logger.trace("make_lexicon_fst: " + lex.getName() + " -> " + fst.getName());
        launcher.run();
        logger.trace("Done.");
    }

    public static void fsttablecompose(File fst_one, File fst_two, File fst_out) {
        String[] cmd = new String[]{fsttablecompose.getAbsolutePath(), fst_one.getAbsolutePath(),
                fst_two.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace(
                "fsttablecompose: " + fst_one.getName() + "+" + fst_two.getName() + "=" + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstdeterminizestar(File fst_in, File fst_out, boolean use_log) {

        String strUseLog = "true";
        if (!use_log)
            strUseLog = "false";

        String[] cmd = new String[]{fstdeterminizestar.getAbsolutePath(), "--use-log=" + strUseLog,
                fst_in.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstdeterminizestar: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstminimizeencoded(File fst_in, File fst_out) {
        String[] cmd = new String[]{fstminimizeencoded.getAbsolutePath(), fst_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstminimizeencoded: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static boolean fstisstochastic(File fst) {

        String[] cmd = new String[]{fstisstochastic.getAbsolutePath(), fst.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstisstochastic: " + fst.getName() + "...");
        launcher.run();
        logger.trace("Done.");

        return launcher.getReturnValue() == 0;
    }

    public static void fstcomposecontext(int context_size, int central_position, File disamb_syms_in,
                                         File disamb_syms_out, File ilabels_out, File fst_in, File fst_out) {

        String[] cmd = new String[]{fstcomposecontext.getAbsolutePath(), "--context-size=" + context_size,
                "--central-position=" + central_position, "--read-disambig-syms=" + disamb_syms_in.getAbsolutePath(),
                "--write-disambig-syms=" + disamb_syms_out.getAbsolutePath(), ilabels_out.getAbsolutePath(),
                fst_in.getAbsolutePath(), fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstcomposecontext: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void make_h_transducer(File disamb_syms_out, float trans_scale, File ilabels, File tree, File mdl_in,
                                         File fst_out) {

        String[] cmd = new String[]{make_h_transducer.getAbsolutePath(),
                "--disambig-syms-out=" + disamb_syms_out.getAbsolutePath(), "--transition-scale=" + trans_scale,
                ilabels.getAbsolutePath(), tree.getAbsolutePath(), mdl_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("make_h_transducer: " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstrmsymbols(File syms, File fst_in, File fst_out) {

        String[] cmd = new String[]{fstrmsymbols.getAbsolutePath(), syms.getAbsolutePath(), fst_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstrmsymbols: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstrmepslocal(File fst_in, File fst_out) {

        String[] cmd = new String[]{fstrmepslocal.getAbsolutePath(), fst_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstrmsymbols: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void fstrmepsilon(File fst_in, File fst_out) {

        String[] cmd = new String[]{fstrmepsilon.getAbsolutePath(), fst_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("fstrmepsilon: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");
    }

    public static void add_self_loops(float loop_scale, boolean reorder, File mdl_in, File fst_in, File fst_out)
            throws RuntimeException {

        String strReorder = "true";
        if (!reorder)
            strReorder = "false";

        String[] cmd = new String[]{add_self_loops.getAbsolutePath(), "--self-loop-scale=" + loop_scale,
                "--reorder=" + strReorder, mdl_in.getAbsolutePath(), fst_in.getAbsolutePath(),
                fst_out.getAbsolutePath()};

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("add_self_loops: " + fst_in.getName() + " -> " + fst_out.getName() + "...");
        launcher.run();
        logger.trace("Done.");

        if (launcher.getReturnValue() != 0)
            throw new RuntimeException("Retval: " + launcher.getReturnValue());

    }

    public static void online_wav_gmm_decode_faster(float rt_min, float rt_max, int max_active, float beam,
                                                    float acoustic_scale, File wav_scp, File mdl_file, File HCLG_fst, File words_list, File trans, File ali,
                                                    File lda_matrix) {

        String[] cmd;

        if (lda_matrix != null) {
            cmd = new String[]{online_wav_gmm_decode_faster.getAbsolutePath(), "--verbose=1", "--rt-min=" + rt_min,
                    "--rt-max=" + rt_max, "--max-active=" + max_active, "--beam=" + beam,
                    "--acoustic-scale=" + acoustic_scale, "scp:" + wav_scp.getAbsolutePath(),
                    mdl_file.getAbsolutePath(), HCLG_fst.getAbsolutePath(), words_list.getAbsolutePath(), "1:2:3:4:5",
                    "ark,t:" + trans.getAbsolutePath(), "ark,t:" + ali.getAbsolutePath(),
                    lda_matrix.getAbsolutePath()};
        } else {
            cmd = new String[]{online_wav_gmm_decode_faster.getAbsolutePath(), "--verbose=1", "--rt-min=" + rt_min,
                    "--rt-max=" + rt_max, "--max-active=" + max_active, "--beam=" + beam,
                    "--acoustic-scale=" + acoustic_scale, "scp:" + wav_scp.getAbsolutePath(),
                    mdl_file.getAbsolutePath(), HCLG_fst.getAbsolutePath(), words_list.getAbsolutePath(), "1:2:3:4:5",
                    "ark,t:" + trans.getAbsolutePath(), "ark,t:" + ali.getAbsolutePath()};
        }

        ProgramLauncher launcher = new ProgramLauncher(cmd);
        launcher.setLibraries(fstlibs);
        launcher.setStdoutStream(logger_stdout);
        launcher.setStderrStream(logger_stderr);

        logger.trace("online_wav_gmm_decode_faster...");
        launcher.run();
        logger.trace("Done.");
    }
}
