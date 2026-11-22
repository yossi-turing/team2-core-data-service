package com.therapy.coredataservice.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * הצפנה ופיענוח של שדות רגישים במנוחה באמצעות AES-256-GCM.
 * <p>
 * פורמט הערך השמור ב-DB: Base64( IV[12 bytes] || ciphertext || authTag[16 bytes] ).
 * לכל הצפנה נוצר IV אקראי חדש, ולכן אותו טקסט מוצפן לערך שונה בכל פעם.
 */
@Service
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(@Value("${app.security.encryption-key}") String base64Key) {
        this.secretKey = buildKey(base64Key);
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();
            return Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt field value", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null) {
            return null;
        }
        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(encryptedBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Encrypted field value is not valid Base64", e);
        }
        if (payload.length < IV_LENGTH_BYTES + TAG_LENGTH_BITS / 8) {
            throw new IllegalStateException("Encrypted field value is too short");
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey,
                    new GCMParameterSpec(TAG_LENGTH_BITS, payload, 0, IV_LENGTH_BYTES));
            byte[] plain = cipher.doFinal(payload, IV_LENGTH_BYTES, payload.length - IV_LENGTH_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            throw new IllegalStateException("Encrypted field value failed integrity check (wrong key or tampered data)", e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt field value", e);
        }
    }

    private static SecretKey buildKey(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException("app.security.encryption-key (DATA_ENCRYPTION_KEY) must be set");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("app.security.encryption-key must be valid Base64", e);
        }
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException("app.security.encryption-key must decode to exactly 32 bytes (AES-256), got "
                    + keyBytes.length);
        }
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
}
