package pl.edu.pjwstk.kaldi.service.tasks;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import pl.edu.pjwstk.kaldi.programs.Python;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class NSERTask extends Task {

	File input_file;

	@Override
	public void run() {

		File output_file = new File(Settings.curr_task_dir, "out.TextGrid");
		File script = new File(Settings.python_scripts_dir,
				"NSER/pjatk/Main.py");
		File model = new File(Settings.python_scripts_dir,
				"NSER/svc_model_prob.pklz");
		File hcopy = new File(Settings.python_scripts_dir, "NSER/hcopy.conf");
		File sd_dir = new File(Settings.python_scripts_dir, "NSER/SD");

		String args[] = { "--tmp", Settings.curr_task_dir.getAbsolutePath(),
				"--sd", sd_dir.getAbsolutePath(), "--model",
				model.getAbsolutePath(), "--hcopy_conf",
				hcopy.getAbsolutePath(), input_file.getAbsolutePath(),
				output_file.getAbsolutePath() };

		state = State.RUNNING;
		Log.info("Starting test task...");

		Python.run(script, args);

		if (!output_file.exists())
			state = State.FAILED;
		else
			state = State.SUCCEEDED;
		Log.info("Completed!");

	}

	@Override
	public void loadSettings(XPath xpath, Element node)
			throws XPathExpressionException {

		String input_file_name = (String) xpath.evaluate("input-file", node,
				XPathConstants.STRING);
		input_file = new File(input_file_name);

	}

	@Override
	public void updateHash(MessageDigest m) throws IOException {
		processFileHash(m, input_file);
	}
}
