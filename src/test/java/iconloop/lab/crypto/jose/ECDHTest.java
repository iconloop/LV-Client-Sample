package iconloop.lab.crypto.jose;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.EC;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

public class ECDHTest {

    public static final String PROVIDER = "BC";
    public static final String CURVE = "secp256r1";
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] ar) throws Exception {
        AlgorithmParameterSpec pairParams = new ECGenParameterSpec(CURVE);

        KeyPairGeneratorSpi.EC gen = new KeyPairGeneratorSpi.EC();
        gen.initialize(pairParams, new SecureRandom());
        KeyPair pairA = gen.generateKeyPair();

        gen.initialize(pairParams, new SecureRandom());
        KeyPair pairB = gen.generateKeyPair();

        SecretKey ab = deriveSecret("AES", (ECPublicKey)pairB.getPublic(), (ECPrivateKey) pairA.getPrivate());
        SecretKey ba = deriveSecret("AES", (BCECPublicKey)pairA.getPublic(), (BCECPrivateKey) pairB.getPrivate());
        System.out.println("AB : " + Hex.toHexString(ab.getEncoded()));
        System.out.println("BA : " + Hex.toHexString(ba.getEncoded()));

        byte[] wraped = keyWrap(ab, ba);
        System.out.println("AB Wrap : " + Hex.toHexString(wraped) + ", " + wraped.length);
        SecretKey unWraped = keyUnWrap("AESWrap", "AES", wraped, ba);
        System.out.println("AB UnWr : " + Hex.toHexString(unWraped.getEncoded()) + ", " + unWraped.getEncoded().length);

        byte[] wraped2 = keyWrap("AESWrap", ba, ab);
        System.out.println("BA Wrap : " + Hex.toHexString(wraped2) + ", " + wraped2.length);
        byte[] unWraped2 = keyUnWrap(wraped2, ab);
        System.out.println("AB UnWr : " + Hex.toHexString(unWraped2) + ", " + unWraped2.length);

        byte[] otherInfo = Hex.decode("000000074131323847434d000000000000000000000080");
        byte[] kdf1 = kdf("SHA-256", "AES", ab, 128, otherInfo).getEncoded();
        System.out.println("P KDF : " + Hex.toHexString(kdf1));
        byte[] kdf2 = kdf("AES", ab, 128, otherInfo).getEncoded();
        System.out.println("B KDF : " + Hex.toHexString(kdf2));
    }

    public static SecretKey deriveSecret(String encAlgorithm, ECPublicKey receiverPublic, ECPrivateKey senderPrivate) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
        KeyAgreement agree = KeyAgreement.getInstance("ECDH", PROVIDER);
        agree.init(senderPrivate);
        agree.doPhase(receiverPublic, true);
        byte[] secret = agree.generateSecret();
        System.out.println("PV Secret : " + Hex.toHexString(secret));
        return new SecretKeySpec(secret, encAlgorithm);
    }

    public static SecretKey deriveSecret(String encAlgorithm, BCECPublicKey receiverPublic, BCECPrivateKey senderPrivate) {
        EC ec = new EC(CURVE);
        ECDomainParameters ecParams = ec.getDomainParameters();
        ECPrivateKeyParameters priParam = new ECPrivateKeyParameters(senderPrivate.getS(), ecParams);
        ECPublicKeyParameters pubParam = new ECPublicKeyParameters(receiverPublic.getQ(), ecParams);

        ECDHBasicAgreement agree = new ECDHBasicAgreement();
        agree.init(priParam);
        BigInteger secret = agree.calculateAgreement(pubParam);
        System.out.println("BC Secret : " + Hex.toHexString(secret.toByteArray()));
        return new SecretKeySpec(Utils.bigIntToUnsigned(secret, (ec.getFieldSize()+7)/8), encAlgorithm);
    }

    public static byte[] keyWrap(SecretKey cek, SecretKey kek) {
        RFC3394WrapEngine cipher = new RFC3394WrapEngine(new AESEngine());
        cipher.init(true, new KeyParameter(kek.getEncoded()));
        byte[] in = cek.getEncoded();
        return cipher.wrap(in, 0, in.length);
    }

    public static byte[] keyUnWrap(byte[] encryptedKey, SecretKey kek) throws InvalidCipherTextException {
        RFC3394WrapEngine cipher = new RFC3394WrapEngine(new AESEngine());
        cipher.init(false, new KeyParameter(kek.getEncoded()));
        return cipher.unwrap(encryptedKey, 0, encryptedKey.length);
    }

    public static byte[] keyWrap(String keyRapAlgorithm, SecretKey cek, SecretKey kek) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(keyRapAlgorithm, PROVIDER);
        cipher.init(Cipher.WRAP_MODE, kek);
        return cipher.wrap(cek);
    }

    public static SecretKey keyUnWrap(String unWrapAlgorithm, String keyAlgorithm, byte[] encryptedKey, SecretKey kek)  throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(unWrapAlgorithm, PROVIDER);
        cipher.init(Cipher.UNWRAP_MODE, kek);
        return (SecretKey)cipher.unwrap(encryptedKey, keyAlgorithm, Cipher.SECRET_KEY);
    }

    public static SecretKey kdf(String hashAglrotihm, String encAlgorithm, SecretKey sharedSecret, int keyLength, byte[] otherInfo) throws NoSuchAlgorithmException, NoSuchProviderException {
        MessageDigest digest = MessageDigest.getInstance(hashAglrotihm, PROVIDER);
        int hLen = digest.getDigestLength();

        byte[] hashBuf;
        int outputLen = 0;

        byte[] out = new byte[(keyLength + 7)/8];

        byte[] shared = sharedSecret.getEncoded();

        int round = (out.length + (hLen -1))/hLen;
        for(int i=1; i<=round; i++) {
            byte[] counter = intToBytes(i);

            digest.update(counter, 0, counter.length);
            digest.update(shared, 0, shared.length);
            digest.update(otherInfo, 0, otherInfo.length);

            hashBuf = digest.digest();

            if(i == round)
                System.arraycopy(hashBuf, 0, out, outputLen, out.length - (i-1)*hLen);
            else
                System.arraycopy(hashBuf, 0, out, outputLen, hLen);
            outputLen += hLen;
        }
        return new SecretKeySpec(out, encAlgorithm);
    }

    public static SecretKey kdf(String encAlgorithm, SecretKey sharedSecret, int keyLength, byte[] otherInfo) {
        int hLen = 32;//digest.getDigestLength();

        byte[] hashBuf;
        int outputLen = 0;

        byte[] out = new byte[(keyLength + 7)/8];

        byte[] shared = sharedSecret.getEncoded();

        int round = (out.length + (hLen -1))/hLen;
        for(int i=1; i<=round; i++) {
            byte[] counter = intToBytes(i);

            hashBuf = Utils.sha256Digest(counter, shared, otherInfo);
//            digest.update(counter, 0, counter.length);
//            digest.update(shared, 0, shared.length);
//            digest.update(otherInfo, 0, otherInfo.length);
//
//            hashBuf = digest.digest();

            if(i == round)
                System.arraycopy(hashBuf, 0, out, outputLen, out.length - (i-1)*hLen);
            else
                System.arraycopy(hashBuf, 0, out, outputLen, hLen);
            outputLen += hLen;
        }
        return new SecretKeySpec(out, encAlgorithm);
    }

    public static byte[] intToBytes(int intValue) {
        byte[] res = new byte[4];
        res[0] = (byte) (intValue >>> 24);
        res[1] = (byte) ((intValue >>> 16));
        res[2] = (byte) ((intValue >>> 8));
        res[3] = (byte) (intValue & 0xFF);
        return res;
    }

}
