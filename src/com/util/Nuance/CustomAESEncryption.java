package com.util.Nuance;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;
import java.util.Base64;

public class CustomAESEncryption {

    public static String encrypt(String data, String key) throws Exception {
        // Generate a random IV (Initialization Vector)
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        // Create AES key from the given key
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Create AES cipher in CBC mode with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        // Encrypt the data
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));

        // Combine IV and encrypted data and encode to Base64
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String encryptedData, String key) throws Exception {
        // Decode Base64 and extract IV
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, iv.length);

        // Create AES key from the given key
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Create AES cipher in CBC mode with PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        // Decrypt the data
        byte[] decryptedBytes = cipher.doFinal(combined, iv.length, combined.length - iv.length);
        return new String(decryptedBytes, "UTF-8");
    }

    public static void main(String[] args) {
        try {
            String originalData = "IspeTeNd$@@7";
            String encryptionKey = "mysecretpassword"; // Replace with your actual key

            // Encrypt data
            String encryptedData = encrypt(originalData, encryptionKey);
            System.out.println("Encrypted: " + encryptedData);

            // Decrypt data
            String decryptedData = new CustomAESEncryption().decrypt(encryptedData, encryptionKey);
            System.out.println("Decrypted: " + decryptedData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

