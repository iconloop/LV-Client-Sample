package iconloop.lab.crypto.mpc.ecdsa;

public class MPCKeySharingTest {

    private static final String CURVE_NAME = "secp256k1";
    private MPCKeySharingPlayer[] _players;


    // 키 생성 테스트
    public static void main(String[] ar) throws Exception {

        String keyId  = "KEYID_TEST_01";
        String repoId = "REPOID_TEST_01";
        int n = 4;
        int t = 2;

        MPCConfig config = new MPCConfig(keyId, CURVE_NAME, n, t);

        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.KEY_SHARING_MODE, config);

        MPCKeySharingPlayer[] players = new MPCKeySharingPlayer[n];
        for(int i=0; i<n; i++) {
            players[i] = new MPCKeySharingPlayer(config);
            client.addKeySharingPlayer(players[i]);
        }
        String pubKey = client.keySharing();

        System.out.println("* generate check");
        boolean check = client.checkKeyPair(pubKey);
        System.out.println(check);

        for(int i=0; i<n; i++) {
            System.out.println(players[i].getPlayerKey());
        }
    }
}
