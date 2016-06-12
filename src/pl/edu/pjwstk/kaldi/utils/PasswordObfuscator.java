package pl.edu.pjwstk.kaldi.utils;

import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;

public class PasswordObfuscator {

	private final static long passwordObfuscator = 2934278932478932493L;
	
	public static String obfuscatePassword(String password) {

		int longNumBytes = Long.SIZE / 8;

		ByteBuffer in = ByteBuffer.wrap(password.getBytes());
		ByteBuffer out = ByteBuffer.allocate(in.capacity());
		ByteBuffer obf = ByteBuffer.allocate(longNumBytes);
		obf.putLong(passwordObfuscator);
		obf.rewind();

		while (in.remaining() >= longNumBytes) {
			long l = in.getLong();
			out.putLong(l ^ passwordObfuscator);
		}

		while (in.hasRemaining()) {
			byte b = in.get();
			out.put((byte) (b ^ obf.get()));
		}

		return DatatypeConverter.printBase64Binary(out.array());

	}

	public static String deobfuscatePassword(String password) throws IllegalArgumentException {

		int longNumBytes = Long.SIZE / 8;

		ByteBuffer in = ByteBuffer.wrap(DatatypeConverter.parseBase64Binary(password));
		ByteBuffer out = ByteBuffer.allocate(in.capacity());
		ByteBuffer obf = ByteBuffer.allocate(longNumBytes);
		obf.putLong(passwordObfuscator);
		obf.rewind();

		while (in.remaining() >= longNumBytes) {
			long l = in.getLong();
			out.putLong(l ^ passwordObfuscator);
		}

		while (in.hasRemaining()) {
			byte b = in.get();
			out.put((byte) (b ^ obf.get()));
		}

		return new String(out.array());
	}
	
}
