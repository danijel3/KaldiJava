package pl.edu.pjwstk.kaldi.files.julius;

import java.util.Vector;

import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.SegmentationList;

public class AlignedSequence {

	public static class AlignedWord {
		public String word;
		public int start, end;
		public double score;
		public boolean correct;
	}

	int max_len = 0;

	public Vector<AlignedWord> sequence;

	public AlignedSequence() {
		sequence = new Vector<AlignedWord>();
	}

	public void addWord(String line) {
		if (!line.startsWith("["))
			return;

		line = line.replace('[', ' ');
		line = line.replace(']', ' ');

		String[] args = line.split("\\s+");

		AlignedWord word = new AlignedWord();

		word.start = Integer.parseInt(args[1]);
		word.end = Integer.parseInt(args[2]);
		word.score = Double.parseDouble(args[3]);
		word.word = args[4];
		word.correct = true;

		if (word.end > max_len)
			max_len = word.end;

		if (word.word.equals("<s>") || word.word.equals("</s>"))
			return;

		sequence.add(word);
	}

	public int getLength() {
		return max_len;
	}

	public String toString() {
		String ret = "";
		for (AlignedWord w : sequence) {
			ret += w.word + " ";
		}
		return ret;
	}

	public AlignedSequence getCurrectSegments(boolean only_correct) {
		AlignedSequence ret = new AlignedSequence();

		boolean last = sequence.get(0).correct;
		String words = "";
		int start = sequence.get(0).start;
		int end = sequence.get(0).end;
		AlignedWord seq;

		for (AlignedWord word : sequence) {
			if (word.correct == last) {
				words += word.word + " ";
				end = word.end;
			} else {
				if (last || !only_correct) {
					seq = new AlignedWord();
					seq.start = start;
					seq.end = end;
					seq.correct = last;
					seq.word = words;
					ret.sequence.add(seq);
					if (ret.max_len < seq.end)
						ret.max_len = seq.end;
				}

				start = word.start;
				end = word.end;
				words = word.word + " ";
				last = word.correct;
			}
		}

		if (last || !only_correct) {
			seq = new AlignedWord();
			seq.start = start;
			seq.end = end;
			seq.correct = last;
			seq.word = words;
			ret.sequence.add(seq);
			if (ret.max_len < seq.end)
				ret.max_len = seq.end;
		}

		return ret;
	}

	public Segmentation toSegmentation(double win_off) {
		SegmentationList ret = new SegmentationList();

		ret.renameTier(0, "Julius");

		for (AlignedWord word : sequence) {
			ret.addSegment(0, word.start * win_off, word.end * win_off,
					word.word, word.score);
		}

		return ret;
	}

}
