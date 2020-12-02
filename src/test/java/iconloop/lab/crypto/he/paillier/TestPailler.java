package iconloop.lab.crypto.he.paillier;

import junit.framework.TestCase;

import java.math.BigInteger;
import java.security.SecureRandom;

public class TestPailler extends TestCase {

    private static int _keySize = 2048;
    private static int _paramSize = 2046;

    public void testKeyGeneration() throws PaillierException {
        System.out.println("\n--- Key Generation Test ---");
        PaillierPrivateKey priKey = PaillierUtils.generateKey(_keySize);
        PaillierPublicKey pubKey = priKey.getPublicKey();
        System.out.println("PriKey : " + priKey);
        System.out.println("PubKey : " + pubKey);

        BigInteger a = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("a      : " + a.toString(16));
        BigInteger c = pubKey.encrypt(a);
        System.out.println("c      : " + c.toString(16));
        System.out.println("c.bits : " + c.bitLength());
        BigInteger m = priKey.decrypt(c);
        System.out.println("m      : " + m.toString(16));
        assertEquals(a, m);
    }

    public void testEncAdd() throws PaillierException {
        System.out.println("\n--- Add Cipher Test ---");
        // E(a)*E(b) = E(a+b)
        PaillierPrivateKey priKey = PaillierUtils.generateKey(_keySize);
        PaillierPublicKey pubKey = priKey.getPublicKey();

        BigInteger a = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("a      : " + a.toString(16));
        BigInteger ca = pubKey.encrypt(a);
        System.out.println("ca     : " + ca.toString(16));

        BigInteger b = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("b      : " + b.toString(16));
        BigInteger cb = pubKey.encrypt(b);
        System.out.println("cb     : " + cb.toString(16));

        BigInteger cacb = PaillierUtils.cipherAdd(ca, cb, pubKey);
        System.out.println("cacb   : " + cacb.toString(16));

        BigInteger m = priKey.decrypt(cacb);
        System.out.println("a+b    : " + m.toString(16));

        BigInteger ab = a.add(b).mod(pubKey.getN());
        assertEquals(m, ab);
    }

    public void testScalarMul() throws PaillierException {
        System.out.println("\n--- Scalar Mul Cipher Test ---");
        // E(a)^s = E(sa)
        PaillierPrivateKey priKey = PaillierUtils.generateKey(_keySize);
        PaillierPublicKey pubKey = priKey.getPublicKey();

        BigInteger a = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("a      : " + a.toString(16));
        BigInteger ca = pubKey.encrypt(a);
        System.out.println("ca     : " + ca.toString(16));

        BigInteger s = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("s      : " + s.toString(16));

        BigInteger sca = PaillierUtils.cipherScalarMul(s, ca, pubKey);
        System.out.println("ca^s   : " + sca.toString(16));

        BigInteger m = priKey.decrypt(sca);
        System.out.println("sa     : " + m.toString(16));

        BigInteger sa = s.multiply(a).mod(pubKey.getN());
        assertEquals(m, sa);
    }

    public void testComplex() throws PaillierException {
        System.out.println("\n--- Scalar Mul & Add Cipher Test ---");

        // E(a)^s + E(b) = E(sa + b)
        PaillierPrivateKey priKey = PaillierUtils.generateKey(_keySize);
        PaillierPublicKey pubKey = priKey.getPublicKey();

        BigInteger a = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("a      : " + a.toString(16));
        BigInteger ca = pubKey.encrypt(a);
        System.out.println("ca     : " + ca.toString(16));

        BigInteger s = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("s      : " + s.toString(16));

        BigInteger sca = PaillierUtils.cipherScalarMul(s, ca, pubKey);
        System.out.println("ca^s   : " + sca.toString(16));

        BigInteger b = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("b      : " + b.toString(16));
        BigInteger cb = pubKey.encrypt(b);
        System.out.println("cb     : " + cb.toString(16));

        BigInteger scacb = PaillierUtils.cipherAdd(sca, cb, pubKey);
        System.out.println("sca+cb : " + scacb.toString(16));

        BigInteger m = priKey.decrypt(scacb);
        System.out.println("sa+b   : " + m.toString(16));

        BigInteger sab = s.multiply(a).add(b).mod(pubKey.getN());
        assertEquals(m, sab);

    }

    public void testRandomBaseG() throws PaillierException {
        System.out.println("\n--- Random Base G Test ---");
        PaillierPrivateKey priKey = PaillierUtils.generateKey(_keySize);
        BigInteger g = priKey.genereateBaseG();
        System.out.println("g      : " + g.toString(16));
        PaillierPublicKey pubKey = priKey.getPublicKey(g);
        System.out.println("PriKey : " + priKey);
        System.out.println("PubKey : " + pubKey);

        BigInteger a = new BigInteger(_paramSize, new SecureRandom());
        System.out.println("a      : " + a.toString(16));
        BigInteger c = pubKey.encrypt(a);
        System.out.println("c      : " + c.toString(16));
        System.out.println("c.bits : " + c.bitLength());
        BigInteger m = priKey.decrypt(c, g);
        System.out.println("m      : " + m.toString(16));
        assertEquals(a, m);
    }
}
