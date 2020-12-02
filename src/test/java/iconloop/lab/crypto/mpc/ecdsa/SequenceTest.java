package iconloop.lab.crypto.mpc.ecdsa;

import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;
import iconloop.lab.crypto.he.paillier.PaillierException;
import iconloop.lab.crypto.he.paillier.PaillierUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.Vector;

public class SequenceTest {

    static MPCConfig _config;
    static Vector<Vector<Boolean>> _combination;
    static Vector<String> _result = new Vector<String>();
    static Vector<Long> _timer = new Vector<Long>();
    static int sCount=0;
    static int fCount=0;

    // N의 수에 따라 가능한 T와 Player 조합을 순서대로 테스트
    // N, T 선택 -> 키 생성 -> Player 선택 -> 서명 -> 키 업데이트 -> Player 선택 -> 서명
    public static void main(String[] ar) throws Exception {
        String curveName = "secp256k1";
        String preKeyId  = "KEY_";
        String preRepoId = "REPO_";
        int n;
        int t;

        SequenceTest test = new SequenceTest();

        byte[] message = "test".getBytes();
        byte[] hashedMessage = Utils.sha256Digest(message);

        int minN = 2;
        int maxN = 4;

        for(int i=minN; i<maxN+1; i++) {
            n = i;
            for(int j=1; j<i; j++) {
                t = j;
                System.out.println("### Start => n : " + n + ", t : " + t);
                String errMsg = " n : " + n + ", t : " + t;

                String keyId = preKeyId + n + "_" + t;
                String repoId = preRepoId + n + "_" + t;
                _config = new MPCConfig(keyId, curveName, n, t);
                boolean check1 = false;
                boolean check2 = false;
                long start = System.currentTimeMillis();
                long middleA, middleB = 0;

                String[] players = test.generateShare(repoId);
                System.out.println(" * Generated PublicKey : " + _config.getEncodedPublicKey());

                test.setCombination(n, t);
                for(Vector<Boolean> index : _combination) {
                    String[] signers = test.selectSigner(index, players);

                    middleA = System.currentTimeMillis();
                    String signature = test.sign(repoId, hashedMessage, signers);

                    check1 = test.verify(curveName, message, signature, _config.getEncodedPublicKey());
                    middleB = System.currentTimeMillis();
                    System.out.print(" => " + check1);
                    if(!check1)
                        errMsg = errMsg + ", " + test.getError(signers);
                    System.out.println(", " + (middleB - middleA) + " Milliseconds");
                }

                middleA = System.currentTimeMillis();
                String[] newPlayers = test.updateShare(repoId, players);
                middleB = System.currentTimeMillis();
                System.out.println(" * Updated PublicKey : " +  _config.getEncodedPublicKey() + ", " + (middleB - middleA) + " Milliseconds");

                test.setCombination(n, t);
                for(Vector<Boolean> index : _combination) {
                    String[] signers = test.selectSigner(index, newPlayers);

                    middleA = System.currentTimeMillis();
                    String signature = test.sign(repoId, hashedMessage, signers);

                    check2 = test.verify(curveName, message, signature, _config.getEncodedPublicKey());
                    middleB = System.currentTimeMillis();
                    System.out.print(" => " + check2);
                    if(check1 && !check2)
                        errMsg = errMsg + ", " + test.getError(signers);
                    System.out.println(", " + (middleB - middleA) + " Milliseconds");
                }

                long end = System.currentTimeMillis();
                long running = (end - start);
                int cases = _combination.size();
                System.out.println(" * " + running + " Milliseconds, " + cases + " Cases");
                _timer.add((running/cases));
                if(check1 && check2)
                    sCount++;
                else {
                    fCount++;
                    _result.add(errMsg);
                }
                System.out.println();
            }
        }

        System.out.println("---------------- Report --------------");
        int totalTest = sCount + fCount;
        System.out.println(" Signing total : " + totalTest);
        System.out.println(" Failed        : " + fCount);
        for(String msg : _result)
            System.out.println("  * " + msg);

        long total = 0L;
        long maxTimer = 0L;
        long minTimer = 0L;
        for(long time : _timer) {
            total = total + time;
            if(time > maxTimer)
                maxTimer = time;
            if(minTimer == 0L || time < minTimer)
                minTimer = time;
        }
        System.out.println(" Total Time    : " + total + " milliseconds");
        System.out.println(" Max   Time    : " + maxTimer + " milliseconds");
        System.out.println(" Min   Time    : " + minTimer + " milliseconds");
        System.out.println(" Ava   Time    : " + (total/totalTest) + " milliseconds");

    }

