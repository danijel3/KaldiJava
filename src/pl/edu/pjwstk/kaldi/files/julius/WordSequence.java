package pl.edu.pjwstk.kaldi.files.julius;

import java.util.StringTokenizer;
import java.util.Vector;

public class WordSequence {

	public Vector<String> words;

	public WordSequence() {
		words = new Vector<String>();
	}

	public WordSequence(String text) {
		this();

		StringTokenizer strtok = new StringTokenizer(text);
		while (strtok.hasMoreTokens())
			addWord(strtok.nextToken());
	}

	public void copy(WordSequence clone) {
		words.addAll(clone.words);
		original = clone.original;
		orig_num = clone.orig_num;
		levenshtein_row = clone.levenshtein_row;
		insertions = clone.insertions;
		deletions = clone.deletions;
		substitutions = clone.substitutions;
		error_sum = clone.error_sum;

		lev_old = new Error[clone.lev_old.length];
		for (int i = 0; i < lev_old.length; i++) {
			lev_old[i] = new Error();
			lev_old[i].copy(clone.lev_old[i]);
		}

		lev_new = new Error[clone.lev_new.length];
		for (int i = 0; i < lev_new.length; i++)
			lev_new[i] = new Error();
	}

	public void addWord(String word) {
		String test = word.trim().toLowerCase();
		if (test.equals("<s>") || test.equals("</s>"))
			return;

		words.add(word);
	}

	public int insertions, deletions, substitutions, error_sum;

	public String getResults() {
		String ret = "";

		int count = original.words.size();

		ret += "=======\n";
		ret += "Token count: " + count + "\n";
		ret += "Errors: " + error_sum + "\n";
		ret += "Insertions: " + insertions + "\n";
		ret += "Deletions: " + deletions + "\n";
		ret += "Substitutions: " + substitutions + "\n";
		ret += "------\n";
		ret += "Correctness: " + (count - (substitutions + deletions))
				/ (double) count * 100.0 + "\n";
		ret += "Accuracy: " + (count - error_sum) / (double) count * 100.0
				+ "\n";
		ret += "=======";

		return ret;
	}

	public String toString() {
		String ret = "";
		for (String w : words) {
			ret += w + " ";
		}
		return ret;
	}

	class Error {
		public int ins = 0, del = 0, sub = 0, sum = 0;

		public void copy(Error err) {
			ins = err.ins;
			del = err.del;
			sub = err.sub;
			sum = err.sum;
		}

		public String toString() {
			return ins + "," + del + "," + sub + "=" + sum;
		}
	}

	enum ErrorType {
		SUBSTITUTION, DELETION, INSERTION
	}

	private WordSequence original;
	private int orig_num;
	private Error[] lev_old, lev_new;
	private int levenshtein_row;

	public void setupLevenshtein(WordSequence original) {
		this.original = original;
		orig_num = original.words.size();
		lev_old = new Error[orig_num + 1];
		lev_new = new Error[orig_num + 1];
		levenshtein_row = 1;
		for (int i = 0; i <= orig_num; i++) {
			lev_new[i] = new Error();
			lev_old[i] = new Error();
			lev_old[i].del = i;
			lev_old[i].sum = i;
		}

		deletions = orig_num;
		substitutions = 0;
		insertions = 0;

		updateLevenshtein();
	}

	public void updateLevenshtein() {
		if (words.size() < levenshtein_row)
			return;

		int sub, ins, del;
		boolean words_match;

		for (; levenshtein_row <= words.size(); levenshtein_row++) {
			String test_word = words.get(levenshtein_row - 1);

			lev_new[0].copy(lev_old[0]);
			lev_new[0].ins += 2;
			lev_new[0].sum += 2;
			for (int j = 1; j <= orig_num; j++) {
				String orig_word = original.words.get(j - 1);
				if (test_word.equals(orig_word))
					words_match = true;
				else
					words_match = false;

				ins = lev_old[j].sum + 2;
				del = lev_new[j - 1].sum + 1;
				sub = lev_old[j - 1].sum;
				if (!words_match)
					sub++;

				ErrorType type;

				if (sub < ins) {
					if (sub < del)
						type = ErrorType.SUBSTITUTION;
					else
						type = ErrorType.DELETION;
				} else {
					if (ins < del)
						type = ErrorType.INSERTION;
					else
						type = ErrorType.DELETION;
				}

				switch (type) {
				case SUBSTITUTION:
					lev_new[j].copy(lev_old[j - 1]);
					if (!words_match) {
						lev_new[j].sub++;
						lev_new[j].sum++;
					}
					break;
				case DELETION:
					lev_new[j].copy(lev_new[j - 1]);
					lev_new[j].del++;
					lev_new[j].sum++;
					break;
				case INSERTION:
					lev_new[j].copy(lev_old[j]);
					lev_new[j].ins += 2;
					lev_new[j].sum += 2;
					break;
				}
			}

			Error[] t = lev_new;
			lev_new = lev_old;
			lev_old = t;
		}

		// insertions are weighted double to minimize their presence (in order
		// to improve accuracy)?
		insertions = lev_old[orig_num].ins / 2;
		substitutions = lev_old[orig_num].sub;
		deletions = lev_old[orig_num].del;
		error_sum = insertions + substitutions + deletions;
	}

}
