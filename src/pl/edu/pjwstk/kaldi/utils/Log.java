package pl.edu.pjwstk.kaldi.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    public static class Stream extends OutputStream {

        private StringBuffer str = new StringBuffer();

        private String prefix = "OUT>>";

        public Stream() {
        }

        public Stream(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void write(int b) throws IOException {
            if (b == '\n' || b == '\r') {
                Log.verbose(prefix + str.toString());
                str.setLength(0);
            } else
                str.append((char) b);
        }

    }

    private static class FileLog extends Handler {

        private PrintWriter fileWriter;

        public FileLog(String prog_name, boolean append)
                throws FileNotFoundException {

            this(new File(Settings.log_dir, prog_name + "_main.log"), append);
        }

        public FileLog(File logFile, boolean append)
                throws FileNotFoundException {

            Settings.log_dir.mkdirs();

            fileWriter = new PrintWriter(new FileOutputStream(logFile, append));
        }

        @Override
        public void close() throws SecurityException {
            fileWriter.close();

        }

        @Override
        public void flush() {
            fileWriter.flush();
        }

        private SimpleDateFormat sdf = new SimpleDateFormat(
                "[yyyy-MM-dd HH:mm:ss]");

        @Override
        public void publish(LogRecord lr) {

            String loglevel = lr.getLevel().getName();
            if (lr.getLevel() == Level.SEVERE)
                loglevel = "ERROR";

            String line = sdf.format(new Date(lr.getMillis())) + " <"
                    + loglevel + "> " + lr.getMessage();

            fileWriter.println(line);
            fileWriter.flush();

            Throwable th = lr.getThrown();
            if (th != null) {
                fileWriter.println("Exception: " + th.toString());

                fileWriter.println("Stack trace:");

                for (StackTraceElement el : th.getStackTrace()) {
                    fileWriter.println("**" + el.getFileName() + ":"
                            + el.getLineNumber() + " in " + el.getClassName()
                            + ":" + el.getMethodName());
                }

                fileWriter.flush();
            }

        }
    }

    public static class SimpleConsoleLog extends Handler {

        @Override
        public void close() throws SecurityException {
        }

        @Override
        public void flush() {
            System.out.flush();
            System.err.flush();
        }

        private SimpleDateFormat sdf = new SimpleDateFormat(
                "[yyyy-MM-dd HH:mm:ss]");

        @Override
        public void publish(LogRecord lr) {

            String loglevel = lr.getLevel().getName();
            if (lr.getLevel() == Level.SEVERE)
                loglevel = "ERROR";

            String line = sdf.format(new Date(lr.getMillis())) + " <"
                    + loglevel + "> " + lr.getMessage();

            if (lr.getLevel() == Level.SEVERE)
                System.err.println(line);
            else
                System.out.println(line);

            Throwable th = lr.getThrown();
            if (th != null) {
                th.printStackTrace();
            }

        }

    }

    private static Logger logger = null;
    private static boolean suppress_output = false;

    public static void init(String prog_name, boolean append)
            throws SecurityException, FileNotFoundException {

        initFile(prog_name, append);

        logger.addHandler(new SimpleConsoleLog());
    }

    public static void initFile(String prog_name, boolean append)
            throws SecurityException, FileNotFoundException {

        logger = Logger.getLogger(Log.class.getPackage().getName());

        logger.setUseParentHandlers(false);

        logger.addHandler(new FileLog(prog_name, append));

        logger.setLevel(Level.ALL);
    }

    public static void setLevel(Level level) {
        if (logger != null)
            logger.setLevel(level);
    }

    public static void  disableOutput() {
        suppress_output=true;
    }

    public static void  enableOutput() {
        suppress_output=false;
    }

    public static void verbose(String message) {

        if(suppress_output) return;

        if (logger == null) {
            System.out.println("V:" + message);
            return;
        }

        logger.log(Level.FINE, message);
    }

    public static void info(String message) {

        if(suppress_output) return;

        if (logger == null) {
            System.out.println("I:" + message);
            return;
        }

        logger.log(Level.INFO, message);
    }

    public static void warn(String message) {

        if(suppress_output) return;

        if (logger == null) {
            System.out.println("W:" + message);
            return;
        }

        logger.log(Level.WARNING, message);
    }

    public static void error(String message) {

        if(suppress_output) return;

        if (logger == null) {
            System.out.println("E:" + message);
            return;
        }

        logger.log(Level.SEVERE, message);
    }

    public static void error(String message, Throwable e) {

        if(suppress_output) return;

        if (logger == null) {
            System.out.println("E:" + message);
            e.printStackTrace();
            return;
        }

        logger.log(Level.SEVERE, message, e);
    }

    public static void addHandler(Handler h) {
        if (logger != null)
            logger.addHandler(h);
    }

}
