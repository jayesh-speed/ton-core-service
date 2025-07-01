package com.speed.toncore.util;

import com.speed.toncore.constants.Errors;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class SecurityManagerUtil {

	/**
	 * just for one time use, will remove after generating keys for testnet and mainnet
	 */
	public static byte[] generateKey(int keySize) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(keySize);
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] generatedSecret = secretKey.getEncoded();
		String sttValue = Base64.getEncoder().encodeToString(generatedSecret);
		byte[] decodedSecret = Base64.getDecoder().decode(sttValue);
		return secretKey.getEncoded();
	}


	public static String encrypt(String algorithm, String input, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, algorithm));
			byte[] cipherText = cipher.doFinal(input.getBytes());
			return Base64.getEncoder()
					.encodeToString(cipherText);
		} catch (Exception ex) {
			LOG.error(Errors.ENCRYPTION_EXCEPTION, ex);
		}
		return null;
	}

	public static String decrypt(String algorithm, String cipherText, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, algorithm));
			byte[] plainText = cipher.doFinal(Base64.getDecoder()
					.decode(cipherText));
			return new String(plainText);
		} catch (Exception ex) {
			LOG.error(Errors.DECRYPTION_EXCEPTION, ex);
		}
		return null;
	}
}
