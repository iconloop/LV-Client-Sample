package iconloop.lab.crypto.vault;

import java.security.SecureRandom;
import java.util.HashMap;

import iconloop.lab.crypto.common.Utils;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class SecretSharingTest {

    // secret 분배 정책
    private static final int NumOfStorage = 2;
    private static final int NumOfThreshold = 1;


    public static void main(String[] args) throws Exception {
        // data for sharing
//        String inputSecret = "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f";
//        byte[] secret = Hex.decode(inputSecret);
        byte[] secret = new SecureRandom().generateSeed(32);

        System.out.println("##### Secret Sharing #####");
        System.out.println("  - Number of Storage   : " + NumOfStorage);
        System.out.println("  - Number of Threshold : " + NumOfThreshold);
        System.out.println("  - Origin Data         : " + Hex.toHexString(secret));
        System.out.println("  - Origin Data         : " + Utils.encodeToBase64UrlSafeString(secret));

        System.out.println("");

        SecretSharingTest main = new SecretSharingTest();

        System.out.println("----- 1. Start Sharing -----");
        main.share(secret);
        System.out.println("----- 1. End Sharing -----");

        System.out.println("\n----- 2. Start Reconstruct -----");
        byte[] reconstruct = main.reconstruct();
        System.out.println(" * Reconstruct Result : " + Arrays.areEqual(secret, reconstruct));
        System.out.println("----- 2. End Reconstruct -----");

    }

    public void share(byte[] data) throws Exception {
        SecretSharing ss = new SecretSharing(new SecureRandom(), NumOfStorage, NumOfThreshold);
        byte[][] shares = ss.split(data);
        printArray("  -share   ", shares);

        for(int i=0; i<NumOfStorage; i++)
            Storage.add(i, shares[i]);
    }

    // 최소 numOfThreshold +1 만큼의 Storage가 참여
    public byte[] reconstruct() throws Exception {
        byte[][] shares = new byte[NumOfThreshold+1][];

        for (int i = 0; i < NumOfThreshold+1; i++) {
            shares[i] = Storage.get(i);
        }

        printArray("  -share    ", shares);

        // 4. User : share로 부터 data를 복구한다.
        SecretSharing ss = new SecretSharing(new SecureRandom(), NumOfStorage, NumOfThreshold);
        byte[] data = ss.reconstruct(shares);
        print("  -Reconstructed Data", data);

        return data;
    }

    public static void print(String prefix, byte[] a) {
        System.out.println(prefix + " : " + Hex.toHexString(a));
    }

    public static void printArray(String prefix, byte[][] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.println(prefix + "[" + i + "] : " + Hex.toHexString(a[i]));
        }
    }

    public static class Storage {

        private static HashMap<Integer, byte[]> _storages = new HashMap<Integer, byte[]>();

        public static void add(int id, byte[] shares) {
            _storages.put(id, shares);
        }

        public static byte[] get(int id) {
            return _storages.get(id);
        }
    }
}
