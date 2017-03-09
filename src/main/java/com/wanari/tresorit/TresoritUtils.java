package com.wanari.tresorit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zcsongor on 2017.02.27..
 */
public class TresoritUtils {

	static byte[] string_to_bytes(String str) {
		return str.getBytes();
	}

	private static byte[] sha256(byte[] bytes) throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-256").digest(bytes);
	}

	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytes_to_hex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for(int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private static byte[] hex_to_bytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for(int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private static String toISOString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return dateFormat.format(date);
	}

	private static String getHeaderStringToHash(String verb, String url, Map<String, String> headers, List<String> hmacHeaders) {
		List<String> headerLines = new LinkedList<>();
		for(String key : hmacHeaders) headerLines.add(key + ":" + headers.get(key));
		return "" + verb + "\n" + url + "\n" + String.join("\n", headerLines);
	}

	private static byte[] hmacSha256(String keyHex, String stringData) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] keyBytes = hex_to_bytes(keyHex);
		byte[] data = string_to_bytes(stringData);

		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(keyBytes, "HmacSHA256");
		sha256_HMAC.init(secret_key);

		return sha256_HMAC.doFinal(data);
	}

	static Map<String, String> adminPostAuth(String adminUId, String adminKey, String url, byte[] contentBuffer) throws InvalidKeyException, NoSuchAlgorithmException {
		String date = toISOString(new Date());
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("UserId", adminUId);
		headers.put("TresoritDate", date);

		if(contentBuffer != null) {
			byte[] hash = sha256(contentBuffer);
			headers.put("Content-Type", "application/json");
			headers.put("Content-SHA256", bytes_to_hex(hash));
		}

		List<String> hmacHeaders = new LinkedList<>(headers.keySet());
		hmacHeaders.add("HMACHeaders");
		headers.put("HMACHeaders", String.join(",", hmacHeaders));
		String headerStringToHash = getHeaderStringToHash(contentBuffer != null ? "POST" : "GET", url, headers, hmacHeaders);
		byte[] hmacBytes = hmacSha256(adminKey, headerStringToHash);
		headers.put("Authorization", "AdminKey " + Base64.getEncoder().encodeToString(hmacBytes));
		return headers;
	}
}
