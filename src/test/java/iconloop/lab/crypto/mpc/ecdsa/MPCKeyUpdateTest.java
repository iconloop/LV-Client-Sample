package iconloop.lab.crypto.mpc.ecdsa;

public class MPCKeyUpdateTest {

    String _pubKey;

    // 키 생성 및 업데이트 테스트
    public static void main(String[] ar) throws Exception {
        String CURVE_NAME = "secp256k1";

        String keyId = "KEYID_TEST_01";
        String repoId = "REPOID_TEST_01";
        int n = 4;
        int t = 2;

        MPCConfig config = new MPCConfig(keyId, CURVE_NAME, n, t);

        MPCKeyUpdateTest test = new MPCKeyUpdateTest();
        String[] createKey = test.create(config, repoId);
        String[] updateKey = test.update(config, repoId, createKey);

        for(int i=0; i<n; i++) {
            System.out.println("C : " + createKey[i]);
            System.out.println("U : " + updateKey[i]);
        }
    }

    public String[] create(MPCConfig config, String repoId) throws MPCEcdsaException {

        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.KEY_SHARING_MODE, config);

        int n = config.getNumberOfPlayers();
        MPCKeySharingPlayer[] players = new MPCKeySharingPlayer[n];
        for (int i = 0; i < n; i++) {
            players[i] = new MPCKeySharingPlayer(config);
            client.addKeySharingPlayer(players[i]);
        }
        _pubKey = client.keySharing();

        System.out.println("* generate check");
        boolean check = client.checkKeyPair(_pubKey);
        System.out.println(check);

        return client.getPlayerKey();
    }

    public String[] update(MPCConfig config, String repoId, String[] playerKeys) throws MPCEcdsaException {

        MPCClient update = new MPCClient(repoId);
        update.init(MPCClient.KEY_UPDATE_MODE, config);

        int n = config.getNumberOfPlayers();
        for(int i=0; i<n; i++) {
            MPCKeySharingPlayer player = new MPCKeySharingPlayer(new PlayerKey(playerKeys[i]));
            update.addKeyUpdatePlayer(player);
        }
        String pubKey = update.keyUpdate();

        System.out.println("* update check");
        boolean check = update.checkKeyPair(_pubKey);
        System.out.println(check);

        return update.getPlayerKey();
    }
}
