package iconloop.lab.crypto.mpc.ecdsa;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;
import iconloop.lab.crypto.he.paillier.PaillierException;
import iconloop.lab.crypto.he.paillier.PaillierPrivateKey;
import iconloop.lab.crypto.he.paillier.PaillierUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class MPCSigningTest {

    static String _publicKey;

    // 키 생성, 서명 테스트
    public static void main(String[] ar) throws Exception {
        byte[] message = "test".getBytes();

        String CURVE_NAME = "secp256k1";
        String keyId = "KEYID_TEST_01";
        String repoId = "REPOID_TEST_01";
        int n = 4;
        int t = 2;

        MPCSigningTest test = new MPCSigningTest();

        System.out.println("### Key Sharing ###");
        MPCConfig config = new MPCConfig(keyId, CURVE_NAME, n, t);
        String[] sharedKeys = test.keySharing(config, repoId);

        System.out.println("\n### MPC Signing ###");
        byte[] tbs = Utils.sha256Digest(message);
        String sign = test.sign(tbs, config, sharedKeys);

        ECParameterSpec paramSpec = ECUtils.getECParameterSpec(CURVE_NAME);
        BCECPublicKey pub = ECUtils.getBCECPublicKey(paramSpec, Hex.decode(_publicKey));

        System.out.println(" * PlainText : " + Hex.toHexString(message));
        System.out.println(" * Signature : " + sign);
        System.out.println(" * PublicKey : " + _publicKey);

        BigInteger[] sig = ECUtils.decodeStdDSASignature(Hex.decode(sign));
        byte[] hashedMessage = Utils.sha256Digest(message);
        System.out.println(" * Verify    : " + ECUtils.verifyECDSA(CURVE_NAME, hashedMessage, pub, sig[0], sig[1]));
    }

    public String sign(byte[] message, MPCConfig config, String[] sharedKeys) throws MPCEcdsaException, PaillierException, Exception {
        String repoId = "repository_01";

        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.SIGNING_MODE, config);

        for(String sharedKey : sharedKeys) {
            PlayerKey key = new PlayerKey(sharedKey);
            client.addSignPlayer(new MPCSigningPlayer(key, PaillierUtils.generateKey(1024)));
        }

//        sharedKeys[0] = "{\"keyId\":\"KEYID_TEST_01\",\"crv\":\"secp256k1\",\"t\":\"2\",\"index\":\"1\",\"others\":[\"2\",\"3\",\"4\"],\"xi\":\"ff0f7e4678b6016ceb4c9b977c9ca2883e0e09952c7af494fb7e3aa0318c238b\",\"uiG\":\"038ce82fa39c4b6bcca66bc1f4d298e09f83875fe3ce7769bfce048cecd7a04f44\"}";
//        sharedKeys[1] = "{\"keyId\":\"KEYID_TEST_01\",\"crv\":\"secp256k1\",\"t\":\"2\",\"index\":\"2\",\"others\":[\"1\",\"3\",\"4\"],\"xi\":\"70ba01165d3d43103a48abf91988d1dec4ca292e5e2406827e77a15af1adbe22\",\"uiG\":\"03a90a5bffac8ee553b3a599e2bba4ed97ca3c3f3a3c218592e05fbb3bf6e9ef34\"}";
//        sharedKeys[2] = "{\"keyId\":\"KEYID_TEST_01\",\"crv\":\"secp256k1\",\"t\":\"2\",\"index\":\"3\",\"others\":[\"1\",\"2\",\"4\"],\"xi\":\"02e780424f6b90b02e9795cae3534bb3d596892e89d3542f56286f3e1aecda68\",\"uiG\":\"0223afc3cc45885c253ebd0a2768734609cd845ab672b073d0e0e903af80ae92f6\"}";
//        client.addSignPlayer(new MPCSigningPlayer(new PlayerKey(sharedKeys[0]), getHEPK(0)));
//        client.addSignPlayer(new MPCSigningPlayer(new PlayerKey(sharedKeys[1]), getHEPK(1)));
//        client.addSignPlayer(new MPCSigningPlayer(new PlayerKey(sharedKeys[2]), getHEPK(2)));
//        client.addSignPlayer(new MPCSigningPlayer(new PlayerKey(sharedKeys[3]), 1024));

        return client.signing(message);
    }

    public String[] keySharing(MPCConfig config, String repoId) throws MPCEcdsaException {

        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.KEY_SHARING_MODE, config);

        int n = config.getNumberOfPlayers();
        MPCKeySharingPlayer[] players = new MPCKeySharingPlayer[n];
        for (int i = 0; i < n; i++) {
            players[i] = new MPCKeySharingPlayer(config);
            client.addKeySharingPlayer(players[i]);
        }
        _publicKey = client.keySharing();

        System.out.println(" * generate check");
        boolean check = client.checkKeyPair(_publicKey);
        System.out.println("   - result: " + check);

        return client.getPlayerKey();
    }

    private PaillierPrivateKey getHEPK(int index) {
        BigInteger[] hepk = new BigInteger[4];
        if(index == 0) {
            hepk[0] = new BigInteger(1, Hex.decode("a6167f82493ab0f4c8352785c61134bd294294ae6fd2f5e19a2c55f895ef51206ac986d1b86c00ef102eb4a6ff5c080f0c364139c213c8c232f5e61c719eb414b39c32685d89e6d3aec4ebc024f6affbfaf09cacc81c3d8f25225a152beca4bc6b987999e99a3d9d98609d18d3ee1d46f460200c3264f0c8359c0e16e614fc85"));
            hepk[1] = new BigInteger(1, Hex.decode("eab822953410b4a5ca59e3c29b2caa39dc1abb1db471452dbaa671a9a951190f7debc45660ada9f945215188c9f31d43490cc044c126e6d42d38da049732abeb"));
            hepk[2] = new BigInteger(1, Hex.decode("b5256b5eb4d137373e4ef50973fef4238a731d443bf572e357cfdfc7db38581a6536902c54fab6f1dd8c2ec091fbfc53fbab53376f92fc603bfe6cdfc5650d4f"));
        } else if(index == 1) {
            hepk[0] = new BigInteger(1, Hex.decode("a05a6953075590d47d9a9352051c53d0a0cab449847fb62478438d32409efa34402169b8f17ab57f6f8d0534d984da9355a4efb7895c56e376d790faa9408ca6b51f735caf7caf30343f3124032b326b4e39d417079d01d5705624c8ce20b7026a6488fa171726e45d418032290b12827f019b20efcdab03ecba0d7d5ed5ac97"));
            hepk[1] = new BigInteger(1, Hex.decode("d06c5f83e5b8ce21f119a1ad06e486687869f8c2a59077ae3179f6832088998b74d1d739db64f359c89c81bdc91813acdb015cc77add9c2b8301f54719ea7ed7"));
            hepk[2] = new BigInteger(1, Hex.decode("c4f4f6c321a9122bf4adb8c0cf917b221847f27000c2387a9aab1e72c171b25b400529967249b10bc7f151e86215455011898247a9b76d662cbc8c91d5794841"));
        } else if(index == 2) {
            hepk[0] = new BigInteger(1, Hex.decode("a174d7f28f84ee50ee44046893fc94907517ee1dfe5035fdfff63fc62d6c3bd43c09288071dea939efb01cad263ff78f502654e19b0fe7b2d19da6c1b632fb9743480c2685433981f74812c9f520f439d449f11087d0572cef56946b4167864fee1274ab42f871c0c9aaa0c50666a40c3837e20b4bb82aa849fa36bafe7f98ed"));
            hepk[1] = new BigInteger(1, Hex.decode("f4ff158141cf9e5ff523ef3a838a0da348e7bcdfb219da00a77b9b8658cde5abd3215810aa88e5e11881ebac931fadd957a858a4a38a37e20e15b4324778d6d7"));
            hepk[2] = new BigInteger(1, Hex.decode("a8b53c1073f14b890f0aa34c813e3e753f5ce5e993796af74cbe9fa7c7c0b8c62a184d60822b2ce21ec39deeb3846980fd3a0e285e7c0d296b4a8facd0f7c9db"));
        } else {
            hepk[0] = new BigInteger(1, Hex.decode("ce93388a81f1c73e8183fb1b3b19236a197042cb84d6813e3606b518f617ac6dcccb311169f209fe9593df1f40befb5cdf9a793a0dee6e2594fcea7b45b69e3be97ec9ac9493517a0bc90ecdafa323da27148b9722b0fd23ba6a9c32a5f0288ceaab2859328c88e444b5b4e1c96e7a7bf629ef66d883b9ffe403adb503211f29"));
            hepk[1] = new BigInteger(1, Hex.decode("fbcd8748fd837013a7f08590ee47728cc4e0e95e3aadeca3a931893a659f6d91ab3f0f0df8facee92df61a66e1e870f78c040ab04a5ad28518c1b30d1580bcb5"));
            hepk[2] = new BigInteger(1, Hex.decode("d204b34b0a342737ba20dee3687ad4886564984bf268023685dcb9e9811933ec3555bdd18d88f6ed562ec247d445aeedfc58326ad6d79501e8e99ef379bf1525"));
        }
        return new PaillierPrivateKey(hepk[0], hepk[1], hepk[2]);
    }
}
