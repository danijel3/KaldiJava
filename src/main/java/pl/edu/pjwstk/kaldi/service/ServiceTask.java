package pl.edu.pjwstk.kaldi.service;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import pl.edu.pjwstk.kaldi.programs.KaldiKWS;
import pl.edu.pjwstk.kaldi.programs.KaldiScripts;
import pl.edu.pjwstk.kaldi.programs.KaldiUtils;
import pl.edu.pjwstk.kaldi.programs.NGram;
import pl.edu.pjwstk.kaldi.programs.Shout;
import pl.edu.pjwstk.kaldi.programs.Transcriber;
import pl.edu.pjwstk.kaldi.service.database.dbTasks;
import pl.edu.pjwstk.kaldi.service.database.dbTasks.dbStatus;
import pl.edu.pjwstk.kaldi.service.tasks.Task;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ParseOptions;
import pl.edu.pjwstk.kaldi.utils.Settings;

public class ServiceTask {

	public static void main(String[] args) {

		dbTasks.Task db_task = null;

		try {

			Locale.setDefault(Locale.ENGLISH);

			ParseOptions po = new ParseOptions("Kaldi Task", "Task runner for Kaldi Service.");

			po.addArgument(Integer.class, "task_id", "Database ID of the given task.");

			po.addArgument("settings", 's', File.class, "Load program settings from a file", null);
			po.addArgument("restart", 'r', Boolean.class, "Restart if task already exists!", "false");

			if (!po.parse(args))
				return;

			if (po.getArgument("settings") != null) {
				Settings.loadSettings((File) po.getArgument("settings"));
			}

			int id = (Integer) po.getArgument(0);

			db_task = dbTasks.getByID(id);

			try {
				int pid = Integer.parseInt(new File("/proc/self").getCanonicalFile().getName());
				dbTasks.changePID(db_task, pid);
			} catch (NumberFormatException | IOException | SecurityException e) {
				throw new RuntimeException("Cannot get PID!", e);
			}

			File task_file = new File(db_task.task_file);
			File task_dir = new File(task_file.getParent(), "task");

			if (task_dir.exists()) {
				if ((Boolean) po.getArgument("restart")) {
					File newname = File.createTempFile("task", "bak", task_dir.getParentFile());
					newname.delete();
					task_dir.renameTo(newname);
				} else
					throw new RuntimeException("Task dir already exists!");
			}

			task_dir.mkdirs();

			Settings.curr_task_dir = task_dir;
			Settings.log_dir = Settings.curr_task_dir;
			Settings.temp_dir = new File(Settings.curr_task_dir, "tmp");
			Settings.temp_dir2 = new File(Settings.curr_task_dir, "tmp2");

			Log.initFile("KaldiTask", true);

			KaldiUtils.init();
			KaldiUtils.test();
			KaldiScripts.init(Settings.curr_task_dir);
			KaldiScripts.test();
			Shout.test();
			Transcriber.init();
			Transcriber.test();
			NGram.test_srilm();
			KaldiKWS.test();

			Task.run(task_file);

			dbTasks.changeStatus(db_task, dbStatus.done);

		} catch (Exception e) {
			Log.error("Main.", e);
			if (db_task != null)
				dbTasks.changeStatus(db_task, dbStatus.dead);
		}
	}
}
