package pl.edu.pjwstk.kaldi.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * Class for parsing options.
 * <p>
 * Current supported options types are String, File, Integer, Double, Float and
 * Boolean.
 *
 * @author guest
 */
public class ParseOptions {

    private String program_name;
    private String program_description;

    public ParseOptions(String program_name, String program_description) {
        this.program_name = program_name;
        this.program_description = program_description;
    }

    private class Argument {
        public Object value;
        public String default_value;
        private Class<?> type;
        public String name;
        public String description;

        public Argument(Class<?> type, String name, String description, String default_value)
                throws ClassCastException, NumberFormatException {
            this.type = type;
            this.name = name;
            this.description = description;
            this.default_value = default_value;
            if (default_value != null)
                set(default_value);
            else {
                default_value = null;
                check();
            }
        }

        public void set(String value) throws ClassCastException, NumberFormatException {

            switch (type.getName()) {
                case "java.lang.String":
                    this.value = value;
                    break;
                case "java.io.File":
                    this.value = new File(value);
                    break;
                case "java.lang.Integer":
                    this.value = Integer.parseInt(value);
                    break;
                case "java.lang.Float":
                    this.value = Float.parseFloat(value);
                    break;
                case "java.lang.Double":
                    this.value = Double.parseDouble(value);
                    break;
                case "java.lang.Boolean":
                    this.value = Boolean.parseBoolean(value);
                    break;
                default:
                    throw new ClassCastException("Type not implemented!");
            }
        }

        public String toString() {
            switch (type.getName()) {
                case "java.lang.String":
                    return value.toString();
                case "java.io.File":
                    return ((File) value).getAbsolutePath();
                case "java.lang.Integer":
                    return value.toString();
                case "java.lang.Float":
                    return value.toString();
                case "java.lang.Double":
                    return value.toString();
                case "java.lang.Boolean":
                    return value.toString();
                default:
                    throw new ClassCastException("Type not implemented!");
            }
        }

        public void check() throws ClassCastException {
            switch (type.getName()) {
                case "java.lang.String":
                case "java.io.File":
                case "java.lang.Integer":
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.lang.Boolean":
                    break;
                default:
                    throw new ClassCastException("Type not implemented!");
            }
        }

        public void printHelp(String name, int col_width, int max_width) {
            int desc_offset;
            int desc_end;

            System.out.print("    ");
            System.out.print(name);
            for (int i = 8 + name.length(); i < col_width; i++)
                System.out.print(" ");
            System.out.print("    ");

            desc_offset = 0;
            desc_end = max_width - col_width + 8;
            if (desc_end > description.length())
                desc_end = description.length();

            System.out.println(description.substring(desc_offset, desc_end));
            desc_offset = desc_end;

            while (desc_offset < description.length()) {

                for (int i = 0; i < col_width; i++)
                    System.out.print(" ");

                desc_end = desc_offset + max_width - col_width + 8;
                if (desc_end > description.length())
                    desc_end = description.length();
                System.out.println(description.substring(desc_offset, desc_end));
                desc_offset = desc_end;
            }

            if (default_value != null) {
                for (int i = 0; i < col_width; i++)
                    System.out.print(" ");
                System.out.println("(default = " + default_value + ")");
            }
        }

        public boolean isBoolean() {
            return type.getName().equals("java.lang.Boolean");
        }
    }

    private HashMap<String, Argument> arg_map = new HashMap<>();
    private HashMap<Character, Argument> shortarg_map = new HashMap<>();
    private Vector<Argument> arg_map_list = new Vector<>();
    private Vector<Argument> arg_list = new Vector<>();

    /**
     * Add a named argument.
     *
     * @param name          prepended with double dash on command-line; followed by equal
     *                      sign; example: --argument-name=value
     * @param shortname     prepended with single dash on command-line; followed by space;
     *                      example: -a value
     * @param type          type of argument; example String.getClass()
     * @param description   description of argument for the help
     * @param default_value default value for this argument
     */
    public <T> void addArgument(String name, Character shortname, Class<?> type, String description,
                                String default_value) {

        Argument arg = new Argument(type, name, description, default_value);
        arg_map.put(name, arg);
        shortarg_map.put(shortname, arg);
        arg_map_list.add(arg);

    }

    /**
     * Add un-named main argument.
     *
     * @param type        type of argument; example: String.class
     * @param name        name of argument for the help
     * @param description description of the arguemnt for the help
     */
    public void addArgument(Class<?> type, String name, String description) {
        arg_list.add(new Argument(type, name, description, null));
    }

