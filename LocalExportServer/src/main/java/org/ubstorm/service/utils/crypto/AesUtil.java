package org.ubstorm.service.utils.crypto;

import java.io.UnsupportedEncodingException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ubstorm.service.utils.Base64Coder;

public class AesUtil {
	private static final String IV = "F27D5C9927726BCEFE7510B1BDD3D137";
    private static final String SALT = "3FF2EC019C627B945225DEBAD71A01B6985FE84C95A70EB132882F88C0A59A55";
	private static final String passphrase = "dbqltmxha";	
	
    private final int keySize;
    private final int iterationCount;
    private final Cipher cipher;
    
    public AesUtil() {
        this.keySize = 128;
        this.iterationCount = 11;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        }
        catch (Exception e) {
            throw fail(e);
        }
    }
    
    public String encrypt(String plaintext) {
    	return encrypt(SALT, IV, passphrase, plaintext);
    }    
    private String encrypt(String salt, String iv, String passphrase, String plaintext) {
        try {
            SecretKey key = generateKey(salt, passphrase);
            byte[] encrypted = doFinal(Cipher.ENCRYPT_MODE, key, iv, plaintext.getBytes("UTF-8"));
            return base64(encrypted);
        }
        catch (UnsupportedEncodingException e) {
            throw fail(e);
        }
    }
    
    public String decrypt(String ciphertext) {
    	return decrypt(SALT, IV, passphrase, ciphertext);
    }
    private String decrypt(String salt, String iv, String passphrase, String ciphertext) {
        try {
            SecretKey key = generateKey(salt, passphrase);
            byte[] decrypted = doFinal(Cipher.DECRYPT_MODE, key, iv, base64(ciphertext));
            return new String(decrypted, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw fail(e);
        }
    }
    
    private byte[] doFinal(int encryptMode, SecretKey key, String iv, byte[] bytes) {
        try {
            cipher.init(encryptMode, key, new IvParameterSpec(hex(iv)));
            return cipher.doFinal(bytes);
        }
        catch (Exception e) {
            throw fail(e);
        }
    }
    
    private SecretKey generateKey(String salt, String passphrase) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), hex(salt), iterationCount, keySize);
            SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (Exception e) {
            throw fail(e);
        }
    }
    
    public static String base64(byte[] bytes) {
        return Base64Coder.encodeLines(bytes);
    }
    
    public static byte[] base64(String str) {
        return Base64Coder.decode(str);
    }
     
    public static byte[] hex(String str) {
        try {
            return Hex.decodeHex(str.toCharArray());
        }
        catch (DecoderException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private IllegalStateException fail(Exception e) {
        return new IllegalStateException(e);
    }

}
