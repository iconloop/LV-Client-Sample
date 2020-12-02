package iconloop.lab.crypto.vault;

import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Vector;

public class SecretSharingSequenceTest {

    static Vector<Vector<Boolean>> _combination;

    public static void main(String[] ar) throws Exception {
        SecretSharingSequenceTest test = new SecretSharingSequenceTest();

        byte[] data = "test".getBytes();

        int minN = 2;
        int maxN = 5;

        int count = 0;
        int errCnt = 0;

        int n, t;
        for (int i = minN; i < maxN + 1; i++) {
            n = i;
            for (int j = 1; j < i; j++) {
                t = j;
                System.out.println("\n### Start => n : " + n + ", t : " + t);

                int[] total = test.share(data, n, t);

                test.setCombination(n, t);

                for (Vector<Boolean> index : _combination) {
                    int[] player = test.selectPlayer(index, total);

                    System.out.println("\n --- reconstuct ---");
                    printArray(" Players ", player);
                    byte[] recon = test.reconstruct(player, n, t);
                    boolean result = Arrays.areEqual(data, recon);
                    System.out.println("  -result : " + result);
                    if(!result)
                        errCnt++;
                    count++;
                }
            }
            System.out.println();
            System.out.println("=> Total : " + count + ", Failed : " + errCnt);
        }
    }

    public int[] share(byte[] data, int numOfStorage, int numOfThreshold) throws Exception {
        SecretSharing ss = new SecretSharing(new SecureRandom(), numOfStorage, numOfThreshold);
        byte[][] shares = ss.split(data);
        printShare("  - share   ", shares);

        int[] players = new int[numOfStorage];
        for(int i=0; i<numOfStorage; i++) {
            SecretSharingTest.Storage.add(i, shares[i]);
            players[i] = i;
        }
        return players;
    }

    public byte[] reconstruct(int[] players, int numOfStorage, int numOfThreshold) throws Exception {
        byte[][] shares = new byte[players.length][];

        for (int i = 0; i < players.length; i++) {
            int index = players[i];
            shares[i] = SecretSharingTest.Storage.get(index);
        }

        printShare("  -receive  ", shares);

        // 4. User : share로 부터 data를 복구한다.
        SecretSharing ss = new SecretSharing(new SecureRandom(), numOfStorage, numOfThreshold);
        return ss.reconstruct(shares);
    }

    private int[] selectPlayer(Vector<Boolean> visited, int[] players) {
        Vector<Integer> tmp = new Vector<Integer>();
        for(int i=0; i<visited.size(); i++) {
            if(visited.get(i))
                tmp.add(players[i]);
        }

        int[] selected = new int[tmp.size()];
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

    private static void printArray(String prefix, int[] data) {
        System.out.print(prefix + " : ");
        for(int i=0; i<data.length; i++) {
            System.out.print(data[i]);
            if(i < data.length-1)
                System.out.print(", ");
        }
        System.out.println();
    }

    public static void printShare(String prefix, byte[][] share) {
        for (int i = 0; i < share.length; i++) {
            int index = share[i][0];
            byte[] tmp = new byte[share[i].length - 1];
            System.arraycopy(share[i], 1, tmp, 0, tmp.length);
            System.out.println(prefix + "[" + (index-1) + "] : " + Hex.toHexString(tmp));
        }
    }

    public static class Storage {

        private static HashMap<Integer, byte[]> _storages = new HashMap<Integer, byte[]>();

        public static void add(int id, byte[] shares) {
            _storages.put(id, shares);
        }

        public static byte[] get(int id) {
            return _storages.get(id);
        }
    }

}
