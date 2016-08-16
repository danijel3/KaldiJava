package pl.edu.pjwstk.kaldi.programs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.Vector;

import pl.edu.pjwstk.kaldi.files.CTM;
import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.SegmentationList;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class KaldiScripts {

    private static boolean removeTempFiles = Settings.removeTempFile;

    public static File model_dir = new File("model");
    public static File temp_dir = new File("tmp");
    public static File lang_dir = new File(temp_dir, "lang");
    public static File feat_dir = new File(temp_dir, "feat");

    private static File distr_mdl;
    private static File distr_tree;
    private static File distr_L_fst;
    private static File distr_HCLG;
    private static File distr_lda;
    private static File distr_mfcc_config;
    private static File distr_words_txt;
    private static File distr_phones_txt;
    private static File distr_word_boundaries;
    private static File distr_silence_phones;
    private static File distr_nonsilence_phones;

    private static File distrFiles[];

    public static void init() throws IOException {
        init(null);
    }

    public static void init(File change_dir) throws IOException {

        removeTempFiles = Settings.removeTempFile;

        if (change_dir != null) {
            model_dir = new File(change_dir, "model");
            temp_dir = new File(change_dir, "tmp");
            feat_dir = new File(temp_dir, "feat");
            lang_dir = new File(temp_dir, "lang");
        }

        temp_dir.mkdirs();
        FileUtils.cleanup(temp_dir, new File[]{}, 5);
        lang_dir.mkdirs();
        feat_dir.mkdirs();

        distr_mdl = new File(model_dir, "final.mdl");
        distr_tree = new File(model_dir, "tree");
        distr_HCLG = new File(model_dir, "HCLG.fst");
        distr_L_fst = new File(model_dir, "L.fst");
        distr_mfcc_config = new File(model_dir, "mfcc.conf");
        distr_words_txt = new File(model_dir, "words.txt");
        distr_phones_txt = new File(model_dir, "phones.txt");
        distr_word_boundaries = new File(model_dir, "word_boundary.int");
        distr_silence_phones = new File(model_dir, "silence_phones.txt");
        distr_nonsilence_phones = new File(model_dir, "nonsilence_phones.txt");
        distr_lda = null;

        distrFiles = new File[]{distr_mfcc_config, distr_mdl, distr_tree, distr_HCLG, distr_L_fst, distr_words_txt,
                distr_phones_txt, distr_word_boundaries, distr_silence_phones, distr_nonsilence_phones, distr_lda};
    }

    public static void test() throws FileNotFoundException {
        for (File file : distrFiles)
            if (file != null && !file.exists())
                throw new FileNotFoundException(file.getAbsolutePath());
    }

    public static void prepare_lang(File dir, File dict, File vocab) throws IOException {

        File dict_p = new File(dir, "lexiconp.txt");
        File dict_disambig = new File(dir, "lexiconp_disambig.txt");
        File L_fst = new File(dir, "L.fst");
        File Ld_fst = new File(dir, "L_disambig.fst");
        File words = new File(dir, "words.txt");
        File phones = new File(dir, "phones.txt");
        File disambig_ids = new File(dir, "disambig.int");
        File word_boundaries = new File(dir, "word_boundary.int");

        File tmp1 = new File(dir, "tmp1");
        File tmp2 = new File(dir, "tmp2");
        File tmp3 = new File(dir, "tmp3");
        File tmp4 = new File(dir, "tmp4");

        FileUtils.add_probs_to_lexicon(dict, tmp1);
        FileUtils.add_besi_to_lexicon(tmp1, dict_p);

        int ndisambig = KaldiUtils.add_lex_disambig(false, true, dict_p, dict_disambig);
        ndisambig++; //add one disambig symbol for silence in lexicon FST

        FileUtils.make_words_list(vocab, words);
        FileUtils.make_phones_list(distr_silence_phones, distr_nonsilence_phones, ndisambig, phones);

        KaldiUtils.make_lexicon_fst(true, dict_p, 0.5, "sil", 0, tmp1);
        KaldiUtils.fstcompile(phones, words, false, false, tmp1, tmp2);
        KaldiUtils.fstarcsort("olabel", tmp2, L_fst);

        int ph_disamb_id = FileUtils.get_id_from_table(phones, "#0");
        FileUtils.writelnFile(tmp3, "" + ph_disamb_id);
        int w_disamb_id = FileUtils.get_id_from_table(words, "#0");
        FileUtils.writelnFile(tmp4, "" + w_disamb_id);

        FileUtils.writeLines(FileUtils.get_ids_from_table(phones, "^#.*$"), disambig_ids, "UTF-8");

        FileUtils.convert_besi_to_desc(phones, word_boundaries);

        KaldiUtils.make_lexicon_fst(true, dict_disambig, 0.5, "sil", ndisambig, tmp1);
        KaldiUtils.fstcompile(phones, words, false, false, tmp1, tmp2);
        KaldiUtils.fstaddselfloops(tmp3, tmp4, tmp2, tmp1);
        KaldiUtils.fstarcsort("olabel", tmp1, Ld_fst);

        if (removeTempFiles) {
            tmp1.delete();
            tmp2.delete();
            tmp3.delete();
            tmp4.delete();
        }
    }

    public static void makeL(File txt) throws IOException {
        Transcriber.test();
        KaldiUtils.test();

        File vocab = new File(lang_dir, "vocab.txt");
        File v_temp = new File(lang_dir, "tmp");
        File dict_raw = new File(lang_dir, "lexicon.raw");
        File dict = new File(lang_dir, "lexicon.txt");

        FileUtils.cleanup(lang_dir, new File[]{}, 5);

        FileUtils.makeVocab(txt, v_temp);

        Vector<String> lines = new Vector<String>();
        lines.add("<UNK>");
        lines.add("SIL");

        FileUtils.appendLines(v_temp, "UTF-8", vocab, "UTF-8", lines, true);

        Transcriber.transcribe(vocab, Settings.default_encoding, dict_raw, Settings.default_encoding, false);

        FileUtils.sort_uniq(dict_raw, dict, Settings.default_encoding);

        prepare_lang(lang_dir, dict, vocab);

        if (removeTempFiles) {
            v_temp.delete();
        }
    }

    public static File makeHCLG(File txt) throws IOException {

        Transcriber.test();
        NGram.test_srilm();
        KaldiUtils.test();

        File HCLG_fst = new File(lang_dir, "HCLG.fst");

        // File input_conv = new File(lang_dir, "input_cp1250.txt");
        File vocab = new File(lang_dir, "vocab.txt");
        File model = new File(lang_dir, "model.lm");
        File dict_raw = new File(lang_dir, "lexicon.raw");
        File dict = new File(lang_dir, "lexicon.txt");
        File G_fst = new File(lang_dir, "G.fst");

        FileUtils.cleanup(lang_dir, new File[]{}, 5);

        //NGram.estimate(txt, vocab, model, 3);
        NGram.srilm_estimate(txt, vocab, model, 3);

        Transcriber.transcribe(vocab, Settings.default_encoding, dict_raw, Settings.default_encoding, false);

        FileUtils.sort_uniq(dict_raw, dict, Settings.default_encoding);

        prepare_lang(lang_dir, dict, vocab);

        File words = new File(lang_dir, "words.txt");

        lm2fst(model, G_fst, words);

        // String text = FileUtils.readFile(txt);
        // linearfst(text, G_fst, word);

        File Ldisamb_fst = new File(lang_dir, "L_disambig.fst");
        File Ltemp_fst = new File(lang_dir, "Ltmp1");
        File Ltemp2_fst = new File(lang_dir, "Ltmp2");
        File LG_fst = new File(lang_dir, "LG.fst");

        KaldiUtils.fsttablecompose(Ldisamb_fst, G_fst, Ltemp_fst);
        KaldiUtils.fstdeterminizestar(Ltemp_fst, Ltemp2_fst, true);
        KaldiUtils.fstminimizeencoded(Ltemp2_fst, LG_fst);

        if (KaldiUtils.fstisstochastic(LG_fst))
            Log.verbose(LG_fst.getName() + " is stochastic!");
        else
            Log.verbose(LG_fst.getName() + " is NOT stochastic!");

        if (removeTempFiles) {
            Ltemp_fst.delete();
            Ltemp2_fst.delete();
        }

        File disamb_syms_in = new File(lang_dir, "disambig.int");
        File disamb_syms_out = new File(lang_dir, "disambig_ilabels.int");
        File ilabels_out = new File(lang_dir, "ilabels");
        File CLG_fst = new File(lang_dir, "CLG.fst");

        KaldiUtils.fstcomposecontext(3, 1, disamb_syms_in, disamb_syms_out, ilabels_out, LG_fst, CLG_fst);

        if (KaldiUtils.fstisstochastic(CLG_fst))
            Log.verbose(CLG_fst.getName() + " is stochastic!");
        else
            Log.verbose(CLG_fst.getName() + " is NOT stochastic!");

        File h_disamb_syms = new File(lang_dir, "disambig_tid.int");
        File H_fst = new File(lang_dir, "H.fst");

        KaldiUtils.make_h_transducer(h_disamb_syms, 1.0f, ilabels_out, distr_tree, distr_mdl, H_fst);

        KaldiUtils.fsttablecompose(H_fst, CLG_fst, Ltemp_fst);
        KaldiUtils.fstdeterminizestar(Ltemp_fst, Ltemp2_fst, true);
        KaldiUtils.fstrmsymbols(h_disamb_syms, Ltemp2_fst, Ltemp_fst);
        KaldiUtils.fstminimizeencoded(Ltemp_fst, Ltemp2_fst);

        if (KaldiUtils.fstisstochastic(Ltemp2_fst))
            Log.verbose(Ltemp2_fst.getName() + " is stochastic!");
        else
            Log.verbose(Ltemp2_fst.getName() + " is NOT stochastic!");

        KaldiUtils.add_self_loops(0.1f, true, distr_mdl, Ltemp2_fst, HCLG_fst);

        if (KaldiUtils.fstisstochastic(HCLG_fst))
            Log.verbose(HCLG_fst.getName() + " is stochastic!");
        else
            Log.verbose(HCLG_fst.getName() + " is NOT stochastic!");

        if (removeTempFiles) {
            Ltemp_fst.delete();
            Ltemp2_fst.delete();
        }

        return HCLG_fst;
    }

    public static File makeHCLG(File G_fst, File Ldisamb_fst, File HCLG_fst) throws IOException {

        File Ltemp_fst = new File(lang_dir, "Ltmp1");
        File Ltemp2_fst = new File(lang_dir, "Ltmp2");
        File LG_fst = new File(lang_dir, "LG.fst");

        KaldiUtils.fsttablecompose(Ldisamb_fst, G_fst, Ltemp_fst);
        KaldiUtils.fstdeterminizestar(Ltemp_fst, Ltemp2_fst, true);
        KaldiUtils.fstminimizeencoded(Ltemp2_fst, LG_fst);

        if (KaldiUtils.fstisstochastic(LG_fst))
            Log.verbose(LG_fst.getName() + " is stochastic!");
        else
            Log.verbose(LG_fst.getName() + " is NOT stochastic!");

        if (removeTempFiles) {
            Ltemp_fst.delete();
            Ltemp2_fst.delete();
        }

        File disamb_syms_in = new File(lang_dir, "disambig.int");
        File disamb_syms_out = new File(lang_dir, "disambig_ilabels.int");
        File ilabels_out = new File(lang_dir, "ilabels");
        File CLG_fst = new File(lang_dir, "CLG.fst");

        KaldiUtils.fstcomposecontext(3, 1, disamb_syms_in, disamb_syms_out, ilabels_out, LG_fst, CLG_fst);

        if (KaldiUtils.fstisstochastic(CLG_fst))
            Log.verbose(CLG_fst.getName() + " is stochastic!");
        else
            Log.verbose(CLG_fst.getName() + " is NOT stochastic!");

        File h_disamb_syms = new File(lang_dir, "disambig_tid.int");
        File H_fst = new File(lang_dir, "H.fst");

        KaldiUtils.make_h_transducer(h_disamb_syms, 1.0f, ilabels_out, distr_tree, distr_mdl, H_fst);

        KaldiUtils.fsttablecompose(H_fst, CLG_fst, Ltemp_fst);
        KaldiUtils.fstdeterminizestar(Ltemp_fst, Ltemp2_fst, true);
        KaldiUtils.fstrmsymbols(h_disamb_syms, Ltemp2_fst, Ltemp_fst);
        KaldiUtils.fstminimizeencoded(Ltemp_fst, Ltemp2_fst);

        if (KaldiUtils.fstisstochastic(Ltemp2_fst))
            Log.verbose(Ltemp2_fst.getName() + " is stochastic!");
        else
            Log.verbose(Ltemp2_fst.getName() + " is NOT stochastic!");

        KaldiUtils.add_self_loops(0.1f, true, distr_mdl, Ltemp2_fst, HCLG_fst);

        if (KaldiUtils.fstisstochastic(HCLG_fst))
            Log.verbose(HCLG_fst.getName() + " is stochastic!");
        else
            Log.verbose(HCLG_fst.getName() + " is NOT stochastic!");

        if (removeTempFiles) {
            Ltemp_fst.delete();
            Ltemp2_fst.delete();
        }

        return HCLG_fst;
    }

    public static void linearfst(String text, File output, File word_symbols) throws IOException {

        String seq[] = text.split("\\s+");

        File temp = File.createTempFile("linearfst", ".tmp", output.getParentFile());

        PrintWriter writer = new PrintWriter(temp, "CP1250");

        int s = 0;

        for (String w : seq) {
            writer.println(s + "\t" + (s + 1) + "\t" + w + "\t" + w);
            s++;
        }
        writer.println("" + s);

        writer.close();

        KaldiUtils.fstcompile(word_symbols, word_symbols, false, false, temp, output);

        if (removeTempFiles) {
            temp.delete();
        }

    }

    public static void lm2fst(File input, File output, File word_symbols) throws IOException {

        File temp = File.createTempFile("arpafst", ".tmp", input.getParentFile());
        File temp2 = File.createTempFile("arpafst", ".tmp", input.getParentFile());
        File temp3 = File.createTempFile("arpafst", ".tmp", input.getParentFile());
        File temp4 = File.createTempFile("arpafst", ".tmp", input.getParentFile());
        File temp5 = File.createTempFile("arpafst", ".tmp", input.getParentFile());
        File temp6 = File.createTempFile("arpafst", ".tmp", input.getParentFile());

        ArrayList<String> lines = new ArrayList<String>();
        lines.add("<s> <s>");
        lines.add("</s> <s>");
        lines.add("</s> </s>");

        FileUtils.removeLines(input, Settings.default_encoding, temp, Settings.default_encoding, lines, false);

        KaldiUtils.arpa2fst(temp, temp2);

        KaldiUtils.fstprint(temp2, temp3);

        KaldiUtils.eps2disambig(temp3, temp4);

        KaldiUtils.s2eps(temp4, temp5);

        KaldiUtils.fstcompile(word_symbols, word_symbols, false, false, temp5, temp6);

        KaldiUtils.fstrmepsilon(temp6, output);

        if (removeTempFiles) {
            temp.delete();
            temp2.delete();
            temp3.delete();
            temp4.delete();
            temp5.delete();
            temp6.delete();
        }

    }


    public static void checkFiles(File[] arr) throws FileNotFoundException {
        for (File file : arr) {
            if (!file.canRead())
                throw new FileNotFoundException(file.getAbsolutePath());
        }
    }


    public static void decode_online(File input_wav, File trans, File ali, boolean use_distr) throws IOException {

        File HCLG_fst = new File(lang_dir, "HCLG.fst");
        File words = new File(lang_dir, "words.txt");
        if (use_distr) {
            HCLG_fst = distr_HCLG;
            words = distr_words_txt;
        }

        checkFiles(new File[]{input_wav, trans, ali, distr_mdl, HCLG_fst, words});

        File wav_scp = new File(feat_dir, "wav.scp");

        FileUtils.makeSCPFile(wav_scp, new File[]{input_wav}, true);

        KaldiUtils.online_wav_gmm_decode_faster(0.5f, 3.0f, 6000, 72.0f, 0.0769f, wav_scp, distr_mdl, HCLG_fst,
                words, trans, ali, distr_lda);

    }

    public static Segmentation decode(File input_wav, boolean use_distr) throws IOException, RuntimeException {

        File HCLG_fst = new File(lang_dir, "HCLG.fst");
        File words_txt = new File(lang_dir, "words.txt");
        File phones_txt = new File(lang_dir, "phones.txt");
        File word_boundaries = new File(lang_dir, "word_boundary.int");
        if (use_distr) {
            HCLG_fst = distr_HCLG;
            words_txt = distr_words_txt;
            phones_txt = distr_phones_txt;
            word_boundaries = distr_word_boundaries;
        }

        File wav_scp = new File(feat_dir, "wav.scp");
        File feat = new File(feat_dir, "feat");
        File cmvn_stats = new File(feat_dir, "cmvn.stats");
        File cmvn_feat = new File(feat_dir, "cmvn");
        File delta_feat = new File(feat_dir, "delta");
        File lattice = new File(temp_dir, "lattice");
        File aligned_lattice = new File(temp_dir, "aligned");
        File words = new File(temp_dir, "words");
        File alignment = new File(temp_dir, "alignment");
        File ctm_words_raw = new File(temp_dir, "words_raw.ctm");
        File ctm_words = new File(temp_dir, "words.ctm");
        File phone_lattice = new File(temp_dir, "phone.lat");
        File best_phone_lattice = new File(temp_dir, "best_phone.lat");
        File ctm_phones_raw = new File(temp_dir, "phones_raw.ctm");
        File ctm_phones = new File(temp_dir, "phones.ctm");

        FileUtils.makeSCPFile(wav_scp, new File[]{input_wav}, true);

        KaldiUtils.compute_mfcc_feats(distr_mfcc_config, wav_scp, feat);

        KaldiUtils.compute_cmvn_stats(feat, cmvn_stats);

        KaldiUtils.apply_cmvn(cmvn_stats, feat, cmvn_feat);

        KaldiUtils.add_deltas(cmvn_feat, delta_feat);

        KaldiUtils.gmm_latgen_faster(distr_mdl, HCLG_fst, delta_feat, lattice, words, alignment);

        KaldiUtils.lattice_align_words(word_boundaries, distr_mdl, lattice, aligned_lattice);

        KaldiUtils.lattice_to_ctm_conf(aligned_lattice, ctm_words_raw);

        KaldiUtils.int2sym("5", words_txt, ctm_words_raw, ctm_words);

        KaldiUtils.lattice_to_phone_lattice(distr_mdl, aligned_lattice, phone_lattice);

        KaldiUtils.lattice_1best(phone_lattice, best_phone_lattice);

        KaldiUtils.nbest_to_ctm(best_phone_lattice, ctm_phones_raw);

        KaldiUtils.int2sym("5", phones_txt, ctm_phones_raw, ctm_phones);

        CTM wctm = new CTM();
        wctm.read(ctm_words);
        CTM pctm = new CTM();
        pctm.read(ctm_phones);
        SegmentationList ret = new SegmentationList();
        ret.addTier(wctm, 0);
        ret.addTier(pctm, 0);
        return ret;
    }

    public static Segmentation decode_oracle(File input_wav, File input_txt, boolean use_distr) throws IOException, RuntimeException {

        File HCLG_fst = new File(lang_dir, "HCLG.fst");
        File words_txt = new File(lang_dir, "words.txt");
        File phones_txt = new File(lang_dir, "phones.txt");
        File word_boundaries = new File(lang_dir, "word_boundary.int");
        if (use_distr) {
            HCLG_fst = distr_HCLG;
            words_txt = distr_words_txt;
            phones_txt = distr_phones_txt;
            word_boundaries = distr_word_boundaries;
        }

        File txt_file = new File(temp_dir, "input.txt");
        File txt_int = new File(temp_dir, "input.int");
        File wav_scp = new File(feat_dir, "wav.scp");
        File feat = new File(feat_dir, "feat");
        File cmvn_stats = new File(feat_dir, "cmvn.stats");
        File cmvn_feat = new File(feat_dir, "cmvn");
        File delta_feat = new File(feat_dir, "delta");
        File lattice = new File(temp_dir, "lattice");
        File aligned_lattice = new File(temp_dir, "aligned");
        File oracle_lattice = new File(temp_dir, "oracle");
        File words = new File(temp_dir, "words");
        File alignment = new File(temp_dir, "alignment");
        File ctm_words_raw = new File(temp_dir, "words_raw.ctm");
        File ctm_words = new File(temp_dir, "words.ctm");
        File phone_lattice = new File(temp_dir, "phone.lat");
        File best_phone_lattice = new File(temp_dir, "best_phone.lat");
        File ctm_phones_raw = new File(temp_dir, "phones_raw.ctm");
        File ctm_phones = new File(temp_dir, "phones.ctm");

        FileUtils.appendName(input_wav.getName(), input_txt, txt_file);

        FileUtils.makeSCPFile(wav_scp, new File[]{input_wav}, true);

        KaldiUtils.compute_mfcc_feats(distr_mfcc_config, wav_scp, feat);

        KaldiUtils.compute_cmvn_stats(feat, cmvn_stats);

        KaldiUtils.apply_cmvn(cmvn_stats, feat, cmvn_feat);

        KaldiUtils.add_deltas(cmvn_feat, delta_feat);

        KaldiUtils.gmm_latgen_faster(distr_mdl, HCLG_fst, delta_feat, lattice, words, alignment);

        KaldiUtils.sym2int("2-", words_txt, txt_file, txt_int);

        KaldiUtils.lattice_oracle(lattice, txt_int, oracle_lattice, words_txt);

        KaldiUtils.lattice_align_words(word_boundaries, distr_mdl, oracle_lattice, aligned_lattice);

        KaldiUtils.lattice_to_ctm_conf(aligned_lattice, ctm_words_raw);

        KaldiUtils.int2sym("5", words_txt, ctm_words_raw, ctm_words);

        KaldiUtils.lattice_to_phone_lattice(distr_mdl, aligned_lattice, phone_lattice);

        KaldiUtils.lattice_1best(phone_lattice, best_phone_lattice);

        KaldiUtils.nbest_to_ctm(best_phone_lattice, ctm_phones_raw);

        KaldiUtils.int2sym("5", phones_txt, ctm_phones_raw, ctm_phones);

        CTM wctm = new CTM();
        wctm.read(ctm_words);
        CTM pctm = new CTM();
        pctm.read(ctm_phones);
        SegmentationList ret = new SegmentationList();
        ret.addTier(wctm, 0);
        ret.addTier(pctm, 0);
        return ret;
    }

    public static Segmentation align(File input_wav, File input_txt, boolean use_distr) throws IOException, RuntimeException {

        File words_txt = new File(lang_dir, "words.txt");
        File phones_txt = new File(lang_dir, "phones.txt");
        File word_boundaries = new File(lang_dir, "word_boundary.int");
        File L_fst = new File(lang_dir, "L.fst");
        if (use_distr) {
            words_txt = distr_words_txt;
            phones_txt = distr_phones_txt;
            word_boundaries = distr_word_boundaries;
            L_fst = distr_L_fst;
        }

        File wav_scp = new File(feat_dir, "wav.scp");
        File feat = new File(feat_dir, "feat");
        File cmvn_stats = new File(feat_dir, "cmvn.stats");
        File cmvn_feat = new File(feat_dir, "cmvn");
        File delta_feat = new File(feat_dir, "delta");
        File transcription_txt = new File(temp_dir, "transcription.txt");
        File transcription_int = new File(temp_dir, "transcription.int");
        File transcription_bin = new File(temp_dir, "transcription");
        File alignment_bin = new File(temp_dir, "alignment");
        File alignment_int = new File(temp_dir, "alignment.int");
        File alignment_txt = new File(temp_dir, "alignment.txt");
        File lattice = new File(temp_dir, "lattice");
        File aligned_lattice = new File(temp_dir, "aligned.lat");
        File ctm_words_raw = new File(temp_dir, "ctm.raw");
        File ctm_words = new File(temp_dir, "ctm");
        File phone_lattice = new File(temp_dir, "phone.lat");
        File best_phone_lattice = new File(temp_dir, "best_phone.lat");
        File ctm_phones_raw = new File(temp_dir, "phones_raw.ctm");
        File ctm_phones = new File(temp_dir, "phones.ctm");

        FileUtils.appendName(input_wav.getName(), input_txt, transcription_txt);

        KaldiUtils.sym2int("2-", words_txt, transcription_txt, transcription_int);

        KaldiUtils.copy_int_vector("ark,t", transcription_int, "ark", transcription_bin);

        FileUtils.makeSCPFile(wav_scp, new File[]{input_wav}, true);

        KaldiUtils.compute_mfcc_feats(distr_mfcc_config, wav_scp, feat);

        KaldiUtils.compute_cmvn_stats(feat, cmvn_stats);

        KaldiUtils.apply_cmvn(cmvn_stats, feat, cmvn_feat);

        KaldiUtils.add_deltas(cmvn_feat, delta_feat);

        KaldiUtils.gmm_align(Settings.align_beam, Settings.align_retry_beam, distr_tree, distr_mdl, L_fst, delta_feat,
                transcription_bin, alignment_bin);

        KaldiUtils.copy_int_vector("ark", alignment_bin, "ark,t", alignment_int);

        KaldiUtils.show_alignments(phones_txt, distr_mdl, alignment_int, alignment_txt);

        KaldiUtils.linear_to_nbest(alignment_bin, transcription_bin, lattice);

        KaldiUtils.lattice_align_words(word_boundaries, distr_mdl, lattice, aligned_lattice);

        KaldiUtils.nbest_to_ctm(aligned_lattice, ctm_words_raw);

        KaldiUtils.int2sym("5", words_txt, ctm_words_raw, ctm_words);

        KaldiUtils.lattice_to_phone_lattice(distr_mdl, aligned_lattice, phone_lattice);

        KaldiUtils.lattice_1best(phone_lattice, best_phone_lattice);

        KaldiUtils.nbest_to_ctm(best_phone_lattice, ctm_phones_raw);

        KaldiUtils.int2sym("5", phones_txt, ctm_phones_raw, ctm_phones);

        if (ctm_phones.length() == 0)
            throw new RuntimeException("Phones CTM is empty!");

        CTM wctm = new CTM();
        wctm.read(ctm_words);
        CTM pctm = new CTM();
        pctm.read(ctm_phones);
        SegmentationList ret = new SegmentationList();
        ret.addTier(wctm, 0);
        ret.addTier(pctm, 0);
        return ret;
    }
}
