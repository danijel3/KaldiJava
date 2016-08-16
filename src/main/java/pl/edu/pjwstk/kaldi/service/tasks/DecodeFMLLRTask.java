package pl.edu.pjwstk.kaldi.service.tasks;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import pl.edu.pjwstk.kaldi.programs.KaldiScripts;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils.FMLLRUpdateType;
import pl.edu.pjwstk.kaldi.utils.FileUtils;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class DecodeFMLLRTask extends Task {

	private File input_file;
	private File mfcc_config;
	private File decode_config;

	private File hclg_file;
	private File words_table;
	private File phones_table;
	private File word_boundaries;
	private File lda_matrix;

	private File ali_mdl;
	private File adapt_mdl;
	private File final_mdl;

	private String silence_list;

	@Override
	public void run() {
		state = State.RUNNING;

		boolean fail = false;
		File files[] = { input_file, mfcc_config, decode_config, hclg_file,
				words_table, phones_table, word_boundaries, lda_matrix,
				ali_mdl, adapt_mdl, final_mdl };
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
		File splice = new File(Settings.curr_task_dir, "splice");
		File lda = new File(Settings.curr_task_dir, "lda");

		File lattice = new File(Settings.curr_task_dir, "lattice");
		File post = new File(Settings.curr_task_dir, "post");
		File postsil = new File(Settings.curr_task_dir, "postsil");
		File gpost = new File(Settings.curr_task_dir, "gpost");
		File pre_trans = new File(Settings.curr_task_dir, "pretrans");
		File fmllr1 = new File(Settings.curr_task_dir, "fmllr1");
		File fmllr1_lattice = new File(Settings.curr_task_dir, "fmllr1_lattice");
		File fmllr1_latpruned = new File(Settings.curr_task_dir,
				"fmllr1_latpruned");
		File trans = new File(Settings.curr_task_dir, "trans");
		File fmllr2 = new File(Settings.curr_task_dir, "fmllr2");
		File fmllr2_lattice = new File(Settings.curr_task_dir, "fmllr2_lattice");
		File fmllr2_latpruned = new File(Settings.curr_task_dir,
				"fmllr2_latpruned");

		File words = new File(Settings.curr_task_dir, "words");
		File alignment = new File(Settings.curr_task_dir, "alignment");
		File words_int = new File(Settings.curr_task_dir, "words.int");
		File words_txt = new File(Settings.curr_task_dir, "words.txt");
		File aligned_lattice = new File(Settings.curr_task_dir,
				"aligned_lattice");
		File ctm_int = new File(Settings.curr_task_dir, "ctm.int");
		File ctm_txt = new File(Settings.curr_task_dir, "ctm.txt");

		try {

			FileUtils.makeSCPFile(scp_file, new File[] { input_file }, true);

			KaldiUtils.compute_mfcc_feats(mfcc_config, scp_file, mfcc);

			KaldiUtils.compute_cmvn_stats(mfcc, cmvn_stats);

			KaldiUtils.apply_cmvn(cmvn_stats, mfcc, cmvn);

			KaldiUtils.splice_feats(cmvn, splice);

			KaldiUtils.transform_feats(lda_matrix, false, splice, lda);

			KaldiUtils.gmm_latgen_faster(ali_mdl, hclg_file, lda, lattice,
					words, alignment);

			// TODO accwt as param?
			KaldiUtils.lattice_to_post(0.083333, lattice, post);

			KaldiUtils.weight_silence_post(0.01, silence_list, ali_mdl, post,
					postsil); // TODO silwt as param?

			KaldiUtils.gmm_post_to_gpost(ali_mdl, lda, postsil, gpost);

			KaldiUtils.gmm_est_fmllr_gpost(FMLLRUpdateType.full, null, ali_mdl,
					lda, gpost, pre_trans);

			KaldiUtils.transform_feats(pre_trans, true, lda, fmllr1);

			KaldiUtils.gmm_latgen_faster(adapt_mdl, hclg_file, fmllr1,
					fmllr1_lattice, words, alignment);

			// TODO params?
			KaldiUtils.lattice_determinize_pruned(0.083333, 4.0,
					fmllr1_lattice, fmllr1_latpruned);

			// TODO accwt as param?
			KaldiUtils.lattice_to_post(0.083333, fmllr1_latpruned, post);

			KaldiUtils.weight_silence_post(0.01, silence_list, adapt_mdl, post,
					postsil); // TODO silwt as param?

			KaldiUtils.gmm_est_fmllr(FMLLRUpdateType.full, null, adapt_mdl,
					fmllr1, postsil, trans);

			KaldiUtils.transform_feats(trans, true, fmllr1, fmllr2);

			KaldiUtils.gmm_rescore_lattice(final_mdl, fmllr1_lattice, fmllr2,
					fmllr2_lattice);

			// TODO params?
			KaldiUtils.lattice_determinize_pruned(0.083333, 4.0,
					fmllr2_lattice, fmllr2_latpruned);

			KaldiUtils.lattice_best_path(fmllr2_latpruned, words, alignment);

			KaldiUtils.copy_int_vector("ark", words, "ark,t", words_int);

			KaldiUtils.int2sym("2-", words_table, words_int, words_txt);

			KaldiUtils.lattice_align_words(word_boundaries, final_mdl,
					fmllr2_latpruned, aligned_lattice);

			KaldiUtils.lattice_to_ctm_conf(aligned_lattice, ctm_int);

			KaldiUtils.int2sym("5", words_table, ctm_int, ctm_txt);

			state = State.SUCCEEDED;

		} catch (Exception e) {
			Log.error("Decoding task.", e);
			state = State.FAILED;
		}
	}

	@Override
	public void loadSettings(XPath xpath, Element node)
			throws XPathExpressionException {

		input_file = new File((String) xpath.evaluate("input-file", node,
				XPathConstants.STRING));
		mfcc_config = new File((String) xpath.evaluate("mfcc-config", node,
				XPathConstants.STRING));
		decode_config = new File((String) xpath.evaluate("decode-config", node,
				XPathConstants.STRING));

		hclg_file = new File((String) xpath.evaluate("hclg", node,
				XPathConstants.STRING));
		words_table = new File((String) xpath.evaluate("words-table", node,
				XPathConstants.STRING));
		phones_table = new File((String) xpath.evaluate("phones-table", node,
				XPathConstants.STRING));
		word_boundaries = new File((String) xpath.evaluate("word-boundaries",
				node, XPathConstants.STRING));
		lda_matrix = new File((String) xpath.evaluate("lda-matrix", node,
				XPathConstants.STRING));

		ali_mdl = new File((String) xpath.evaluate("ali-mdl", node,
				XPathConstants.STRING));
		adapt_mdl = new File((String) xpath.evaluate("adapt-mdl", node,
				XPathConstants.STRING));
		final_mdl = new File((String) xpath.evaluate("final-mdl", node,
				XPathConstants.STRING));

		silence_list = (String) xpath.evaluate("silence-list", node,
				XPathConstants.STRING);

	}

	public static void main(String[] args) {

		try {
			Locale.setDefault(Locale.ENGLISH);

			Settings.curr_task_dir = File.createTempFile("DecodeFMLLRUnitTest",
					"", Settings.tasks_dir);
			Settings.curr_task_dir.delete();
			Settings.curr_task_dir.mkdirs();

			Settings.log_dir = Settings.curr_task_dir;
			Settings.temp_dir = new File(Settings.curr_task_dir, "tmp");
			Settings.temp_dir2 = new File(Settings.curr_task_dir, "tmp2");

			Log.init("DecodeFMLLRUnitTests", true);

			KaldiUtils.init();
			KaldiUtils.test();
			KaldiScripts.init();
			KaldiScripts.test();

			DecodeFMLLRTask task = new DecodeFMLLRTask();

			task.input_file = new File(
					"/home/guest/data/RadioPiNKaldi/Elzbieta_Bienkowska/wav/Elzbieta_Bienkowska_002.wav");
			task.mfcc_config = new File(
					"/home/guest/kaldi/egs/synat2/s5/conf/mfcc.conf");
			task.decode_config = new File(
					"/home/guest/kaldi/egs/synat2/s5/conf/decode.config");

			task.hclg_file = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/graph/HCLG.fst");
			task.words_table = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/graph/words.txt");
			task.phones_table = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/graph/phones.txt");
			task.word_boundaries = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/graph/phones/word_boundary.int");
			task.lda_matrix = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/final.mat");

			task.ali_mdl = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/final.alimdl");

			task.adapt_mdl = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b/final.mdl");

			task.final_mdl = new File(
					"/home/guest/kaldi/egs/synat2/s5/exp/tri3b_mmi/final.mdl");

			task.silence_list = "1:2:3:4:5";

			task.run();

		} catch (Exception e) {
			Log.error("Main error.", e);
		}
	}

	@Override
	public void updateHash(MessageDigest m) throws IOException {
		processFileHash(m, input_file);
	}
}
