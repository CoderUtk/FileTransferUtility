package filetransferutility.utils;

import filetransferutility.main.FTConstants;
import filetransferutility.main.Messages;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Utils {

    private Utils() {
    }

    public static void deleteFileFolder(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        Files.delete(path);
    }

    public static String getEncryptedString(String value) {
        try {
            SecretKey key = getSecretKey();
            Cipher pbeCipher = Cipher.getInstance(FTConstants.CIPHER_ALGORITHM);
            pbeCipher.init(Cipher.ENCRYPT_MODE, key);
            AlgorithmParameters parameters = pbeCipher.getParameters();
            IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
            byte[] cryptoText = pbeCipher.doFinal(value.getBytes(FTConstants.CHARSET));
            byte[] iv = ivParameterSpec.getIV();
            return base64Encode(iv) + ":" + base64Encode(cryptoText);
        } catch (Exception e) {
            System.err.println(Messages.ENCRYPT_FAILED + e.getMessage());
            return value;
        }
    }

    public static String getDecryptedString(String value) {
        try {
            SecretKey key = getSecretKey();
            String iv = value.split(":")[0];
            String property = value.split(":")[1];
            Cipher pbeCipher = Cipher.getInstance(FTConstants.CIPHER_ALGORITHM);
            pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
            return new String(pbeCipher.doFinal(base64Decode(property)), FTConstants.CHARSET);
        } catch (Exception e) {
            System.err.println(Messages.DECRYPT_FAILED + e.getMessage());
            return null;
        }
    }

    private static SecretKey getSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] salt = FTConstants.SALT.getBytes();
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(FTConstants.SHA_512);
        PBEKeySpec keySpec = new PBEKeySpec(
                FTConstants.SECRET_KEY.toCharArray(),
                salt,
                FTConstants.ITERATION_COUNT,
                FTConstants.KEY_LENGTH);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), FTConstants.AES_ENCRYPTION);
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] base64Decode(String property) {
        return Base64.getDecoder().decode(property);
    }
}
