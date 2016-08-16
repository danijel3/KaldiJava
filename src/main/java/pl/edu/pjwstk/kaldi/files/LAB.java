package pl.edu.pjwstk.kaldi.files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class LAB extends Segmentation {

	public LAB() {

	}

	public LAB(Tier tier) {

		tiers.add(tier);

	}

	public void write(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file);

		Tier tier = tiers.get(0);

		for (Segment seg : tier.segments) {

			long start = (long) (seg.start_time * 10000000);
			long end = (long) (seg.end_time * 10000000);
			writer.format("%d %d %s %.2f\n", start, end, seg.name,
					seg.confidence);
		}
		writer.close();
	}

	@Override
	public void read(File file) throws IOException {
		throw new IOException("NYI");
	}

}
