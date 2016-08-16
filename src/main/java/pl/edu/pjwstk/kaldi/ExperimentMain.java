package pl.edu.pjwstk.kaldi;

import pl.edu.pjwstk.kaldi.files.CTM;
import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.TextGrid;
import pl.edu.pjwstk.kaldi.grammars.Akt;
import pl.edu.pjwstk.kaldi.grammars.Grammar;
import pl.edu.pjwstk.kaldi.grammars.Numbers;
import pl.edu.pjwstk.kaldi.programs.KaldiScripts;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils;
import pl.edu.pjwstk.kaldi.programs.Transcriber;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;
import pl.edu.pjwstk.kaldi.utils.WAV;

import javax.xml.soap.Text;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class ExperimentMain {

    public static void main(String[] args) {

        try {
            File work_dir = new File("work");

            Log.init("Experiment", false);
            Transcriber.init();
            KaldiUtils.init();
            KaldiUtils.test();
            KaldiScripts.init(work_dir);
            KaldiScripts.test();

            Log.info("Starting experiment...");

            File lang_dir = KaldiScripts.lang_dir;
            File temp_dir = KaldiScripts.temp_dir;

            Grammar grammar = Akt.zgonu();

            File fst_txt = new File(lang_dir, "grammar.txt");
            File syms = new File(lang_dir, "grammar.syms");
            grammar.save(fst_txt, syms);

            File fst_bin = new File(lang_dir, "grammar.bin");
            KaldiUtils.fstcompile(syms, syms, false, false, fst_txt, fst_bin);

            File fst_sort = new File(lang_dir, "grammar.sort");
            KaldiUtils.fstarcsort("ilabel", fst_bin, fst_sort);

            File fst_det = new File(lang_dir, "grammar.det");
            KaldiUtils.fstdeterminizestar(fst_sort, fst_det, false);

            File fst_min = new File(lang_dir, "grammar_min");
            KaldiUtils.fstminimizeencoded(fst_det, fst_min);

            Log.info("Generating random sequences from grammar...");
            Log.disableOutput();
            Random rand = new Random((new Date()).getTime());
            PrintWriter rand_samples = new PrintWriter(new File(work_dir, "random_grammar.txt"));
            for (int i = 0; i < 100; i++) {
                File fst_rand = new File(lang_dir, "rand.fst");
                KaldiUtils.fstrandgen(fst_min, fst_rand, rand.nextInt());

                File rand_txt = new File(lang_dir, "rand.txt");
                KaldiUtils.fstprint(fst_rand, rand_txt, syms, syms);

                Vector<String> words = FileUtils.cut(rand_txt, 2);
                rand_samples.println("" + words);
            }
            Log.enableOutput();


            File fst_dot = new File(work_dir, "grammar.dot");
            KaldiUtils.fstdraw(fst_min, fst_dot, syms, syms, true);

            File v_temp = new File(lang_dir, "vocab.tmp");
            File vocab = new File(lang_dir, "vocab");

            HashSet<String> skip = new HashSet<String>();
            skip.add("<eps>");

            FileUtils.getSymsFromList(syms, v_temp, skip);

            Vector<String> lines = new Vector<String>();
            lines.add("<UNK>");
            lines.add("SIL");

            FileUtils.appendLines(v_temp, "UTF-8", vocab, "UTF-8", lines, true);

            File dict_raw = new File(lang_dir, "dict_raw");
            File dict = new File(lang_dir, "dict");
            Transcriber.transcribe(vocab, Settings.default_encoding, dict_raw, Settings.default_encoding, false);
            FileUtils.sort_uniq(dict_raw, dict, Settings.default_encoding);

            KaldiScripts.prepare_lang(KaldiScripts.lang_dir, dict, vocab);


            File fst_txt2 = new File(lang_dir, "grammar_2.txt");
            KaldiUtils.fstprint(fst_min, fst_txt2, syms, syms);

            File fst_final = new File(lang_dir, "G.fst");
            File words = new File(lang_dir, "words.txt");
            KaldiUtils.fstcompile(words, words, false, false, fst_txt2, fst_final);




            File input_txt = new File("/home/guest/Desktop/GENEA/audio/metryka_zgonu.txt");
            Vector<String> input_tok = FileUtils.readTokens(input_txt, "UTF-8");
            Grammar input_lin = new Grammar();
            input_lin.setWordSequence(input_tok);
            File input_fst = new File(lang_dir, "input.fst");
            input_lin.save(input_fst, null);

            File input_bin = new File(lang_dir, "input.bin");
            KaldiUtils.fstcompile(words, words, false, false, input_fst, input_bin);

            File output_bin = new File(lang_dir, "output.bin");
            KaldiUtils.fstcompose(input_bin, fst_final, output_bin);

            if (input_bin.length() == output_bin.length()) {
                Log.info("Files are the same. Seems like everything is ok.");
            } else {
                Log.warn("Files are NOT the same! There may be a problem!");
            }



            File Ldisamb = new File(lang_dir, "L_disambig.fst");
            File HCLG = new File(lang_dir, "HCLG.fst");
            KaldiScripts.makeHCLG(fst_final, Ldisamb, HCLG);

            File wav = new File("/home/guest/Desktop/GENEA/audio/metryka_zgonu_16k.wav");
            KaldiScripts.decode(wav, false);

            File lattice = new File(temp_dir, "aligned");
            File lat_fst_txt = new File(temp_dir, "lat_fst.txt");
            KaldiUtils.lattice_to_fst(lattice, lat_fst_txt);

            File lat_fst_tmp = new File(temp_dir, "lat_fst_tmp.txt");
            FileUtils.tail(lat_fst_txt, lat_fst_tmp, 1);

            KaldiUtils.int2sym("3-4", words, lat_fst_tmp, lat_fst_txt);

            File lat_fst = new File(temp_dir, "lat.fst");
            KaldiUtils.fstcompile(words, words, false, false, lat_fst_txt, lat_fst);

            File lat_dot = new File(work_dir, "lattice.dot");
            KaldiUtils.fstdraw(lat_fst, lat_dot, words, words, true);

            File words_ctm = new File(temp_dir, "words.ctm");
            CTM ctm = new CTM();
            ctm.read(words_ctm);

            File out_tg = new File(work_dir, "out.TextGrid");
            TextGrid textgrid = new TextGrid(ctm);
            textgrid.write(out_tg);

            Segmentation align_seg = KaldiScripts.align(wav, input_txt, false);
            TextGrid tg_align=new TextGrid(align_seg);
            tg_align.write(new File(work_dir,"align.TextGrid"));

            Log.info("Done!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
