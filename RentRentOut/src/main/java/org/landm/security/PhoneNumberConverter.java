package org.landm.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256 enkripcija broja telefona u bazi.
 * Ključ mora biti tačno 32 karaktera (postaviti u application.properties).
 * Postojeći plain-text brojevi su backward-compat (try/catch vraća original).
 */
@Component
@Converter
public class PhoneNumberConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    // Static field — Spring popunjava pre nego što JPA počne konverziju
    private static String KEY;

    @Value("${encryption.phone-key}")
    public void setKey(String key) {
        PhoneNumberConverter.KEY = key;
    }

    @Override
    public String convertToDatabaseColumn(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) return phoneNumber;
        try {
            byte[] keyBytes = Arrays.copyOf(KEY.getBytes(StandardCharsets.UTF_8), 32);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(phoneNumber.getBytes(StandardCharsets.UTF_8));

            // Spojimo IV (16B) + encrypted u jedan niz → Base64
            byte[] combined = new byte[16 + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, 16);
            System.arraycopy(encrypted, 0, combined, 16, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri enkripciji telefona", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return dbData;
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            if (combined.length <= 16) return dbData; // plain-text fallback

            byte[] iv        = Arrays.copyOfRange(combined, 0, 16);
            byte[] encrypted = Arrays.copyOfRange(combined, 16, combined.length);

            byte[] keyBytes = Arrays.copyOf(KEY.getBytes(StandardCharsets.UTF_8), 32);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Backward compat: ako je vrednost još uvek plain-text, vraćamo je kakva jeste
            return dbData;
        }
    }
}
