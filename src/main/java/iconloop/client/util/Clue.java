package iconloop.client.util;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.vault.SecretSharing;
import org.bouncycastle.crypto.InvalidCipherTextException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class Clue {
    // FIXME: recoveryKey is Dummy code.
    private static final String recoveryKey = "f660911567e08de702b01a819a2d1572";
    private static final SecretKey _recoveryKey = new SecretKeySpec(recoveryKey.getBytes(), "AES");;

    public String[] makeClue(int N, int T, byte[] data) {
        SecretSharing ss = new SecretSharing(new SecureRandom(), N, T);
        byte[][] clues = ss.split(data);
        String[] out = new String[N];
        for(int i=0; i<N; i++) {
            try {
                out[i] = encryptData(clues[i]);
            } catch (InvalidCipherTextException e) {
                System.out.println("encrypt fail(" + e.getMessage() + ")");
            }
        }
        return out;
    }

    private String encryptData(byte[] data) throws InvalidCipherTextException {
        byte[] iv = Utils.getRandomBytes(16);
        byte[] cipherText = Utils.aesEncrypt(data, _recoveryKey, iv);
        byte[] output = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(cipherText, 0, output, iv.length, cipherText.length);
        return Utils.encodeToBase64UrlSafeString(output);
    }

    public byte[] reconstruct(int N, int T, String[] encClues) {
        byte[][] shares = new byte[encClues.length][];
        for(int i=0; i< encClues.length; i++) {
            try {
                shares[i] = decryptData(encClues[i]);
            } catch (InvalidCipherTextException e) {
                System.out.println("decrypt fail(" + e.getMessage() + ")");
            }
        }
        SecretSharing ss = new SecretSharing(new SecureRandom(), N, T);
        return ss.reconstruct(shares);
    }

    private byte[] decryptData(String encrypted) throws InvalidCipherTextException {
        byte[] input = Utils.decodeFromBase64UrlSafeString(encrypted);
        byte[] iv = new byte[16];
        byte[] cipherText = new byte[input.length - iv.length];
        System.arraycopy(input, 0, iv, 0, iv.length);
        System.arraycopy(input, iv.length, cipherText, 0, cipherText.length);
        return Utils.aesDecrypt(cipherText, _recoveryKey, iv);
    }
}
