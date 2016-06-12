package pl.edu.pjwstk.kaldi.files.julius;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import pl.edu.pjwstk.kaldi.utils.Settings;

public class WordList {

	Set<String> words;

	public WordList() {
		words = new HashSet<String>();
	}

	public void readFromDictionary(File file) throws IOException {
		words.clear();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), Settings.julius_default_encoding));

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().length() == 0)
				continue;

			String[] arr = line.split("\\s+");

			words.add(arr[0]);
		}

		reader.close();
	}

	public int countMissingWords(WordSequence sequence) {
		int ret = 0;

		for (String word : sequence.words) {
			if (!words.contains(word))
				ret++;
		}

		return ret;
	}

	public Vector<String> getMissingWords(WordSequence sequence) {
		Vector<String> ret = new Vector<String>();

		for (String word : sequence.words) {
			if (!words.contains(word))
				ret.add(word);
		}

		return ret;
	}
}
