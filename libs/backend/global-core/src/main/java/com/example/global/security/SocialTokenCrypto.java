package com.example.global.security;

import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class SocialTokenCrypto {

    private static final String KDF_ALGORITHM = "HKDF-SHA256";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String AES_ALGORITHM = "AES";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int KEY_LENGTH_BYTES = 32;
    private static final byte[] KEY_DERIVATION_SALT = "gyun-social-token".getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY_DERIVATION_INFO = "social-token-aes-gcm-key".getBytes(StandardCharsets.UTF_8);

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public SocialTokenCrypto(@Value("${app.security.encryption-secret}") final String encryptionSecret) {
        this.secretKey = new SecretKeySpec(deriveKeyMaterial(encryptionSecret), AES_ALGORITHM);
    }

    public String encrypt(final String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return "";
        }

        try {
            final byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            final ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (final Exception e) {
            throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    public String decrypt(final String encryptedText) {
        if (!StringUtils.hasText(encryptedText)) {
            return "";
        }

        try {
            final byte[] combined = Base64.getDecoder().decode(encryptedText);
            if (combined.length <= IV_LENGTH) {
                throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, "암호문 형식이 올바르지 않습니다.");
            }

            final byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            final byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] resultPlainText = cipher.doFinal(cipherText);

            return new String(resultPlainText, StandardCharsets.UTF_8);
        } catch (final GlobalException e) {
            throw e;
        } catch (final Exception e) {
            throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private static byte[] deriveKeyMaterial(final String secret) {
        if (!StringUtils.hasText(secret)) {
            throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, "소셜 토큰 암호화 키(encryption-secret)가 비어 있습니다.");
        }

        try {
            final HKDFParameterSpec parameters = HKDFParameterSpec.ofExtract()
                    .addIKM(secret.getBytes(StandardCharsets.UTF_8))
                    .addSalt(KEY_DERIVATION_SALT.clone())
                    .thenExpand(KEY_DERIVATION_INFO.clone(), KEY_LENGTH_BYTES);

            final KDF kdf = KDF.getInstance(KDF_ALGORITHM);
            return kdf.deriveData(parameters);
        } catch (final NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
