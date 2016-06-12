package pl.edu.pjwstk.kaldi.service.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

public abstract class Task implements Runnable {

	public static enum State {
		INITIALIZED, RUNNING, FAILED, SUCCEEDED
	}

	public State state = State.INITIALIZED;

	public abstract void loadSettings(XPath xpath, Element node) throws XPathExpressionException;

	public abstract void updateHash(MessageDigest m) throws IOException;

	protected static Task getTask(String name) {

		if (name.equals("test")) {
			return new TestTask();
		}

		if (name.equals("decode")) {
			return new DecodeTask();
		}

		if (name.equals("align")) {
			return new AlignTask();
		}

		if (name.equals("speaker-diarization")) {
			return new SpeakerDiarizationTask();
		}

		if (name.equals("convert-encoding")) {
			return new ConvertEncodingTask();
		}

		if (name.equals("nser")) {
			return new NSERTask();
		}

		if (name.equals("kws")) {
			return new KeywordSpottingTask();
		}

		Log.error("Unknown task: " + name);
		return null;
	}

	public static void run(File task_config)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, RuntimeException {

		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(task_config);

		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList tasks = (NodeList) xpath.evaluate("/tasks/task", doc, XPathConstants.NODESET);

		for (int i = 0; i < tasks.getLength(); i++) {
			Element elTask = (Element) tasks.item(i);

			String name = elTask.getAttribute("name");

			Task task = getTask(name);

			task.loadSettings(xpath, elTask);

			task.run();

			if (task.state != State.SUCCEEDED) {
				throw new RuntimeException("Failed to complete task!");
			}
		}
	}

	public static String getHash(File task_config) throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException, NoSuchAlgorithmException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(task_config);

		XPath xpath = XPathFactory.newInstance().newXPath();

		NodeList tasks = (NodeList) xpath.evaluate("/tasks/task", doc, XPathConstants.NODESET);

		MessageDigest m = MessageDigest.getInstance("MD5");

		for (int i = 0; i < tasks.getLength(); i++) {
			Element elTask = (Element) tasks.item(i);

			String name = elTask.getAttribute("name");

			Task task = getTask(name);

			if (task == null)
				continue;

			m.update(name.getBytes(Settings.default_encoding));

			task.loadSettings(xpath, elTask);

			try {
				task.updateHash(m);
			} catch (IOException e) {
				// IGNORE MISSING FILES IN TASKS THAT HAVEN'T CREATED THEM
				// YET...
			}
		}

		byte[] d = m.digest();
		BigInteger bigInt = new BigInteger(1, d);
		String hashstr = bigInt.toString(16);
		while (hashstr.length() < 32) {
			hashstr = "0" + hashstr;
		}

		return hashstr;
	}

	protected static void processFileHash(MessageDigest m, File f) throws IOException {

		FileInputStream stream = new FileInputStream(f);
		FileChannel chan = stream.getChannel();

		ByteBuffer bb = ByteBuffer.allocate(512);

		while (true) {
			int ret = chan.read(bb);
			if (ret < 0)
				break;
			m.update(bb);
			bb.rewind();
		}

		chan.close();
		stream.close();
	}

	public static void main(String[] args) {
		try {
			System.out.println(getHash(new File("/var/www/html/mowa/tasks/task_20150501_225120/config.xml")));
		} catch (XPathExpressionException | NoSuchAlgorithmException | SAXException | IOException
				| ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
