package org.ubstorm.service.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AesCrypto {
	private static final String TRANSFORM = "AES/ECB/PKCS5Padding";
	private static final String m_key = "_UBIForm_Server_";
	
	public static String encrypt(String plainText, String key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128);
		
		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(plainText.getBytes());
		return asHex(encrypted);
	}
	
	public static byte[] encryptToByteArray(byte[] source, String key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128);
		
		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(source);
		return encrypted;
	}

	public static String decrypt(String cipherText, String key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128);

		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] original = cipher.doFinal(fromString(cipherText));
		String originalString = new String(original);
		return originalString;
	}

	public static byte[] decryptToByteArray(byte[] source, String key) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128);

		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] original = cipher.doFinal(source);
		
		return original;
	}
	
	private static String asHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}

	private static byte[] fromString(String hex) {
		int len = hex.length();
		byte[] buf = new byte[((len + 1) / 2)];

		int i = 0, j = 0;
		if ((len % 2) == 1)
			buf[j++] = (byte) fromDigit(hex.charAt(i++));

		while (i < len) {
			buf[j++] = (byte) ((fromDigit(hex.charAt(i++)) << 4) | fromDigit(hex
					.charAt(i++)));
		}
		return buf;
	}

	private static int fromDigit(char ch) {
		if (ch >= '0' && ch <= '9')
			return ch - '0';
		if (ch >= 'A' && ch <= 'F')
			return ch - 'A' + 10;
		if (ch >= 'a' && ch <= 'f')
			return ch - 'a' + 10;

		throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
	}
}
