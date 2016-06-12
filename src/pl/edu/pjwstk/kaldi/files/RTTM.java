package pl.edu.pjwstk.kaldi.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import pl.edu.pjwstk.kaldi.utils.Log;

public class RTTM extends Segmentation {

	private double timebase = 1;

	public RTTM(double timebase) {
		this.timebase = timebase;
	}

	@Override
	public void read(File file) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;

		while ((line = reader.readLine()) != null) {

			String[] tok = line.split("\\s+");

			if (tok[0].equals("SPKR-INFO"))
				continue;

			double start = Double.parseDouble(tok[3]);
			double len = Double.parseDouble(tok[4]);
			String name = tok[7];

			addSegment(0, start * timebase, (start + len) * timebase, name);
		}
		reader.close();

		renameTier(0, "RTTM");

		sort();
	}

	@Override
	public void write(File file) throws IOException {

		PrintWriter writer = new PrintWriter(file);

		if (!tiers.isEmpty()) {
			if (tiers.size() > 1) {
				Log.warn("RTTM saving only first tier!");
			}

			Tier tier = tiers.get(0);

			for (Segment seg : tier.segments) {
				// SPEAKER speaker 1 0.0 12.51 <NA> <NA> speaker_1.0 <NA>
				writer.format("SPEAKER speaker 1 %f %f <NA> <NA> %s <NA>\n",
						seg.start_time / timebase,
						(seg.end_time - seg.start_time) / timebase, seg.name);
			}

		}

		writer.close();
	}
}
