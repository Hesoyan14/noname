package itz.silentcore.web.server;

import itz.silentcore.utils.client.Constants;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class ClientCrypto {
    public static String decryptFriendsResponse(String encrypted) {
        try {
            if (encrypted.startsWith("{") || encrypted.startsWith("[")) {
                return encrypted;
            }

            byte[] key = deriveKey(Constants.FRIENDS_API_KEY);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("[ClientCrypto] Failed to decrypt: " + e.getMessage());
            return encrypted;
        }
    }

    private static byte[] deriveKey(String secretKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(secretKey.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(keyBytes, 16);
    }
}