    public static class ArgumentParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ArgumentParseException(String reason) {
            super(reason);
        }

        public ArgumentParseException(Throwable th) {
            super(th);
        }
    }

    public void help() {

        int max_width = 80;
        int col_width = 0;
        for (Argument argument : arg_list)
            if (argument.name.length() + 2 > col_width)
                col_width = argument.name.length() + 2;
        for (Argument argument : arg_map.values()) {
            if (argument.name.length() + 8 > col_width)
                col_width = argument.name.length() + 8;
        }
        col_width += 8;

        System.out.println("Help: ");
        System.out.println();
        System.out.print("  " + program_name + " [options]");
        for (Argument argument : arg_list)
            System.out.print(" <" + argument.name + ">");
        System.out.println();
        System.out.println();
        System.out.println(program_description);
        System.out.println();
        System.out.println("Arguments:");
        for (Argument argument : arg_list) {
            argument.printHelp("<" + argument.name + ">", col_width, max_width);
        }
        System.out.println();
        System.out.println("Options:");
        for (Argument argument : arg_map_list) {

            Character c = '?';
            for (Entry<Character, Argument> arg_entry : shortarg_map.entrySet())
                if (arg_entry.getValue() == argument)
                    c = arg_entry.getKey();

            argument.printHelp("--" + argument.name + " or -" + c, col_width, max_width);
        }
        System.out.print("    --help");
        for (int i = 10; i < col_width; i++)
            System.out.print(" ");
        System.out.println("Shows help.");

    }

    public boolean parse(String[] args) throws ArgumentParseException {

        int arglist_count = 0;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--help")) {
                help();
                return false;
            } else if (arg.startsWith("--")) {
                String tok[] = arg.substring(2).split("=");

                if (tok.length != 2)
                    throw new ArgumentParseException("Argument missing or too many equal signs: " + arg);

                if (tok.length == 1) {
                    Argument argument = arg_map.get(tok[0]);

                    if (argument == null)
                        throw new ArgumentParseException("Unknown argument: " + arg);

                    if (!argument.isBoolean())
                        throw new ArgumentParseException("Argument missing value: " + arg);

                    argument.set("true");

                    continue;
                }

                String name = tok[0];
                String value = tok[1];

                Argument argument = arg_map.get(name);

                if (argument == null)
                    throw new ArgumentParseException("Unknown argument: " + arg);

                try {
                    argument.set(value);
                } catch (NumberFormatException | ClassCastException e) {
                    throw new ArgumentParseException(e);
                }

            } else if (arg.length() > 1 && arg.startsWith("-")) {

                if (arg.length() > 2)
                    throw new ArgumentParseException("Short arguments can contain only one char: " + arg);

                Character c = arg.charAt(1);

                Argument argument = shortarg_map.get(c);

                if (argument == null)
                    throw new ArgumentParseException("Unknown argument: " + arg);

                if (argument.isBoolean()) {
                    argument.set("true");
                    continue;
                }

                i++;
                String value = args[i];

                try {
                    argument.set(value);
                } catch (NumberFormatException | ClassCastException e) {
                    throw new ArgumentParseException(e);
                }

            } else {

                if (arglist_count >= arg_list.size())
                    throw new ArgumentParseException("Too many arguments!");

                Argument argument = arg_list.get(arglist_count);
                arglist_count++;

                try {
                    argument.set(arg);
                } catch (NumberFormatException | ClassCastException e) {
                    throw new ArgumentParseException(e);
                }

            }
        }

        if (arglist_count < arg_list.size()) {
            help();
            return false;
        }

        return true;
    }

    public void printOptions() {

        for (Argument argument : arg_map.values()) {
            System.out.println(argument.name + " = " + argument.value);
        }
        for (Argument argument : arg_list) {
            System.out.println(argument.name + " = " + argument.value);
        }

    }

    public Object getArgument(int num) throws ArrayIndexOutOfBoundsException, ClassCastException {
        if (num < 0 || num >= arg_list.size())
            throw new ClassCastException("Argument num not defined: " + num);
        return arg_list.get(num).value;
    }

    public Object getArgument(String name) throws ClassCastException {
        if (!arg_map.containsKey(name))
            throw new ClassCastException("Argument not defined: " + name);
        return arg_map.get(name).value;
    }

    public Object getArgument(Character name) throws ClassCastException {
        if (!shortarg_map.containsKey(name))
            throw new ClassCastException("Argument not defined: " + name);
        return shortarg_map.get(name).value;
    }
}
