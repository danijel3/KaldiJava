package pl.edu.pjwstk.kaldi.files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pl.edu.pjwstk.kaldi.files.Segmentation;
import pl.edu.pjwstk.kaldi.files.Segmentation.Segment;
import pl.edu.pjwstk.kaldi.utils.Log;

public class ClarinText {

	public class ClarinPhoneme {
		String phoneme;
		double start_time;
		double end_time;
	}

	public class ClarinWord {

		String id;
		String word;

		boolean recognizable = true;

		double start_time;
		double end_time;
		Vector<ClarinPhoneme> phoneme;
	}

	String id;
	ClarinWord[] words;

	Vector<ClarinWord> words_xml;

	public String toString() {
		String ret = "";
		for (ClarinWord word : words) {
			if (word.recognizable) {
				if (ret.isEmpty())
					ret += word.word;
				else
					ret += " " + word.word;
			}
		}
		return ret;
	}

	public void saveText(File file) throws IOException {
		PrintWriter writer = new PrintWriter(file);
		boolean first = true;
		for (int i = 0; i < words.length; i++) {
			if (words[i].recognizable) {
				if (!first)
					writer.print(" ");
				else
					first = false;
				writer.print(words[i].word);
			}
		}
		writer.println();
		writer.close();
	}

	public void saveXML(File file) throws IOException,
			ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException {

		if (words_xml == null)
			return;

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();

		Element elRoot = doc.createElement("audio-segment");
		elRoot.setAttribute("id", id);
		doc.appendChild(elRoot);

		for (ClarinWord word : words_xml) {
			Element elWord = doc.createElement("word");
			elWord.setAttribute("id", word.id);
			elWord.setAttribute("beg", String.format("%2.3f", word.start_time));
			elWord.setAttribute("end", String.format("%2.3f", word.end_time));
			elWord.setAttribute("word", word.word);
			elRoot.appendChild(elWord);

			if (word.phoneme != null) {
				for (ClarinPhoneme phone : word.phoneme) {
					Element elPhoneme = doc.createElement("phoneme");
					elPhoneme.setAttribute("beg",
							String.format("%2.3f", phone.start_time));
					elPhoneme.setAttribute("end",
							"" + String.format("%2.3f", phone.end_time));
					elPhoneme.setTextContent(phone.phoneme);
					elWord.appendChild(elPhoneme);
				}
			}
		}

		Transformer trans = TransformerFactory.newInstance().newTransformer();

		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
				"4");
		trans.setOutputProperty(OutputKeys.METHOD, "xml");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");

		Source source = new DOMSource(doc);
		Result result = new StreamResult(file);
		trans.transform(source, result);

	}

	public int size() {
		return words.length;
	}

	public void checkWords() {
		Pattern p = Pattern.compile("[a-zA-ZąĄćĆęĘłŁńŃoÓśŚźŹżŻ]+");
		for (ClarinWord w : words) {
			Matcher m = p.matcher(w.word);
			if (!m.matches())
				w.recognizable = false;
			else
				w.recognizable = true;
		}
	}

	public void processSegmentation(Segmentation segmentation)
			throws RuntimeException {

		words_xml = new Vector<ClarinWord>();

		List<Segment> wseg = segmentation.tiers.get(0).segments;
		List<Segment> pseg = segmentation.tiers.get(1).segments;

		int i = 0;
		for (Segment w : wseg) {

			while (i < words.length && !words[i].recognizable)
				i++;

			if (i >= words.length)
				break;

			while (i < words.length && !w.name.equals(words[i].word)) {
				Log.warn("Deletion! Fixing...");
				i++;
			}

			if (i >= words.length)
				break;

			words[i].start_time = w.start_time;
			words[i].end_time = w.end_time;
			words[i].phoneme = new Vector<ClarinPhoneme>();
			words_xml.add(words[i]);

			for (Segment p : pseg) {
				if (p.start_time - w.start_time > -0.001
						&& p.end_time - w.end_time < 0.001) {
					ClarinPhoneme ph = new ClarinPhoneme();
					ph.phoneme = p.name;
					ph.start_time = p.start_time;
					ph.end_time = p.end_time;
					words[i].phoneme.add(ph);
				}

				double dist = p.start_time - w.end_time;
				if (dist < 0.001 && dist > -0.001 && p.name.equals("sil")) {
					ClarinWord sil = new ClarinWord();
					sil.start_time = p.start_time;
					sil.end_time = p.end_time;
					sil.id = "n/a";
					sil.word = "SIL";
					sil.phoneme = null;
					words_xml.add(sil);
				}
			}

			i++;
		}
	}
}
