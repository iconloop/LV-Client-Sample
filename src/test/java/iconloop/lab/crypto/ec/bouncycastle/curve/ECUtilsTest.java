package iconloop.lab.crypto.ec.bouncycastle.curve;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.jose.ECKey;
import junit.framework.TestCase;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;

public class ECUtilsTest extends TestCase {

    public void testGenerateKeyPair() throws Exception {
        String curveName = "secp256r1";

        byte[] hashedMessage = "abcedfghigklmnopqrstuvwxyz".getBytes();

        KeyPair keyPair = ECUtils.generateKeyPair(curveName);
        BCECPublicKey pubKey = (BCECPublicKey)keyPair.getPublic();
        BCECPrivateKey priKey = (BCECPrivateKey)keyPair.getPrivate();

        BigInteger[] sig = ECUtils.signECDSA(curveName, hashedMessage, priKey);
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, pubKey, sig[0], sig[1]));

        curveName = "curve25519";
        KeyPair keyPair2 = ECUtils.generateKeyPair(curveName);
        BCECPublicKey pubKey2 = (BCECPublicKey)keyPair2.getPublic();
        BCECPrivateKey priKey2 = (BCECPrivateKey)keyPair2.getPrivate();

        BigInteger[] sig2 = ECUtils.signECDSA(curveName, hashedMessage, priKey2);
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, pubKey2, sig2[0], sig2[1]));
    }

    public void testECKey() throws Exception {
        byte[] hashedMessage = Utils.getRandomBytes(32);
        String curveName = "secp256r1";

        ECParameterSpec paramSpec = ECUtils.getECParameterSpec(curveName);
        byte[] xBytes = Utils.decodeFromBase64UrlSafeString("bsxD5dXR1WDaZ0WL1U7_CeC0x3yvBIa-2XiDPk4UdaQ");
        byte[] yBytes = Utils.decodeFromBase64UrlSafeString("1AQ9z86rzBrscLXE1cUOZyCjCciS-3vlAn_Xp0uAjNE");
        byte[] dBytes = Utils.decodeFromBase64UrlSafeString("Nix-xWemi4RLJZa3Di1A0g3O-thpf98ZG-3Ka6SQ1N0");

        BigInteger x = new BigInteger(1, xBytes);
        BigInteger y = new BigInteger(1, yBytes);
        BigInteger d = new BigInteger(1, dBytes);

        BCECPublicKey pubKey = ECUtils.getBCECPublicKey(paramSpec, x, y);
        BCECPrivateKey priKey = ECUtils.getBCECPrivateKey(paramSpec, d);

        ECPoint point1 = paramSpec.getCurve().createPoint(pubKey.getQ().getAffineXCoord().toBigInteger(), pubKey.getQ().getAffineYCoord().toBigInteger());
        ECPoint point2 = paramSpec.getG().multiply(priKey.getD());
        Assert.assertTrue(point1.equals(point2));

        byte[] encodePoint = point1.getEncoded(true);
        BCECPublicKey pubKey1 = ECUtils.getBCECPublicKey(paramSpec, encodePoint);
        Assert.assertEquals(pubKey, pubKey1);

        encodePoint = Utils.concat(new byte[]{(byte)0x04}, xBytes, yBytes);
        BCECPublicKey pubKey2 = ECUtils.getBCECPublicKey(paramSpec, encodePoint);
        Assert.assertEquals(pubKey, pubKey2);


        BigInteger[] sig = ECUtils.signECDSA(curveName, hashedMessage, priKey);
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, pubKey, sig[0], sig[1]));
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, pubKey1, sig[0], sig[1]));
        Assert.assertTrue(ECUtils.verifyECDSA(curveName, hashedMessage, pubKey2, sig[0], sig[1]));

        ECKey ecKey = new ECKey(curveName, pubKey, priKey);
        BigInteger[] sig2 = ECUtils.signECDSA(hashedMessage, ecKey);
        Assert.assertTrue(ECUtils.verifyECDSA(hashedMessage, ecKey, sig[0], sig[1]));
        Assert.assertTrue(ECUtils.verifyECDSA(hashedMessage, new ECKey(curveName, pubKey1, priKey), sig[0], sig[1]));
        Assert.assertTrue(ECUtils.verifyECDSA(hashedMessage, new ECKey(curveName, pubKey2, priKey), sig[0], sig[1]));

    }

    public void testEncodeStdDSASignature() throws IOException {
        SecureRandom rng = new SecureRandom();
        BigInteger a = new BigInteger(256, rng);
        BigInteger b = new BigInteger(256, rng);

        byte[] sig = ECUtils.encodeStdDSASignature(a, b);
        BigInteger[] desig = ECUtils.decodeStdDSASignature(sig);
    }
}
