package pl.edu.pjwstk.kaldi.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class CTM extends Segmentation {

	@Override
	public void read(File file) throws IOException {

		tiers.clear();

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;
		String filename = "";
		String name;
		double start, end;

		while ((line = reader.readLine()) != null) {
			String tok[] = line.split("\\s");

			if (tok.length < 5 || tok.length > 6) {
				reader.close();
				throw new IOException("wrong line in file " + file.getName() + ": " + line);
			}

			filename = tok[0];
			try {
				start = Double.parseDouble(tok[2]);
				end = start + Double.parseDouble(tok[3]);
			} catch (NumberFormatException e) {
				reader.close();
				throw new IOException("wrong line in file " + file.getName() + ": " + line);
			}
			name = tok[4];

			addSegment(0, start, end, name);

			if (tok.length == 6) {
				addSegment(1, start, end, tok[5]);
			}
		}

		if (tiers.size() > 0)
			tiers.get(0).name = filename;
		if (tiers.size() > 1)
			tiers.get(1).name = "Confidence";

		reader.close();

	}

	@Override
	public void write(File file) throws IOException {

		PrintWriter writer = new PrintWriter(file);

		Tier tier = tiers.get(0);
		for (Segment segment : tier.segments) {
			writer.format("%s 1 %1.2f %1.2f %s %1.2f\n", tier.name, segment.start_time, segment.end_time
					- segment.start_time, segment.name, segment.confidence);
		}

		writer.close();

	}
}
