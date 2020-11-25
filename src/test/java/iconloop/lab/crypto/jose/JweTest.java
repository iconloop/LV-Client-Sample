package iconloop.lab.crypto.jose;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.SecretKey;
import java.security.KeyPair;

public class JweTest {

    public static void main(String[] ar) throws Exception {

        String kid = "test_key";
        String curve = "secp256r1";
        byte[] payload = "JWE plain payload!!!".getBytes();

        KeyPair myKeyPair = ECUtils.generateKeyPair(curve);
        ECKey myKey = new ECKey(curve, myKeyPair);
        System.out.println("my Key    : " + myKey.toJsonObject(true));

        KeyPair otherKeyPair = ECUtils.generateKeyPair(curve);
        ECKey otherKey = new ECKey(curve, otherKeyPair);
        System.out.println("other Key : " + otherKey.toJsonObject(true));

        // ECDH_ES
        // Sender
        System.out.println("### ECDH-ES, Encryption ###");
        JweEncrypt jwe1 = new JweEncrypt(JoseHeader.JWE_ALG_ECDH_ES, JoseHeader.JWE_ENC_A128GCM, kid);
        SecretKey sKey1 = jwe1.deriveKey(otherKey, myKey, true);
        String encrypted1 = jwe1.encrypt(payload, sKey1);
        System.out.println(" - Encrypted        : " + encrypted1);
        printHeader(" - Encrypted Header : ",encrypted1);
        System.out.println(" - Kek(Sender)      : " + Hex.toHexString(sKey1.getEncoded()));

        // Receiver
        System.out.println("### ECDH-ES, Decryption ###");
        JweDecrypt jwd1 = JweDecrypt.parse(encrypted1);
        SecretKey dKey1 = jwd1.deriveKey(otherKey, null);
        System.out.println(" - Kek(Receiver)    : " + Hex.toHexString(dKey1.getEncoded()));
        byte[] plain1 = jwd1.decrypt(dKey1);
        System.out.println(" - Decrypted        : " + new String(plain1) + "\n");

        // ECDH-ES+A128KW
        // Sender
        System.out.println("### ECDH-ES+A128KW ###");
        JweEncrypt jwe2 = new JweEncrypt(JoseHeader.JWE_ALG_ECDH_ES_A128KW, JoseHeader.JWE_ENC_A128GCM, kid);
        SecretKey sKey2 = jwe2.deriveKey(otherKey, myKey, true);
        String encrypted2 = jwe2.encrypt(payload, sKey2);
        System.out.println(" - Encrypted        : " + encrypted2);
        printHeader(" - Encrypted Header : ",encrypted2);
        System.out.println(" - Kek(Sender)      : " + Hex.toHexString(sKey2.getEncoded()));

        // Receiver
        System.out.println("### ECDH-ES+A128KW, Decryption ###");
        JweDecrypt jwd2 = JweDecrypt.parse(encrypted2);
        SecretKey dKey2 = jwd2.deriveKey(otherKey, null);
        System.out.println(" - Kek(Receiver)    : " + Hex.toHexString(dKey2.getEncoded()));
        byte[] plain2 = jwd2.decrypt(dKey2);
        System.out.println(" - Decrypted        : " + new String(plain2) + "\n");

        // dir
        // Sender
        System.out.println("### dir ###");
        JweEncrypt jwe3 = new JweEncrypt(JoseHeader.JWE_ALG_DIRECT, JoseHeader.JWE_ENC_A128GCM, kid);
        SecretKey sKey3 = sKey2;
        String encrypted3 = jwe3.encrypt(payload, sKey3);
        System.out.println(" - Encrypted        : " + encrypted3);
        printHeader(" - Encrypted Header : ",encrypted3);
        System.out.println(" - Kek(Sender)      : " + Hex.toHexString(sKey3.getEncoded()));

        // Receiver
        System.out.println("### dir, Decryption ###");
        JweDecrypt jwd3 = JweDecrypt.parse(encrypted3);
        SecretKey dKey3 = sKey2;
        System.out.println(" - Kek(Receiver)    : " + Hex.toHexString(dKey3.getEncoded()));
        byte[] plain3 = jwd2.decrypt(dKey3);
        System.out.println(" - Decrypted        : " + new String(plain3) + "\n");
    }

    public static void printHeader(String preFix, String jweString) {
        String[] jwe = jweString.split("\\.");
        byte[] header = Utils.decodeFromBase64UrlSafeString(jwe[0]);
        System.out.println(preFix + " : " + new String(header));
    }
}
