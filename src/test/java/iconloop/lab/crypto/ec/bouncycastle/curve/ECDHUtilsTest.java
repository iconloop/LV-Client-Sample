package iconloop.lab.crypto.ec.bouncycastle.curve;

import junit.framework.TestCase;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.KeyPair;

public class ECDHUtilsTest extends TestCase {

    public void testDeriveSecret() throws Exception {
        String curveName = "P-256";

        KeyPair pairA = ECUtils.generateKeyPair(curveName);
        KeyPair pairB = ECUtils.generateKeyPair(curveName);

        SecretKey ab = ECDHUtils.deriveSecret(curveName, "AES", (BCECPublicKey)pairB.getPublic(), (BCECPrivateKey) pairA.getPrivate());
        SecretKey ba = ECDHUtils.deriveSecret(curveName, "AES", (BCECPublicKey)pairA.getPublic(), (BCECPrivateKey) pairB.getPrivate());

        Assert.assertEquals(ab, ba);
    }

    public void testKDF() throws IOException {
        byte[] otherInfo = ECDHUtils.otherInfo("ECDH-ES+A128KW", 128);
        byte[] sampleOtherInfo = Hex.decode("0000000e454344482d45532b413132384b57000000000000000000000080");
        Assert.assertArrayEquals(otherInfo, sampleOtherInfo);

        String algorithm = "AES";
        byte[] keyBytes = Hex.decode("9df8abe9843239eb5651f5eac54e2f536508249940f41b2e424b2c11a73e6a193d35de573445b320cb1bdc7c0f44ae7e");
        SecretKey key1 = new SecretKeySpec(keyBytes, algorithm);

        SecretKey out1 = ECDHUtils.kdf(algorithm, key1, 128, otherInfo);
        byte[] sampleOut = Hex.decode("ffc5ad96264cae97427e5de372bdd25a");
        Assert.assertArrayEquals(out1.getEncoded(), sampleOut);
    }

    public void testKeyWrap() throws InvalidCipherTextException {
        String algorithm = "AES";
        byte[] sampleOutput = Hex.decode("d03263057ae2fe405c0b8e88914e7f264f41a9a41e1ddbf6");
        byte[] cekBytes = Hex.decode("368bb6b9e2a53fbd195c36eaf54ad1c2");
        byte[] kekBytes = Hex.decode("ffc5ad96264cae97427e5de372bdd25a");

        SecretKey kek = new SecretKeySpec(kekBytes, algorithm);
        byte[] wraped = ECDHUtils.keyWrapAES(new SecretKeySpec(cekBytes, algorithm), kek);
        Assert.assertArrayEquals(wraped, sampleOutput);

        SecretKey unWraped = ECDHUtils.keyUnWrapAES(algorithm, wraped, kek);
        Assert.assertArrayEquals(unWraped.getEncoded(), cekBytes);
    }

    public void testAESGCM() throws Exception {
        byte[] payload = "You can trust us to stick with you through thick and thin\\xe2\\x80\\x93to the bitter end. And you can trust us to keep any secret of yours\\xe2\\x80\\x93closer than you keep it yourself. But you cannot trust us to let you face trouble alone, and go off without a word. We are your friends, Frodo.".getBytes();
        byte[] aad = Hex.decode("65794a68624763694f694a46513052494c5556544b3045784d6a684c56794973496d56776179493665794a7264486b694f694a4651794973496d4e7964694936496c41744d7a67304969776965434936496e5643627a5272534642334e6d7469616e673162444234623364795a46397657587043625746364c55644c526c70314e486842526b5a72596c6c70563264316445564c4e6d6c315255527a55545a33546d524f5a7a4d694c434a35496a6f696333417a6344565452326861566b4d795a6d46596457314a4c575535536c5579545738345333427657584a47524849316556424f566e52584e46426e5258646154336c5256454574536d526857546830596a64464d434a394c434a6c626d4d694f694a424d54493452304e4e4969776961326c6b496a6f69634756795a57647961573475644739766130423064574e72596d39796233566e6143356c654746746347786c496e30");
        byte[] iv = Hex.decode("3693bc5e3289fd9c59b178d2");
        byte[] keyBytes = Hex.decode("368bb6b9e2a53fbd195c36eaf54ad1c2");
        byte[] sampleCipher = Hex.decode("1ce9bb09fc2d15636bd2b7510315fce9bb28e48ae60b140e405dbf63de4be89cc758d8478d48d7e8fadecb0a9d4a9db381813364a90264b55eefe7d0d493269631c4432d474165ee07bd803d8a3e1ab471fcc1a3377e0602c8847626097997bdb61baf2e566b01cfeb7b5bcd8976d4c798d20cf45c83b6a488b248bd4e6217b752fb94155c8abafdb9a472e406b1032fe9731a7a914e56c0071c7c3925bd7342b11e95b92d45a549e3085e0ea0e55514227cf43664a77d7937153dd876d53bb69cd056cbbeaf7530164d68f1ecf050ead03c590e5adabf7c2863dbddd0e2a0d23a7d0f66a26aadcf3cb4ae990d6a5fabb98a6277dd44d84ef0ca4cf73f2c912e3df0904a9979461bcdebe82c3af9fd25930e9017730a0ac3026104568130db4cc7fa97");
        byte[] sampleAuthTag = Hex.decode("7f09fa7d9516304a2b139afe10e49f05");
        SecretKey cek = new SecretKeySpec(keyBytes, "AES");
        int authTagLength = 16;

        byte[] encrypted = ECDHUtils.aesGcmEncrypt(payload, cek, iv, aad, authTagLength * 8);
        byte[]cipherText = new byte[encrypted.length - authTagLength];
        System.arraycopy(encrypted, 0, cipherText, 0, cipherText.length);
        byte[] authTag = new byte[authTagLength];
        System.arraycopy(encrypted, cipherText.length, authTag, 0, authTagLength);

        byte[] decrypted = ECDHUtils.aesGcmDecrypt(cipherText, cek, iv, aad, authTag);

        Assert.assertArrayEquals(cipherText, sampleCipher);
        Assert.assertArrayEquals(authTag, sampleAuthTag);
        Assert.assertArrayEquals(decrypted, payload);
    }
}
