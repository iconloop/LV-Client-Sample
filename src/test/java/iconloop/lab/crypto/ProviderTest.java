package iconloop.lab.crypto;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECDHUtils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;
import iconloop.lab.crypto.jose.ECKey;
import junit.framework.TestCase;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

public class ProviderTest extends TestCase {

    private static final SecureRandom RND = new SecureRandom();
    private static final String PROVIDER = "BC";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void testSHA256() throws Exception {
        String algorithm = "SHA-256";
        byte[] message1 = RND.generateSeed(32);
        byte[] message2 = RND.generateSeed(32);
        byte[] message3 = RND.generateSeed(32);

        byte[] digest1I = Utils.sha256Digest(message1);
        byte[] digest1B = digest(algorithm, message1);
        Assert.assertArrayEquals(digest1I, digest1B);

        byte[] digest2I = Utils.sha256Digest(message1, message2, message3);
        byte[] digest2B = digest(algorithm, message1, message2, message3);
        Assert.assertArrayEquals(digest2I, digest2B);
    }

    public void testSHA3256() throws Exception {
        String algorithm = "SHA3-256";
        byte[] message1 = RND.generateSeed(32);
        byte[] message2 = RND.generateSeed(32);
        byte[] message3 = RND.generateSeed(32);

        byte[] digest1I = Utils.sha3Digest(message1);
        byte[] digest1B = digest(algorithm, message1);
        Assert.assertArrayEquals(digest1I, digest1B);

        byte[] digest2I = Utils.sha3Digest(message1, message2, message3);
        byte[] digest2B = digest(algorithm, message1, message2, message3);
        Assert.assertArrayEquals(digest2I, digest2B);
    }

    public void testECDH() throws Exception {
        String curveName = "P-256";
        String algorithm = "AES";

        KeyPair sender = ECUtils.generateKeyPair(curveName);
        KeyPair receiver = ECUtils.generateKeyPair(curveName);

        SecretKey ab = ECDHUtils.deriveSecret(curveName, algorithm, (BCECPublicKey)receiver.getPublic(), (BCECPrivateKey) sender.getPrivate());

        KeyAgreement agreement = KeyAgreement.getInstance("ECDH", PROVIDER);
        agreement.init((ECPrivateKey)receiver.getPrivate());
        agreement.doPhase((ECPublicKey)sender.getPublic(), true);
        byte[] secret = agreement.generateSecret();
        SecretKey ba = new SecretKeySpec(secret, algorithm);

        Assert.assertEquals(ab, ba);
    }

    public void testAESGCM() throws Exception {
        String algorithm = "AES";
        byte[] plain = Utils.getRandomBytes(28);
        byte[] iv = Utils.getRandomBytes(16);
        byte[] aad = Utils.getRandomBytes(44);
        int authTagLength = 16;
        byte[] keyBytes = Utils.getRandomBytes(16);
        SecretKey sKey = new SecretKeySpec(keyBytes, algorithm);

        byte[] cipherText = ECDHUtils.aesGcmEncrypt(plain, sKey, iv, aad, authTagLength * 8);

        Cipher deCipher = Cipher.getInstance(algorithm + "/GCM/NoPadding", PROVIDER);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(authTagLength * 8, iv);
        deCipher.init(Cipher.DECRYPT_MODE, sKey, gcmSpec);
        deCipher.updateAAD(aad);
        byte[] output1 = deCipher.doFinal(cipherText);
        Assert.assertArrayEquals(plain, output1);

        Cipher enCipher = Cipher.getInstance(algorithm + "/GCM/NoPadding", PROVIDER);
        enCipher.init(Cipher.ENCRYPT_MODE, sKey, gcmSpec);
        enCipher.updateAAD(aad);
        byte[] cipherText2 = enCipher.doFinal(plain);
        Assert.assertArrayEquals(cipherText, cipherText2);
    }

    public void testECDSA() throws Exception {
        String curveName = "secp256r1";
        String algorithm = "ECDSA";
        byte[] plain = Utils.getRandomBytes(100);
        byte[] hashedMessage = Utils.sha256Digest(plain);

        KeyPair pair = ECUtils.generateKeyPair(curveName);
        BCECPrivateKey privateKey = (BCECPrivateKey)pair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey)pair.getPublic();

