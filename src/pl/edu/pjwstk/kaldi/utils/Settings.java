package pl.edu.pjwstk.kaldi.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Settings {

	/**************************************************************************************************************************/

	public static File kaldi_root = new File("/home/guest/kaldi");

	public static File perl_bin = new File("/usr/bin/perl");

	public static File python_bin = new File("/usr/bin/python");

	public static File bash_bin = new File("/bin/bash");

	public static File transcriber_dir = new File("/home/guest/transcriber");
	
	public static File kaldi_kws_bin= new File("/usr/local/bin/kaldi_kws");

	public static File essentia_pitch_bin = new File("/home/guest/essentia/build/src/examples/standard_pitchyinfft");

	public static File estimate_ngram_bin = new File("/home/guest/mitlm/estimate-ngram");

	public static File ngram_count_bin = new File("/home/guest/kaldi/tools/srilm/lm/bin/i686-m64/ngram-count");

	public static File praat_bin = new File("/home/guest/praat/sources_5384/praat");

	public static File shout_dir = new File("/home/guest/shout/release/src");

	public static File shout_models = new File("/home/guest/shout/");

	public static File lium_models = new File("lium_model");

	public static File julius_bin = new File("/usr/local/bin/julius");

	public static File julius_mklm_bin = new File("/usr/local/bin/mkbingram");

	public static File ffmpeg_bin = new File("/usr/bin/ffmpeg");

	public static double julius_win_offset = 0.01;

	public static String julius_default_encoding = "utf8";

	public static File java_lib_dir = new File("JAR");

	public static File python_scripts_dir = new File("Python");

	public static File daemon_pid = new File("daemon.pid");

	public static int daemon_slots = 4;

	public static int daemon_startup_timer_ms = 10000;

	public static File daemon_status = new File("daemon_status.xml");

	public static File tasks_dir = new File("tasks");

	public static File curr_task_dir = null;

	public static File log_dir = new File("logs");

	public static File temp_dir = new File("tmp");
	public static File temp_dir2 = new File("tmp2");

	public static double align_beam = 500;

	public static double align_retry_beam = 1000;

	public static boolean removeTempFile = true;

	public static String default_encoding = "utf8";

	public static String db_username = "test";

	@Hidden(password = true)
	public static String db_password = "test";

	public static String db_name = "speech_services";

	/**************************************************************************************************************************/

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Hidden {
		boolean password() default false;
	}

	public static void loadSettings(File settingsFile) throws IOException {

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(settingsFile), default_encoding));

		String line, key, value, type;
		String[] values = null;
		boolean array;
		Field field;
		int pos;
		while ((line = reader.readLine()) != null) {

			if (line.length() == 0)
				continue;

			pos = line.indexOf(';');
			if (pos >= 0) {
				line = line.substring(0, pos).trim();
			}

			if (line.length() == 0)
				continue;

			pos = line.indexOf('=');

			key = line.substring(0, pos).trim();
			value = line.substring(pos + 1).trim();

			try {
				field = Settings.class.getField(key);
			} catch (NoSuchFieldException | SecurityException e) {
				System.err.println("Cannot find key " + key + " to configure!");
				continue;
			}

			if (!Modifier.isStatic(field.getModifiers())) {
				System.err.println("The key " + key + " cannot be configured <not static>!");
				continue;
			}

			type = field.getType().getName();

			Hidden annotation = field.getAnnotation(Hidden.class);
			if (annotation != null && annotation.password()) {
				if (!type.equals("java.lang.String")) {
					System.err.println("Only String types can be deobfuscated! Skipping " + key + "!");
					continue;
				}
			}

			if (field.getType().isArray()) {
				values = value.split(",");
				type = field.getType().getComponentType().getName();
				array = true;
			} else {
				array = false;
			}

			try {
				if (type.equals("java.lang.String")) {

					if (array) {
						String obj[] = new String[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();

							if (annotation != null && annotation.password())
								value = PasswordObfuscator.deobfuscatePassword(value);

							Array.set(obj, i, value);
						}
					} else {
						if (annotation != null && annotation.password())
							value = PasswordObfuscator.deobfuscatePassword(value);
						field.set(null, value);
					}

				} else if (type.equals("int")) {
					if (array) {
						int[] obj = new int[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();
							int iv;
							try {
								iv = Integer.parseInt(value);
							} catch (NumberFormatException ne) {
								System.err.println("The key " + key + " cannot be configured <integer parse exc>!");
								continue;
							}
							obj[i] = iv;
						}
					} else {
						int iv;
						try {
							iv = Integer.parseInt(value);
						} catch (NumberFormatException ne) {
							System.err.println("The key " + key + " cannot be configured <integer parse exc>!");
							continue;
						}

						field.setInt(null, iv);
					}

				} else if (type.equals("char")) {
					if (array) {
						char[] obj = new char[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();
							if (value.length() == 0) {
								System.err.println("WARNING: using space as value for " + key);
								obj[i] = ' ';
							}
							if (value.length() > 1) {
								System.err.println("WARNING: using only first char in " + key);
							}
							obj[i] = value.charAt(0);
						}
					} else {
						if (value.length() == 0) {
							System.err.println("WARNING: using space as value for " + key);
							field.set(null, ' ');
						}
						if (value.length() > 1) {
							System.err.println("WARNING: using only first char in " + key);
						}
						field.set(null, value.charAt(0));
					}
				} else if (type.equals("float")) {
					if (array) {
						float[] obj = new float[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();
							float f;
							try {
								f = Float.parseFloat(value);
							} catch (NumberFormatException ne) {
								System.err.println("The key " + key + " cannot be configured <float parse exc>!");
								continue;
							}
							obj[i] = f;
						}
					} else {
						float f;
						try {
							f = Float.parseFloat(value);
						} catch (NumberFormatException ne) {
							System.err.println("The key " + key + " cannot be configured <float parse exc>!");
							continue;
						}
						field.setFloat(null, f);
					}
				} else if (type.equals("double")) {
					if (array) {
						double[] obj = new double[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();
							double d;
							try {
								d = Double.parseDouble(value);
							} catch (NumberFormatException ne) {
								System.err.println("The key " + key + " cannot be configured <double parse exc>!");
								continue;
							}
							obj[i] = d;
						}
					} else {
						double d;
						try {
							d = Double.parseDouble(value);
						} catch (NumberFormatException ne) {
							System.err.println("The key " + key + " cannot be configured <double parse exc>!");
							continue;
						}
						field.setDouble(null, d);
					}
				} else if (type.equals("boolean")) {
					if (array) {
						boolean[] obj = new boolean[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim().toLowerCase();
							if (value.equals("true") || value.equals("yes") || value.equals("1"))
								obj[i] = true;
							else
								obj[i] = false;
						}
					} else {
						value = value.toLowerCase();
						if (value.equals("true") || value.equals("yes") || value.equals("1"))
							field.setBoolean(null, true);
						else
							field.setBoolean(null, false);
					}
				} else if (type.equals("java.io.File")) {
					if (array) {
						File[] obj = new File[values.length];
						field.set(null, obj);
						for (int i = 0; i < values.length; i++) {
							value = values[i].trim();
							Array.set(obj, i, new File(value));
						}
					} else {
						field.set(null, new File(value));
					}
				} else {
					System.err.println("The key " + key + " cannot be configured <unknown type>!");
					continue;
				}
			} catch (IllegalAccessException e) {
				System.err.println("The key " + key + " cannot be configured <access exc>!");
				continue;
			}
		}

		reader.close();
	}

	public static void dumpSettings() {

		Field[] fields = Settings.class.getFields();

		Log.info("Settings dump:");

		for (Field field : fields) {

			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			if (field.getAnnotation(Hidden.class) != null) {
				Log.info(field.getName() + " = <hidden>");
			} else {
				try {
					if (field.getType().isArray()) {
						Object obj = field.get(null);
						int length = Array.getLength(obj);
						StringBuffer buf = new StringBuffer();
						buf.append("{");
						for (int i = 0; i < length; i++) {
							buf.append("" + Array.get(obj, i));
							if (i < length - 1)
								buf.append(",");
						}
						buf.append("}");
						Log.info(field.getName() + " = " + buf);
					} else
						Log.info(field.getName() + " = " + field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Log.info(field.getName() + " = <cannot access>");
				}
			}
		}
	}

	public static void dumpSettings(File file) throws IOException {

		PrintWriter writer = new PrintWriter(file);

		Field[] fields = Settings.class.getFields();

		writer.println(";; Autogenerated settings file...");

		for (Field field : fields) {

			if (!Modifier.isStatic(field.getModifiers()))
				continue;

			Hidden a = field.getAnnotation(Hidden.class);
			try {
				if (a != null && a.password()) {
					writer.println(
							field.getName() + " = " + PasswordObfuscator.obfuscatePassword("" + field.get(null)));
				} else {
					if (field.getType().isArray()) {
						Object obj = field.get(null);
						int length = Array.getLength(obj);
						StringBuffer buf = new StringBuffer();
						for (int i = 0; i < length; i++) {
							buf.append("" + Array.get(obj, i));
							if (i < length - 1)
								buf.append(",");
						}

						writer.println(field.getName() + " = " + buf);
					} else
						writer.println(field.getName() + " = " + field.get(null));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				writer.println(field.getName() + " = <cannot access>");
			}

		}

		writer.close();

	}
}
