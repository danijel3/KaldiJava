package pl.edu.pjwstk.kaldi.files;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

public class EAF extends Segmentation {

	private DocumentBuilder db;
	private XPath xp = XPathFactory.newInstance().newXPath();
	private String author = "Unknown";
	private File media_file = null;

	public EAF() throws ParserConfigurationException {
		db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	public EAF(Segmentation segmentation) throws ParserConfigurationException {
		this();
		this.tiers = segmentation.tiers;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setMediaFile(File media_file) {
		this.media_file = media_file;
	}

	@Override
	public void read(File file) throws IOException {

		try {
			Document doc = db.parse(file);

			NodeList nlTimeSlots = (NodeList) xp.evaluate(
					"/ANNOTATION_DOCUMENT/TIME_ORDER/TIME_SLOT", doc,
					XPathConstants.NODESET);

			HashMap<String, Double> timeSlots = new HashMap<String, Double>();

			for (int i = 0; i < nlTimeSlots.getLength(); i++) {
				Element timeSlot = (Element) nlTimeSlots.item(i);
				String id = timeSlot.getAttribute("TIME_SLOT_ID");
				int time = Integer
						.parseInt(timeSlot.getAttribute("TIME_VALUE"));
				timeSlots.put(id, time / 1000.0);
			}

			String txt;
			NodeList nlSegments = (NodeList) xp
					.evaluate(
							"/ANNOTATION_DOCUMENT/TIER/ANNOTATION/ALIGNABLE_ANNOTATION",
							doc, XPathConstants.NODESET);

			double segment_start, segment_end;
			String segment_text;

			for (int i = 0; i < nlSegments.getLength(); i++) {
				Element elSegment = (Element) nlSegments.item(i);

				txt = elSegment.getAttribute("TIME_SLOT_REF1");
				if (!timeSlots.containsKey(txt))
					throw new RuntimeException("Missing time slot: " + txt);
				segment_start = timeSlots.get(txt);

				txt = elSegment.getAttribute("TIME_SLOT_REF2");
				if (!timeSlots.containsKey(txt))
					throw new RuntimeException("Missing time slot: " + txt);
				segment_end = timeSlots.get(txt);

				segment_text = (String) xp.evaluate("ANNOTATION_VALUE",
						elSegment, XPathConstants.STRING);
				segment_text = segment_text.toLowerCase()
						.replaceAll("[\\p{Punct}]+", " ")
						.replaceAll("\\s+", " ").trim();

				addSegment(0, segment_start, segment_end, segment_text);
			}
		} catch (ParseException | SAXException | XPathExpressionException e) {
			throw new IOException(e);
		}
	}

	private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-DD'T'HH:mm+ss:SS");

	@Override
	public void write(File file) throws IOException {

		Document doc = db.newDocument();

		Element elAnnotationDoc = doc.createElement("ANNOTATION_DOCUMENT");
		elAnnotationDoc.setAttribute("AUTHOR", author);
		elAnnotationDoc.setAttribute("DATE", sdf.format(new Date()));
		elAnnotationDoc.setAttribute("FORMAT", "2.8");
		elAnnotationDoc.setAttribute("VERSION", "2.8");
		elAnnotationDoc.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		elAnnotationDoc.setAttribute("xsi:noNamespaceSchemaLocation",
				"http://www.mpi.nl/tools/elan/EAFv2.8.xsd");

		doc.appendChild(elAnnotationDoc);

		Element elHeader = doc.createElement("HEADER");
		elHeader.setAttribute("MEDIA_FILE", "");
		elHeader.setAttribute("TIME_UNITS", "milliseconds");

		elAnnotationDoc.appendChild(elHeader);

		if (media_file != null) {
			Element elMedia = doc.createElement("MEDIA_DESCRIPTOR");
			elMedia.setAttribute("MEDIA_URL", media_file.toURI().toURL()
					.toString());
			elMedia.setAttribute("MIME_TYPE", "audio/x-wav");
			elMedia.setAttribute("RELATIVE_MEDIA_URL", file.getParentFile()
					.toPath().relativize(media_file.toPath()).toString());

			elHeader.appendChild(elMedia);
		}

		Element elProperty1 = doc.createElement("PROPERTY");
		elProperty1.setAttribute("NAME", "URN");
		elProperty1.setTextContent("urn:nl-mpi-tools-elan-eaf:"
				+ this.hashCode());

		elHeader.appendChild(elProperty1);

		int sum = 0;
		for (Tier t : tiers) {
			sum += t.segments.size();
		}

		Element elProperty2 = doc.createElement("PROPERTY");
		elProperty2.setAttribute("NAME", "lastUsedAnnotationId");
		elProperty2.setTextContent("" + sum);

		elHeader.appendChild(elProperty2);

		HashMap<Double, String> timeOrder = new HashMap<Double, String>();

		int tsCounter = 1;
		for (Tier t : tiers)
			for (Segment s : t.segments) {
				if (!timeOrder.containsKey(s.start_time)) {
					timeOrder.put(s.start_time, "ts" + tsCounter);
					tsCounter++;
				}
				if (!timeOrder.containsKey(s.end_time)) {
					timeOrder.put(s.end_time, "ts" + tsCounter);
					tsCounter++;
				}
			}

		Element elTimeOrder = doc.createElement("TIME_ORDER");

		elAnnotationDoc.appendChild(elTimeOrder);

		for (Entry<Double, String> e : timeOrder.entrySet()) {
			Element elTimeSlot = doc.createElement("TIME_SLOT");
			elTimeSlot.setAttribute("TIME_SLOT_ID", e.getValue());
			elTimeSlot.setAttribute("TIME_VALUE", ""
					+ (int) (e.getKey() * 1000.0));

			elTimeOrder.appendChild(elTimeSlot);
		}

		int annotationId = 1;
		int tierId = 1;
		for (Tier t : tiers) {
			Element elTier = doc.createElement("TIER");
			elTier.setAttribute("LINGUISTIC_TYPE_REF", "praat");
			elTier.setAttribute("TIER_ID", "tier-" + tierId);
			tierId++;

			elAnnotationDoc.appendChild(elTier);

			for (Segment s : t.segments) {
				Element elAnnotation = doc.createElement("ANNOTATION");
				elTier.appendChild(elAnnotation);

				Element elAlignableAnnotation = doc
						.createElement("ALIGNABLE_ANNOTATION");
				elAlignableAnnotation.setAttribute("ANNOTATION_ID", "a"
						+ annotationId);
				annotationId++;
				elAlignableAnnotation.setAttribute("TIME_SLOT_REF1",
						timeOrder.get(s.start_time));
				elAlignableAnnotation.setAttribute("TIME_SLOT_REF2",
						timeOrder.get(s.end_time));

				elAnnotation.appendChild(elAlignableAnnotation);

				Element elAnnotationValue = doc
						.createElement("ANNOTATION_VALUE");
				elAnnotationValue.setTextContent(s.name);

				elAlignableAnnotation.appendChild(elAnnotationValue);
			}
		}

		Element elLinguisticType = doc.createElement("LINGUISTIC_TYPE");
		elLinguisticType.setAttribute("GRAPHIC_REFERENCES", "false");
		elLinguisticType.setAttribute("LINGUISTIC_TYPE_ID", "praat");
		elLinguisticType.setAttribute("TIME_ALIGNABLE", "true");

		elAnnotationDoc.appendChild(elLinguisticType);

		Element elConstraint1 = doc.createElement("CONSTRAINT");
		elConstraint1
				.setAttribute(
						"DESCRIPTION",
						"Time subdivision of parent annotation's time interval, no time gaps allowed within this interval");
		elConstraint1.setAttribute("STEREOTYPE", "Time_Subdivision");

		elAnnotationDoc.appendChild(elConstraint1);

		Element elConstraint2 = doc.createElement("CONSTRAINT");
		elConstraint2
				.setAttribute(
						"DESCRIPTION",
						"Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered");
		elConstraint2.setAttribute("STEREOTYPE", "Symbolic_Subdivision");

		elAnnotationDoc.appendChild(elConstraint2);

		Element elConstraint3 = doc.createElement("CONSTRAINT");
		elConstraint3.setAttribute("DESCRIPTION",
				"1-1 association with a parent annotation");
		elConstraint3.setAttribute("STEREOTYPE", "Symbolic_Association");

		elAnnotationDoc.appendChild(elConstraint3);

		Element elConstraint4 = doc.createElement("CONSTRAINT");
		elConstraint4
				.setAttribute(
						"DESCRIPTION",
						"Time alignable annotations within the parent annotation's time interval, gaps are allowed");
		elConstraint4.setAttribute("STEREOTYPE", "Included_In");

		elAnnotationDoc.appendChild(elConstraint4);

		try {
			Transformer trans = TransformerFactory.newInstance()
					.newTransformer();

			trans.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			Source source = new DOMSource(doc);
			Result result = new StreamResult(file);
			trans.transform(source, result);

		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) {

		File dir = new File("clarin");

		System.out.println("Converting all TextGrids in " + dir.getPath()
				+ " to EAF...");

		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".TextGrid")) {

				String name = file.getName();
				name = name.substring(0, name.length() - 8);
				File eaf_file = new File(dir, name + "eaf");

				System.out
						.println(file.getPath() + " -> " + eaf_file.getPath());

				TextGrid text_grid = new TextGrid();

				try {
					text_grid.read(file);
				} catch (IOException | RuntimeException e) {
					System.err.println("Error reading " + file.getPath());
					e.printStackTrace();
					continue;
				}

				try {
					EAF eaf = new EAF(text_grid);

					eaf.setAuthor("danijel");
					eaf.setMediaFile(new File(dir, name + "wav"));

					eaf.write(eaf_file);
				} catch (IOException | ParserConfigurationException e) {
					System.err.println("Error writing " + eaf_file.getPath());
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done all!");
	}
}