        BigInteger[] sig = ECUtils.signECDSA(curveName, hashedMessage, privateKey);
        byte[] signature1 = ECUtils.encodeStdDSASignature(sig[0], sig[1]);
        Assert.assertTrue(verifyECDSA(plain, signature1, publicKey));

        ECKey ecKey = new ECKey(curveName, pair);
        sig = ECUtils.signECDSA(hashedMessage, ecKey);
        byte[] signature2 = ECUtils.encodeStdDSASignature(sig[0], sig[1]);
        Assert.assertTrue(verifyECDSA(plain, signature2, publicKey));

        byte[] signature3 = signECDSA(plain, privateKey);
        sig = ECUtils.decodeStdDSASignature(signature3);
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, publicKey, sig[0], sig[1]));

        Assert.assertTrue(ECUtils.verifyECDSA(hashedMessage, ecKey, sig[0], sig[1]));
    }

    public void testAESCBCP7() throws Exception {
        byte[] plainText = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()".getBytes();
        byte[] iv = Utils.getRandomBytes(16);
        byte[] key = Utils.getRandomBytes(16);
        SecretKey sKey = new SecretKeySpec(key, "AES");

        byte[] enc1 = Utils.aesEncrypt(plainText, sKey, iv);
        byte[] dec1 = decryptAESCBC(enc1, sKey, iv);
        Assert.assertArrayEquals(plainText, dec1);

        byte[] enc2 = encryptAESCBC(plainText, sKey, iv);
        byte[] dec2 = Utils.aesDecrypt(enc2, sKey, iv);
        Assert.assertArrayEquals(plainText, dec2);
        Assert.assertArrayEquals(enc1, enc2);
    }

    private static byte[] encryptAESCBC(byte[] plainText, SecretKey sKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, sKey, new IvParameterSpec(iv));
        return cipher.doFinal(plainText);
    }

    private static byte[] decryptAESCBC(byte[] cipherText, SecretKey sKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, sKey, new IvParameterSpec(iv));
        return cipher.doFinal(cipherText);
    }

    private static byte[] signECDSA(byte[] plain, PrivateKey privateKey) throws Exception {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA", PROVIDER);
        ecdsa.initSign(privateKey);
        ecdsa.update(plain);
        return ecdsa.sign();
    }

    private static boolean verifyECDSA(byte[] plain, byte[] signature, PublicKey publicKey) throws Exception {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA", PROVIDER);
        ecdsa.initVerify(publicKey);
        ecdsa.update(plain);
        return ecdsa.verify(signature);
    }

    private static byte[] digest(String algorithm, byte[]... messages) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm, PROVIDER);
        for(byte[] message : messages)
            md.update(message);
        return md.digest();
    }

//    public static void main(String[] ar) throws Exception {
//        byte[] msg = "message".getBytes();
//
//        byte[] dig1 = sha256Digest(msg);
//        System.out.println(Hex.toHexString(dig1));
//        dig1 = sha256Digest(dig1);
//        System.out.println("dig1 : " + Hex.toHexString(dig1));
//        byte[] dig2 = sha256Digest(msg);
//        System.out.println(Hex.toHexString(dig2));
//        dig2 = sha256Digest(dig2);
//        System.out.println(Hex.toHexString(dig2));
//        System.out.println(Arrays.areEqual(dig1, dig2));
//
//        byte[] a = "ab12".getBytes();
//        String aa = Utils.encodeToBase64UrlSafeString(a);
//        System.out.println(aa);
//        System.out.println(aa.length());
//        String bb = Base64.getEncoder().encodeToString(a);
//        System.out.println(bb);
//        System.out.println(bb.length());
//        System.out.println(Hex.toHexString(Utils.encodeToBase64UrlSafeString(a).getBytes()));
//        System.out.println(Hex.toHexString(Utils.decodeFromBase64UrlSafeString(aa)));
//
//        BigInteger c = new BigInteger(256, new SecureRandom());
//        System.out.println("Uns : " + c.toString(16));
//        System.out.println("Uns : " + Hex.toHexString(c.toByteArray()));
//        System.out.println("Uns : " + Hex.toHexString(bigIntToUnsigned(c, 32)));
//    }

}