    public String getError(String[] signers) {
        StringBuffer sb = new StringBuffer();
        sb.append("index[");
        for(String signer : signers) {
            PlayerKey keys = new PlayerKey(signer);
            sb.append(keys.getMyIndex() + ", ");
        }
        String log = sb.toString();
        return log.substring(0, log.length()-2) + "]";
    }

    public String[] generateShare(String repoId) throws MPCEcdsaException {
        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.KEY_SHARING_MODE, _config);

        int n = _config.getNumberOfPlayers();
        MPCKeySharingPlayer[] players = new MPCKeySharingPlayer[n];
        for(int i=0; i<n; i++) {
            players[i] = new MPCKeySharingPlayer(_config);
            client.addKeySharingPlayer(players[i]);
        }
        String pubKey = client.keySharing();
        _config.setPublicKey(pubKey);

        return client.getPlayerKey();
    }

    public String[] updateShare(String repoId, String[] playerKeys) throws MPCEcdsaException {
        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.KEY_UPDATE_MODE, _config);

        int n = _config.getNumberOfPlayers();
        MPCKeySharingPlayer[] players = new MPCKeySharingPlayer[n];
        for(int i=0; i<n; i++) {
            players[i] = new MPCKeySharingPlayer(new PlayerKey(playerKeys[i]));
            client.addKeyUpdatePlayer(players[i]);
        }
        String pubKey = client.keyUpdate();
        if(!_config.getEncodedPublicKey().equals( pubKey))
            throw new MPCEcdsaException("Update Failed.(expected : " + _config.getEncodedPublicKey() + ", result : " + pubKey);

        return client.getPlayerKey();
    }

    public String sign(String repoId, byte[] hashedMessage, String[] playerKeys) throws MPCEcdsaException, PaillierException {
        MPCClient client = new MPCClient(repoId);
        client.init(MPCClient.SIGNING_MODE, _config);

        StringBuilder sb = new StringBuilder();
        sb.append("  - index[");
        for(String playerKey : playerKeys) {
            PlayerKey keys = new PlayerKey(playerKey);
            sb.append(keys.getMyIndex() + ", ");
            client.addSignPlayer(new MPCSigningPlayer(keys, PaillierUtils.generateKey(1024)));
        }
        String log = sb.toString();
        System.out.print(log.substring(0, log.length()-2) + "]");

        return client.signing(hashedMessage);
    }

    public boolean verify(String curveName, byte[] message, String signature, String publicKey) throws Exception {
        ECParameterSpec paramSpec = ECUtils.getECParameterSpec(_config.getCurveName());
        BCECPublicKey pub = ECUtils.getBCECPublicKey(paramSpec, Hex.decode(publicKey));
        BigInteger[] sig = ECUtils.decodeStdDSASignature(Hex.decode(signature));
        byte[] hashedMessage = Utils.sha256Digest(message);
        return ECUtils.verifyECDSA(curveName, hashedMessage, pub, sig[0], sig[1]);
    }

    public String[] selectSigner(Vector<Boolean> visited, String[] players) {
        Vector<String> tmp = new Vector<String>();
        for(int i=0; i<visited.size(); i++) {
            if(visited.get(i))
                tmp.add(players[i]);
        }

        String[] selected = new String[tmp.size()];
        for(int i=0; i<tmp.size(); i++)
            selected[i] = tmp.get(i);
        return selected;
    }

    private void setCombination(int n, int t) {
        _combination = new Vector<Vector<Boolean>>();
        int[] arr = new int[n];
        boolean[] visited = new boolean[n];
        for(int arrI=0; arrI<n; arrI++) {
            arr[arrI] = arrI;
        }
        combination(arr, visited, 0, n, t+1);
    }

    private void combination(int[] arr, boolean[] visited, int start, int n, int r) {
        if (r == 0) {
            Vector<Boolean> vv = new Vector<Boolean>();
            for(int i=0; i<n; i++) {
                vv.add(visited[i]);
            }
            _combination.add(vv);
        }

        for (int i = start; i < n; i++) {
            visited[i] = true;
            combination(arr, visited, i + 1, n, r - 1);
            visited[i] = false;
        }
    }
}
