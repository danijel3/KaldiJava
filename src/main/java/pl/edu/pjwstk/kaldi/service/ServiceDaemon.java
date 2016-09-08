package pl.edu.pjwstk.kaldi.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import pl.edu.pjwstk.kaldi.programs.Java;
import pl.edu.pjwstk.kaldi.service.database.dbTasks;
import pl.edu.pjwstk.kaldi.service.database.dbTasks.dbStatus;
import pl.edu.pjwstk.kaldi.service.tasks.Task;
import pl.edu.pjwstk.kaldi.utils.Log;
import pl.edu.pjwstk.kaldi.utils.ParseOptions;
import pl.edu.pjwstk.kaldi.utils.Settings;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;

public class ServiceDaemon {

    static File settings_file = null;

    static HashMap<String, String> cache_map = new HashMap<>();

    public static void main(String[] args) {
        try {

            Locale.setDefault(Locale.ENGLISH);

            Log.initFile("KaldiServiceDaemon", true);
            Log.setLevel(Level.INFO);

            ParseOptions po = new ParseOptions("Kaldi Service Daemon", "Service daemon for Java.");

            po.addArgument("settings", 's', File.class, "Load program settings from a file", null);
            po.addArgument("dump-settings", 'd', File.class, "Save default program settings to a file", null);
            po.addArgument("background", 'b', Boolean.class, "Run program in background", "false");

            if (!po.parse(args))
                return;

            if (po.getArgument("dump-settings") != null) {
                Log.info("Dumping settings and exitting.");
                Settings.dumpSettings((File) po.getArgument("dump-settings"));
                return;
            }

            if (po.getArgument("settings") != null) {
                Log.info("Loading settings...");
                settings_file = (File) po.getArgument("settings");
                Settings.loadSettings(settings_file);
            }

            try {
                BufferedReader reader = new BufferedReader(new FileReader(Settings.daemon_pid));
                int iPid = Integer.parseInt(reader.readLine());
                reader.close();
                File proc = new File("/proc/" + iPid + "/cmdline");
                if (proc.exists()) {
                    System.out.println("Daemon process is already running @pid: " + iPid);
                    System.out.println("Exitting...");
                    System.exit(0);
                }
            } catch (Exception e) {
            }

            if ((Boolean) po.getArgument("background")) {
                System.out.println("Running Kaldi Java Service Daemon in background...");
                runSelf();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                System.exit(0);
            }

            PrintWriter writer = new PrintWriter(Settings.daemon_pid);
            writer.println(new File("/proc/self").getCanonicalFile().getName());
            writer.close();

        } catch (Exception e) {
            System.out.println("ERROR");
            e.printStackTrace(System.out);
            return;
        }

        while (true) {

            dbTasks.Task running_tasks[] = dbTasks.getAllRunning();

            long now = new Date().getTime();

            int slots_used = 0;
            for (dbTasks.Task t : running_tasks) {
                if (t.pid <= 0 && (now - t.time.getTime()) > Settings.daemon_startup_timer_ms) {
                    Log.info("Task " + t._id + " never started and is stale!");
                    dbTasks.changeStatus(t, dbStatus.dead);
                    continue;
                }

                if (t.pid > 0 && !checkPIDProc(t.pid)) {
                    Log.info("Task " + t._id + " died!");
                    dbTasks.changeStatus(t, dbStatus.dead);
                    continue;
                }

                slots_used++;
            }

            saveStatus(slots_used);

            if (slots_used >= Settings.daemon_slots) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                continue;
            }

            dbTasks.Task queued_task = dbTasks.getOldestQueued();

            if (queued_task == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                continue;
            }

            String hash = "";
            try {
                hash = Task.getHash(new File(queued_task.task_file));
                Log.verbose("Task hash: " + hash);
            } catch (XPathExpressionException | NoSuchAlgorithmException | SAXException | ParserConfigurationException
                    | IOException | NullPointerException e1) {
                Log.error("Getting hash", e1);
                dbTasks.changeStatus(queued_task, dbStatus.dead);
                continue;
            }

            dbTasks.Task copy = dbTasks.getByHash(hash);

            dbTasks.setHash(queued_task, hash);

            if (copy != null) {

                Log.info("Found cached: " + copy._id);

                dbTasks.changeStatus(queued_task, dbStatus.copyof);
                dbTasks.setCopy(queued_task, copy._id);

                continue;
            }

            Log.info("Starting task " + queued_task._id);
            dbTasks.changeStatus(queued_task, dbStatus.running);

            try {
                run(queued_task);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

        }

    }

    public static void run(dbTasks.Task task) throws IOException {

        LinkedList<File> cp_files = new LinkedList<>();
        for (File f : Settings.java_lib_dir.listFiles()) {
            if (f.getName().endsWith(".jar"))
                cp_files.add(f);
        }

        String args[];
        if (settings_file != null)
            args = new String[]{"-s", settings_file.getAbsolutePath(), "" + task._id};
        else
            args = new String[]{"" + task._id};

        Java.java("pl.edu.pjwstk.kaldi.service.ServiceTask", args, cp_files, true);

    }

    public static void runSelf() throws IOException {

        LinkedList<File> cp_files = new LinkedList<>();
        for (File f : Settings.java_lib_dir.listFiles()) {
            if (f.getName().endsWith(".jar"))
                cp_files.add(f);
        }

        String args[];
        if (settings_file != null)
            args = new String[]{"-s", settings_file.getAbsolutePath()};
        else
            args = new String[]{};

        Java.java("pl.edu.pjwstk.kaldi.service.ServiceDaemon", args, cp_files, true);

    }

    private static DocumentBuilder docBuilder = null;
    private static TransformerFactory transfac = TransformerFactory.newInstance();
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public static void saveStatus(int slots_used) {

        try {
            if (docBuilder == null)
                docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element root = doc.createElement("status");

            Element time = doc.createElement("time");
            Element timestamp = doc.createElement("timestamp");
            Element slots = doc.createElement("slots-used");

            Date now = new Date();

            time.setTextContent(sdf.format(now));
            timestamp.setTextContent("" + (now.getTime() / 1000L));
            slots.setTextContent("" + slots_used);

            root.appendChild(time);
            root.appendChild(timestamp);
            root.appendChild(slots);
            doc.appendChild(root);

            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            Source source = new DOMSource(doc);
            Result result = new StreamResult(Settings.daemon_status);
            trans.transform(source, result);

        } catch (ParserConfigurationException | TransformerException e) {
        }
    }

    private static boolean checkPIDProc(int pid) {

        File proc_file = new File("/proc/" + pid);

        return proc_file.exists();
    }
}
