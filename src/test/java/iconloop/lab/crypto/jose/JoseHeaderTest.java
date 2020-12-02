package iconloop.lab.crypto.jose;

import com.google.gson.JsonObject;
import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;

import java.security.KeyPair;

public class JoseHeaderTest {

    public static void main(String[] ar) throws Exception {
        JoseHeader header = new JoseHeader("ES256", "aaa");
        JsonObject h1 = header.toJsonObject();
        System.out.println("Header 1 : " + h1);

        header = JoseHeader.parse(h1);
        JsonObject h2 = header.toJsonObject();
        System.out.println("Header 2 : " + h2);

        KeyPair key = ECUtils.generateKeyPair("secp256r1");
        ECKey dhKey = new ECKey("secp256r1", key);
        System.out.println("DH Key   : " + dhKey.toJsonObject(true));
        JoseHeader dhHeader = new JoseHeader("ECDH-ES","bbb","A128GCM", dhKey);
        JsonObject h3 = dhHeader.toJsonObject();
        System.out.println("Header 3 : " + h3);

        dhHeader = JoseHeader.parse(h3);
        JsonObject h4 = dhHeader.toJsonObject();
        System.out.println("Header 4 : " + h4);

    }
}
