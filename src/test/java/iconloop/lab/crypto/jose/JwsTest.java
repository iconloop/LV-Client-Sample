package iconloop.lab.crypto.jose;

import com.google.gson.JsonObject;
import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;

import java.math.BigInteger;
import java.security.KeyPair;

public class JwsTest {

    public static void main(String[] ar) throws Exception {
        String kid = "test_key";
        String curve = "secp256r1";//"curve25519"
        JsonObject payload = new JsonObject();
        payload.addProperty("test", "JWS test payload");

        KeyPair myKeyPair = ECUtils.generateKeyPair(curve);
        ECKey myKey = new ECKey(curve, myKeyPair);
        System.out.println("my Key    : " + myKey.toJsonObject(true));

        JwsSign jws = new JwsSign(JoseHeader.JWS_ALG_ES256, kid);
        String strJws = jws.sign(payload, myKey, false);

        System.out.println(strJws);

        JwsVerify jwsV = JwsVerify.parse(strJws);
        System.out.println(jwsV.verify(myKey));
        System.out.println(jwsV.getPayload());

        check(curve, strJws, myKey);

        JwsSign jwsN = new JwsSign(JoseHeader.JWS_ALG_NONE, kid);
        String strJwsN = jwsN.sign(payload, null, false);
        System.out.println("JWS None : " + strJwsN);

        jwsV = JwsVerify.parse(strJwsN);
        System.out.println(jwsV.verify());
        System.out.println(jwsV.getPayload());
    }

    public static void check(String curve, String jws, ECKey verKey) throws Exception {
        int sigIndex = jws.lastIndexOf(".");
        String input = jws.substring(0, sigIndex);
        byte[] tbs = input.getBytes();

        BigInteger[] value = decodeECDSASignature(jws.substring(sigIndex+1));
        System.out.println(ECUtils.verifyECDSA(curve, tbs, verKey.getPublicKey(), value[0], value[1]));

    }

    private static BigInteger[] decodeECDSASignature(String b64Signature) throws JoseException {
        byte[] encodedSignValue = Utils.decodeFromBase64UrlSafeString(b64Signature);
        int rSize = 32;
        if(encodedSignValue.length != (32*2))
            throw new JoseException("Invalid signature encoding");

        byte[] rByte = new byte[rSize];
        byte[] sByte = new byte[rSize];
        System.arraycopy(encodedSignValue, 0, rByte, 0, rSize);
        System.arraycopy(encodedSignValue, rSize, sByte, 0, rSize);
        return new BigInteger[]{new BigInteger(1, rByte), new BigInteger(1, sByte)};
    }
}
