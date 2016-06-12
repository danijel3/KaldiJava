package pl.edu.pjwstk.kaldi.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WAV {

	public static float getLength(File file) throws UnsupportedAudioFileException, IOException {

		AudioFileFormat af = AudioSystem.getAudioFileFormat(file);

		return af.getFrameLength() / af.getFormat().getFrameRate();
	}

	public static float getLengthOld(File file) throws IOException {
		byte[] buffer = new byte[44];
		ByteBuffer header = ByteBuffer.wrap(buffer);
		header.order(ByteOrder.LITTLE_ENDIAN);

		FileInputStream fis = new FileInputStream(file);

		fis.read(buffer, 0, 44);

		if (!(new String(buffer, 0, 4).equals("RIFF"))) {
			fis.close();
			throw new IOException("RIFF header missing!");
		}

		if (!(new String(buffer, 8, 4).equals("WAVE"))) {
			fis.close();

			throw new IOException("WAVE header missing!");
		}

		if (!(new String(buffer, 12, 4).equals("fmt "))) {
			fis.close();
			throw new IOException("fmt header missing!");
		}

		if (!(new String(buffer, 36, 4).equals("data"))) {
			fis.close();
			throw new IOException("data header missing!");
		}

		int src_len = header.getInt(40);
		int byte_rate = header.getInt(28);

		fis.close();

		return src_len / (float) byte_rate;
	}

	public static void extract(File source, File dest, double time_start, double time_end)
			throws UnsupportedAudioFileException, IOException {

		AudioInputStream input = AudioSystem.getAudioInputStream(source);

		AudioFormat af = input.getFormat();

		if (af.getChannels() != 1)
			throw new IOException("channel count != 1");

		int ss = af.getSampleSizeInBits() / 8;
		int sl = (int) (input.getFrameLength() * ss);
		float sr = af.getSampleRate();

		int beg = (int) (time_start * sr) * ss;
		if (beg > sl)
			beg = sl;
		beg = (int) (beg / ss) * ss;
		int end = (int) (time_end * sr) * ss;
		if (end > sl)
			end = sl;
		end = (int) (end / ss) * ss;
		int len = end - beg;

		int fl = len / af.getFrameSize();

		byte buf[] = new byte[len];

		input.skip(beg);
		input.read(buf, 0, len);

		AudioInputStream output = new AudioInputStream(new ByteArrayInputStream(buf), af, fl);

		AudioSystem.write(output, AudioFileFormat.Type.WAVE, dest);

	}

	public static void extractOld(File source, File dest, double time_start, double time_end) throws IOException {

		byte[] buffer = new byte[44];
		ByteBuffer header = ByteBuffer.wrap(buffer);
		header.order(ByteOrder.LITTLE_ENDIAN);

		FileInputStream fis = new FileInputStream(source);

		fis.read(buffer, 0, 44);

		if (!(new String(buffer, 0, 4).equals("RIFF"))) {
			fis.close();
			throw new IOException("RIFF header missing!");
		}

		if (!(new String(buffer, 8, 4).equals("WAVE"))) {
			fis.close();

			throw new IOException("WAVE header missing!");
		}

		if (!(new String(buffer, 12, 4).equals("fmt "))) {
			fis.close();
			throw new IOException("fmt header missing!");
		}

		if (!(new String(buffer, 36, 4).equals("data"))) {
			fis.close();
			throw new IOException("data header missing!");
		}

		int src_len = header.getInt(40);
		int byte_rate = header.getInt(28);
		short ba = header.getShort(32);

		int start_offset = (int) (byte_rate * time_start);
		int len = (int) ((time_end - time_start) * byte_rate);

		start_offset = (int) (start_offset / ba) * ba;
		len = (int) (len / ba) * ba;

		if (start_offset + len > src_len)
			len = src_len - start_offset;

		header.putInt(4, len + 36);
		header.putInt(40, len);

		FileOutputStream fos = new FileOutputStream(dest);

		fos.write(header.array(), 0, 44);

		fis.skip(start_offset);

		byte[] data = new byte[len];

		int ret = fis.read(data);

		fis.close();

		if (ret != len) {
			fos.close();
			throw new IOException("Data not read properly!");
		}

		fos.write(data);

		fos.close();

	}

	public static void main(String[] args) {
		try {

			System.out.println(getLength(new File("/home/guest/Desktop/Respeaking/ses0037/file002.wav")));

			extract(new File("/home/guest/Desktop/Respeaking/ses0037/file002.wav"),
					new File("/home/guest/Desktop/Respeaking/ses0037/out.wav"), 284.69, 288.8636779785156);

			extract(new File("/home/guest/Desktop/Respeaking/ses0037/file002.wav"),
					new File("/home/guest/Desktop/Respeaking/ses0037/out2.wav"), 284.69, 300);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
