package pl.edu.pjwstk.kaldi.files.julius;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import pl.edu.pjwstk.kaldi.utils.Settings;

public class Dictionary {

	class Transcriptions {
		public LinkedList<String> transcription;

		public Transcriptions(String word) {
			transcription = new LinkedList<String>();
			transcription.add(word);
		}
	}

	Map<String, Transcriptions> dictionary;

	public Dictionary() {
		dictionary = new HashMap<String, Transcriptions>();
	}

	public void load(File file) throws IOException, RuntimeException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), Settings.julius_default_encoding));

		dictionary.clear();

		String line, word, trans;
		int ret;

		while ((line = reader.readLine()) != null) {
			if (line.length() == 0)
				continue;

			ret = line.indexOf(' ');

			if (ret < 0) {
				reader.close();
				throw new RuntimeException("Error parsing line: " + line);
			}

			word = line.substring(0, ret);
			trans = line.substring(ret + 1);

			if (dictionary.containsKey(word)) {
				dictionary.get(word).transcription.add(trans);
			} else
				dictionary.put(word, new Transcriptions(trans));
		}

		reader.close();
	}

	public void generateSubDictionary(File wordlist, File subdic)
			throws IOException, RuntimeException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(wordlist),
						Settings.julius_default_encoding));
		PrintWriter writer = new PrintWriter(subdic,
				Settings.julius_default_encoding);

		Set<String> added = new HashSet<String>();

		writer.println("<s> sil");
		writer.println("</s> sil");
		writer.println("<UNK> sil");

		added.add("<s>");
		added.add("</s>");
		added.add("<UNK>");

		boolean error = false;
		String line;
		String arr[];
		Transcriptions trans;

		while ((line = reader.readLine()) != null) {
			arr = line.split("\\s+");
			for (String word : arr) {
				word = word.trim();
				if (word.length() == 0)
					continue;

				if (added.contains(word))
					continue;

				added.add(word);

				if (!dictionary.containsKey(word)) {
					error = true;
					System.out.println("ERROR: missing word in dictionary: "
							+ word);
				} else {
					trans = dictionary.get(word);
					for (String str_trans : trans.transcription)
						writer.println(word + " " + str_trans);
				}

			}
		}

		reader.close();
		writer.close();

		if (error) {
			throw new RuntimeException("Didn't find all words!");
		}
	}
}
