package iconloop.lab.crypto.he.paillier;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomTest {

    public static void main(String[] ar) throws PaillierException {
        int keySize = 2048;
        int paramSize = 1024;
        for(int i=0; i<1; i++) {
            long a = System.currentTimeMillis();
            PaillierPrivateKey priKey = PaillierUtils.generateKey(keySize);
            PaillierPublicKey pubKey = priKey.getPublicKey();

            for(int j=0; j<100; j++) {
                long c = System.currentTimeMillis();
                testComplex(pubKey, priKey, paramSize);
                long d = System.currentTimeMillis();
                System.out.println("    " + j + "\t : " + (d - c) + " milliseconds");
            }

            long b = System.currentTimeMillis();
            System.out.println(i + "\t : " + (b - a) + " milliseconds");
        }
    }

    public static void testComplex(PaillierPublicKey pubKey, PaillierPrivateKey priKey, int paramSize) throws PaillierException {
        // E(a)^s + E(b) = E(sa + b)
        BigInteger a = new BigInteger(paramSize, new SecureRandom());
        BigInteger ca = pubKey.encrypt(a);

        BigInteger s = new BigInteger(paramSize, new SecureRandom());

        BigInteger sca = PaillierUtils.cipherScalarMul(s, ca, pubKey);

        BigInteger b = new BigInteger(paramSize, new SecureRandom());
        BigInteger cb = pubKey.encrypt(b);

        BigInteger scacb = PaillierUtils.cipherAdd(sca, cb, pubKey);

        BigInteger m = priKey.decrypt(scacb);

        BigInteger sab = s.multiply(a).add(b).mod(pubKey.getN());

        if(!m.equals(sab)) {
            System.out.println("a  : " + a.toString(16));
            System.out.println("b  : " + a.toString(16));
            System.out.println("s  : " + a.toString(16));
        }
    }
}
