package pl.edu.pjwstk.kaldi.programs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.pjwstk.kaldi.utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

/*
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;
*/

/**
 * THIS FILE IS COMMENTED OUT
 * Lium is lincensed under GPL, so we cannot include it in this project directly.
 * The file will remain commented for now until we figure out how to resolve this issue...
 */

public class Lium {

    private final static Logger logger = LoggerFactory.getLogger(Lium.class);

    private static File gender_model = new File(Settings.lium_models,
            "gender.gmms");
    private static File sil_model = new File(Settings.lium_models, "s.gmms");
    private static File silsp_model = new File(Settings.lium_models, "sms.gmms");
    private static File ubm_model = new File(Settings.lium_models, "ubm.gmm");

    public static void test() throws FileNotFoundException {

        File[] list = new File[]{gender_model, sil_model, silsp_model,
                ubm_model};

        String missing = "";
        for (File file : list) {
            if (!file.exists())
                missing += file.getAbsolutePath() + "\n";
        }

        if (!missing.isEmpty())
            throw new FileNotFoundException(missing);

    }

    public static void diarize(File input) {

        throw new RuntimeException("LIUM is disabled in this version!");

        /*
        File uem = new File(Settings.curr_task_dir, "show.uem.seg");
		File iseg = new File(Settings.curr_task_dir, "show.i.seg");
		// File pmsseg = new File(Settings.curr_task_dir, "show.pms.seg");
		File sseg = new File(Settings.curr_task_dir, "show.s.seg");
		File lseg = new File(Settings.curr_task_dir, "show.l.seg");
		File hseg = new File(Settings.curr_task_dir, "show.h.seg");
		File init_gmms = new File(Settings.curr_task_dir, "show.init.gmms");
		File gmms = new File(Settings.curr_task_dir, "show.gmms");
		File dseg = new File(Settings.curr_task_dir, "show.d.seg");
		File adjseg = new File(Settings.curr_task_dir, "show.adj.seg");
		// File fltseg = new File(Settings.curr_task_dir, "show.flt.seg");
		// File splseg = new File(Settings.curr_task_dir, "show.spl.seg");
		File gseg = new File(Settings.curr_task_dir, "show.g.seg");
		File cgmm = new File(Settings.curr_task_dir, "show.cgmm");
		File finalseg = new File(Settings.curr_task_dir, "show.seg");

		File gridfile = new File(Settings.curr_task_dir, "show.TextGrid");

		try {

			logger.info("Running LIUM...");

			PrintWriter writer = new PrintWriter(uem);
			writer.println("show 2 0 1000000000 U U U 1");
			writer.close();

			// Check the validity of the MFCC
			MSegInit.main(new String[] { "--help",
					"--fInputMask=" + input.getAbsolutePath(),
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					" --sInputMask=" + uem.getAbsolutePath(),
					"--sOutputMask=" + iseg.getAbsolutePath(), "show" });

			// Speech / non-speech segmentation using a set of GMMs
			// MDecode.main(new String[] { "--help",
			// "--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,0:0:0",
			// "--fInputMask=" + input.getAbsolutePath(),
			// "--sInputMask=" + iseg.getAbsolutePath(),
			// "--sOutputMask=" + pmsseg.getAbsolutePath(),
			// "--dPenality=500,500,10",
			// "--tInputMask=" + silsp_model.getAbsolutePath(), "show" });

			// GLR-based segmentation, make small segments
			MSeg.main(new String[] { "--help", "--kind=FULL", "--sMethod=GLR",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					//"--sMimumWindowSize=13", 
					//"--sModelWindowSize=13",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + iseg.getAbsolutePath(),
					"--sOutputMask=" + sseg.getAbsolutePath(), "show" });

			// Linear clustering, fuse consecutive segments of the same speaker
			// from the start to the end
			MClust.main(new String[] { "--help",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + sseg.getAbsolutePath(),
					"--sOutputMask=" + lseg.getAbsolutePath(), "--cMethod=l",
					"--cThr=1", "show" });

			// Hierarchical bottom-up BIC clustering
			MClust.main(new String[] { "--help",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + lseg.getAbsolutePath(),
					"--sOutputMask=" + hseg.getAbsolutePath(), "--cMethod=h",
					"--cThr=1", "show" });

			// Initialize one speaker GMM with 8 diagonal Gaussian components
			// for each
			MTrainInit.main(new String[] { "--help", "--nbComp=8",
					"--kind=DIAG",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + hseg.getAbsolutePath(),
					"--tOutputMask=" + init_gmms.getAbsolutePath(), "show" });

			// EM computation for each GMM
			MTrainEM.main(new String[] { "--help", "--nbComp=8", "--kind=DIAG",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + hseg.getAbsolutePath(),
					"--tOutputMask=" + gmms.getAbsolutePath(),
					"--tInputMask=" + init_gmms.getAbsolutePath(), "show" });

			// Viterbi decoding using the set of GMMs trained by EM
			MDecode.main(new String[] { "--help",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + hseg.getAbsolutePath(),
					"--sOutputMask=" + dseg.getAbsolutePath(),
					"--dPenality=250",
					"--tInputMask=" + gmms.getAbsolutePath(), "show" });

			// Adjust segment boundaries near silence sections
			SAdjSeg.main(new String[] { "--help",
					"--fInputDesc=audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + dseg.getAbsolutePath(),
					"--sOutputMask=" + adjseg.getAbsolutePath(), "show" });

			// Filter speaker segmentation using sil/sp segmentation earlier
			// SFilter.main(new String[] { "--help",
			// "--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,0:0:0",
			// "--fInputMask=" + input.getAbsolutePath(),
			// "--fltSegMinLenSpeech=150", "--fltSegMinLenSil=25",
			// "--sFilterClusterName=music", "--fltSegPadding=25",
			// "--sFilterMask=" + pmsseg.getAbsolutePath(),
			// "--sInputMask=" + adjseg.getAbsolutePath(),
			// "--sOutputMask=" + fltseg.getAbsolutePath(), "show" });

			// Split segments longer than 20s (useful for transcription)
			// SSplitSeg.main(new String[] { "--help",
			// "--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,0:0:0",
			// "--fInputMask=" + input.getAbsolutePath(),
			// "--sSegMaxLen=2000", "--sSegMaxLenModel=2000",
			// "--sFilterClusterName=iS,iT,j",
			// "--sFilterMask=" + pmsseg.getAbsolutePath(),
			// "--sInputMask=" + fltseg.getAbsolutePath(),
			// "--sOutputMask=" + splseg.getAbsolutePath(),
			// "--tInputMask=" + sil_model.getAbsolutePath(), "show" });

			// Set gender and bandwidth
			SAdjSeg.main(new String[] { "--help", "--sGender", "--sByCluster",
					"--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,1:1:0",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + adjseg.getAbsolutePath(),
					"--sOutputMask=" + gseg.getAbsolutePath(),
					"--tInputMask=" + gender_model.getAbsolutePath(), "show" });

			// NCLR clustering Features contain static and delta and are
			// centered and reduced (--fInputDesc)

			MClust.main(new String[] { "--help",
					"--fInputDesc=audio2sphinx,1:3:2:0:0:0,13,1:1:300:4",
					"--fInputMask=" + input.getAbsolutePath(),
					"--sInputMask=" + gseg.getAbsolutePath(),
					"--sOutputMask=" + finalseg.getAbsolutePath(),
					"--cMethod=ce", "--cThr=1.7", "--emCtrl=1,5,0.01",
					"--sTop=5," + ubm_model.getAbsolutePath(),
					"--tInputMask=" + ubm_model.getAbsolutePath(),
					"--tOutputMask=" + cgmm.getAbsolutePath(), "show" });

			logger.info("Saving TextGrid...");

			TextGrid textgrid = new TextGrid();

			BufferedReader reader = new BufferedReader(new FileReader(finalseg));
			String line;

			double timebase = 0.01;

			while ((line = reader.readLine()) != null) {

				if (line.startsWith(";;"))
					continue;

				String[] tok = line.split("\\s+");

				int start = Integer.parseInt(tok[2]);
				int len = Integer.parseInt(tok[3]);
				String name = tok[7];

				textgrid.addSegment(0, start * timebase, (start + len)
						* timebase, name);
			}
			reader.close();

			textgrid.sort();
			textgrid.renameTier(0, "speakers");

			textgrid.write(gridfile);

			logger.info("Done LIUM!");

		} catch (Exception e) {
			logger.error("LIUM diarization", e);
		}
		*/
    }

    public static void main(String[] args) {

        try {

            Locale.setDefault(Locale.ENGLISH);

            Settings.curr_task_dir = new File("/home/guest/Desktop/tmp/");

            diarize(new File("/home/guest/Desktop/chain.wav"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
