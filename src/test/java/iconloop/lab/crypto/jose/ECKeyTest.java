package iconloop.lab.crypto.jose;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import iconloop.lab.crypto.common.Utils;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

public class ECKeyTest {

    public static void main(String[] ar) throws Exception {
        String curveSpec = "secp256r1";

        AlgorithmParameterSpec pairParams;
        if(curveSpec.equals("curve25519")) {
            X9ECParameters ecP = CustomNamedCurves.getByName(curveSpec);
            pairParams = EC5Util.convertToSpec(ecP);
        } else {
            pairParams = new ECGenParameterSpec(curveSpec);
        }


        KeyPairGeneratorSpi.EC gen = new KeyPairGeneratorSpi.EC();
        gen.initialize(pairParams, new SecureRandom());
        KeyPair pair = gen.generateKeyPair();

        System.out.println(pair.getPublic().getClass());
        System.out.println(pair.getPrivate().getClass());

        ECKey key = new ECKey(curveSpec, pair);
        toPublicKey(key.toJsonObject(true));

    }

    public static void toPublicKey(JsonObject epkObject) {
        String crv = epkObject.get(JoseHeader.JWK_CURVE_NAME).getAsString();

        String strX = epkObject.get(JoseHeader.JWK_KEY_X).getAsString();
        String strY = epkObject.get(JoseHeader.JWK_KEY_Y).getAsString();
        BigInteger x = new BigInteger(1, Utils.decodeFromBase64UrlSafeString(strX));
        BigInteger y = new BigInteger(1, Utils.decodeFromBase64UrlSafeString(strY));

        JsonElement tmp = epkObject.get(JoseHeader.JWK_KEY_D);
        BigInteger d = null;
        if(tmp != null) {
            String strD = tmp.getAsString();
            d = new BigInteger(1, Utils.decodeFromBase64UrlSafeString(strD));
        }
System.out.println(d);
        BCECPublicKey publicKey = getECPublicKey(crv, x, y);
System.out.println(publicKey);
//        ECPrivateKey privateKey = Utils.getECPrivateKey(JCE_ALGORITHM, crv, d);
        BCECPrivateKey privateKey = getECPrivateKey(crv, d);
System.out.println(privateKey.getD());
    }

    public static BCECPublicKey getECPublicKey(String curveSpec, BigInteger x, BigInteger y) {
        ProviderConfiguration configuration = BouncyCastleProvider.CONFIGURATION;

        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(curveSpec);
        ECNamedCurveSpec params = new ECNamedCurveSpec(curveSpec, spec.getCurve(), spec.getG(), spec.getN());

        ECPoint point = new ECPoint(x, y);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);

//        org.bouncycastle.jce.spec.ECParameterSpec s = configuration.getEcImplicitlyCa();
//        org.bouncycastle.jce.spec.ECParameterSpec s = new org.bouncycastle.jce.spec.ECParameterSpec();
//
//        System.out.println(s);
//        System.out.println(s.getCurve().createPoint(x, y));
//        ECPublicKeyParameters ecPublicKey = new ECPublicKeyParameters(s.getCurve().createPoint(x, y), EC5Util.getDomainParameters(configuration, (ECParameterSpec)null));
//
//        return new BCECPublicKey("EC", ecPublicKey, configuration);
        return new BCECPublicKey("EC", pubKeySpec, configuration);
//        KeyFactorySpi.EC fact = new KeyFactorySpi.EC();
//        fact.generatePublic(pubKeySpec);
//        new ECPublicKeyParameters(s.getCurve().createPoint(spec.getQ().getAffineXCoord().toBigInteger(), spec.getQ().getAffineYCoord().toBigInteger()), EC5Util.getDomainParameters(configuration, (ECParameterSpec)null));
//
//        return BCECPublicKey(
//                ECPublicKey key,
//                ProviderConfiguration configuration)
//        return null;
    }

    public static BCECPrivateKey getECPrivateKey(String curveSpec, BigInteger d) {
        ProviderConfiguration configuration = BouncyCastleProvider.CONFIGURATION;

        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(curveSpec);
        ECNamedCurveSpec params = new ECNamedCurveSpec(curveSpec, spec.getCurve(), spec.getG(), spec.getN());
        ECPrivateKeySpec priKeySpec = new ECPrivateKeySpec(d, params);

        return new BCECPrivateKey("EC", priKeySpec, configuration);
    }


}
