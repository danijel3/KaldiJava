package pl.edu.pjwstk.kaldi.service.database;

import pl.edu.pjwstk.kaldi.service.database.Database.Pair;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;

public class dbTasks {

    public enum dbStatus {
        queued, running, done, dead, copyof
    }

    private static DateFormat df = DateFormat.getDateTimeInstance();

    public static class Task {

        public int _id;
        public dbStatus status = dbStatus.queued;
        public String task_file;
        public int pid;
        public String hash;
        public Timestamp time;
        public int login_id;
        public String host;
        public int copyid;

        public String toString() {
            return "(" + _id + ") " + status + " -- " + task_file + " -- "
                    + pid + " -- " + hash + " -- " + df.format(time) + " -- "
                    + login_id + " -- " + host;
        }
    }

    public static Task getOldestQueued() throws RuntimeException {

        try {

            String options = Database.whereAnd(new Pair[]{new Pair("status",
                    "queued")});
            options += " " + Database.order("time", true);
            options += " " + Database.limit(1);

            Object[] ret = Database.get(Task.class, options);

            if (ret.length == 0)
                return null;
            else
                return (Task) ret[0];

        } catch (InstantiationException | IllegalAccessException | SQLException
                | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Task[] getAllRunning() throws RuntimeException {
        try {
            String options = Database.whereAnd(new Pair[]{new Pair("status",
                    "running")});
            options += " " + Database.order("time", true);

            Object[] obj = Database.get(Task.class, options);

            Task[] ret = new Task[obj.length];
            for (int i = 0; i < ret.length; i++)
                ret[i] = (Task) obj[i];

            return ret;

        } catch (InstantiationException | IllegalAccessException | SQLException
                | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changeStatus(Task task, dbStatus status)
            throws RuntimeException {
        try {
            Database.update("Task", "status", status.name(), "_id", task._id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void changePID(Task task, int pid) throws RuntimeException {
        try {
            Database.updateInt("Task", "pid", pid, "_id", task._id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setHash(Task task, String hash) throws RuntimeException {
        try {
            Database.update("Task", "hash", hash, "_id", task._id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void setCopy(Task task, int copy) throws RuntimeException {
        try {
            Database.updateInt("Task", "copyid", copy, "_id", task._id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static Task getByHash(String hash) throws RuntimeException {
        try {

            String options = Database
                    .whereAnd(new Pair[]{new Pair("status", "copyof", true),
                            new Pair("status", "dead", true),
                            new Pair("hash", hash)});
            options += " " + Database.order("time", true);
            options += " " + Database.limit(1);

            Object[] ret = Database.get(Task.class, options);

            if (ret.length == 0)
                return null;
            else
                return (Task) ret[0];

        } catch (InstantiationException | IllegalAccessException | SQLException
                | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Task getByID(int id) throws RuntimeException {
        try {

            String options = Database.whereAnd(new Pair[]{new Pair("_id", ""
                    + id, false, false)});

            Object[] ret = Database.get(Task.class, options);

            if (ret.length == 0)
                return null;
            else
                return (Task) ret[0];

        } catch (InstantiationException | IllegalAccessException | SQLException
                | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        try {
            Task t = getOldestQueued();
            System.out.println("1>" + t);

            Task[] t2 = getAllRunning();
            for (Task t3 : t2) {
                System.out.println("2>" + t3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
