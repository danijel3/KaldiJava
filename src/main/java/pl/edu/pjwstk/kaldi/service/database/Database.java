package pl.edu.pjwstk.kaldi.service.database;

import pl.edu.pjwstk.kaldi.utils.Settings;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class Database {
    
    private static Connection conn = null;

    private static Properties db_props = new Properties();

    static {
        db_props.put("user", Settings.db_username);
        db_props.put("password", Settings.db_password);
    }

    public static Connection get() throws SQLException {
        if (conn == null || !conn.isValid(1)) {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/",
                    db_props);
        }
        return conn;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object[] get(Class<?> type, String options)
            throws InstantiationException, IllegalAccessException,
            SQLException, RuntimeException {

        String tablename = type.getSimpleName();

        HashMap<String, Field> fields = new HashMap<>();
        for (Field f : type.getFields()) {
            fields.put(f.getName(), f);
        }

        String fieldlist = "";
        for (String f : fields.keySet()) {
            if (!fieldlist.isEmpty())
                fieldlist += ",";
            fieldlist += "`" + f + "`";
        }

        String query = "SELECT " + fieldlist + " FROM `" + Settings.db_name
                + "`.`" + tablename + "`";
        if (!options.isEmpty())
            query += " " + options;

        // System.out.println(query);

        Connection db = get();

        ResultSet rs = db.createStatement().executeQuery(query);

        ArrayList<Object> ret = new ArrayList<>();
        while (rs.next()) {
            Object obj = type.newInstance();

            for (Entry<String, Field> e : fields.entrySet()) {
                Field f = e.getValue();
                String n = e.getKey();

                String t = f.getType().getName();
                if (t.equals("int")) {
                    int value = rs.getInt(n);
                    f.setInt(obj, value);
                } else if (t.equals("long")) {
                    long value = rs.getLong(n);
                    f.setLong(obj, value);

                } else if (t.equals("java.sql.Timestamp")) {
                    Timestamp value = rs.getTimestamp(n);
                    f.set(obj, value);
                } else if (t.equals("java.lang.String")) {
                    String value = rs.getString(n);
                    f.set(obj, value);
                } else if (f.getType().isEnum()) {
                    String value = rs.getString(n);

                    f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
                } else {
                    throw new RuntimeException(
                            "Unknown or unimplemented type: " + t);
                }

            }

            ret.add(obj);
        }

        return ret.toArray();
    }

    public static class Pair {

        public String key;
        public String value;
        public boolean not;
        public boolean quoted;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
            this.not = false;
            this.quoted = true;
        }

        public Pair(String key, String value, boolean not) {
            this.key = key;
            this.value = value;
            this.not = not;
            this.quoted = true;
        }

        public Pair(String key, String value, boolean not, boolean quoted) {
            this.key = key;
            this.value = value;
            this.not = not;
            this.quoted = quoted;
        }
    }

    public static String whereAnd(Pair values[]) {
        String ret = "";
        String sign;

        for (Pair p : values) {

            if (p.not)
                sign = "!=";
            else
                sign = "=";

            if (!ret.isEmpty())
                ret += " AND ";
            if (p.quoted)
                ret += "`" + p.key + "` " + sign + " '" + p.value + "'";
            else
                ret += "`" + p.key + "` " + sign + " " + p.value;
        }
        return "WHERE " + ret;
    }

    public static String limit(int num) {
        return "LIMIT " + num;
    }

    public static String order(String column, boolean asc) {
        if (asc)
            return "ORDER BY `" + column + "` ASC";
        else
            return "ORDER BY `" + column + "` DESC";
    }

    public static void update(String table, String column, String value,
                              String id_col, int id_val) throws SQLException {
        String query = "UPDATE `" + Settings.db_name + "`.`" + table
                + "` SET `" + column + "` = '" + value + "' WHERE `" + id_col
                + "`=" + id_val;

        Connection db = get();
        db.createStatement().execute(query);
    }

    public static void updateInt(String table, String column, int value,
                                 String id_col, int id_val) throws SQLException {
        String query = "UPDATE `" + Settings.db_name + "`.`" + table
                + "` SET `" + column + "` = " + value + " WHERE `" + id_col
                + "`=" + id_val;

        Connection db = get();
        db.createStatement().execute(query);
    }

}
