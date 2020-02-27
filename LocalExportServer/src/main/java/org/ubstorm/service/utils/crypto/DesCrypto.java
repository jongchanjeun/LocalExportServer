package org.ubstorm.service.utils.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DesCrypto {
	private static final String TRANSFORM = "DES/ECB/PKCS5Padding";
	private static final String m_key = "_UBIForm";
	
	public static byte[] encryptToByteArray(byte[] source, String key) throws Exception 
	{
		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(source);
		return encrypted;
	}

	public static byte[] decryptToByteArray(byte[] source, String key) throws Exception 
	{
		byte[] raw = key==null ? m_key.getBytes() : key.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "DES");
		Cipher cipher = Cipher.getInstance(TRANSFORM);

		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] original = cipher.doFinal(source);
		
		return original;
	}
}
