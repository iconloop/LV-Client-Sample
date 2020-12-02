package iconloop.lab.crypto.vault;

import org.bouncycastle.util.encoders.Hex;

import java.security.SecureRandom;

public class InterpolateTest {

    public static final int N = 4;
    public static final int T = 1;

    public static void main(String[] ar) throws Exception {
        byte in = (byte)new SecureRandom().nextInt();
        System.out.println("Secret : " + Integer.toHexString(in));

        byte[][] shares = split(in);
        System.out.println("Shares : ");
        for(int i=0; i<N; i++) {
            System.out.println(" - " + Hex.toHexString(shares[i]));
        }

        byte out = reconstruct(shares);
        System.out.println("Reconstructed : " + Integer.toHexString(out));

        for(int i=0; i<N; i++) {
            byte fi = shares[i][0];
            byte tmp = reconstruct(fi, shares);

            if(shares[i][1] == tmp)
                System.out.println(" - OK   : " + Integer.toHexString(fi) + " : " + Integer.toHexString(tmp));
            else
                System.out.println(" - Fail : " + Integer.toHexString(fi) + " : " + Integer.toHexString(tmp));
        }

    }

    public static byte[][] split(byte secret) {
        SecureRandom random = new SecureRandom();

        // generate part values
        byte[][] shares = new byte[N][2];// "00" + "00" = "x"+"f(x)"
        int degree = T;

        //shares의 각 i-th byte를 구하는 for문
        byte[] p = new byte[degree + 1];
        random.nextBytes(p);
        p[0] = secret;
        //gen n points
        for(int j = 0; j < N; j++) {
            shares[j][0] = (byte)(j+1);
            shares[j][1] = GF256.eval(p, (byte)(j+1));
        }

        return shares;
    }

    public static byte reconstruct(byte[][] shares) {

        //to be returned
        final int[] idx = new int[T+1];

        byte[] points = new byte[T+1];
        for(int i = 0; i< T+1; i++) {
            idx[i] = shares[i][0];
            points[i] = shares[i][1];
        }
        return interpolate(idx, points);
    }

    public static byte reconstruct(byte fi, byte[][] shares) {
        final int[] idx = new int[T+1];

        byte[] points = new byte[T+1];
        for(int i = 0; i< T+1; i++) {
            idx[i] = shares[i][0];
            points[i] = shares[i][1];
        }
        return interpolate(fi, idx, points);
    }

    private static byte interpolate(int[] idx, byte[] points) {
        byte f0 = 0; // to be returned

        for (int i = 0; i < points.length; i++) {
            byte li = 1;
            for (int j = 0; j < points.length; j++) {
                if (i != j) {
                    li = GF256.mul(li, GF256.div(GF256.sub((byte)0, (byte)(idx[j])), GF256.sub((byte)(idx[i]), (byte)(idx[j]))));
                }
            }
            f0 = GF256.add(f0, GF256.mul(li, points[i]));
        }
        return f0;
    }

    private static byte interpolate(byte fi, int[] idx, byte[] points) {
        byte f0 = 0; // to be returned

        for (int i = 0; i < points.length; i++) {
            byte li = 1;
            for (int j = 0; j < points.length; j++) {
                if (i != j) {
                    li = GF256.mul(li, GF256.div(GF256.sub(fi, (byte)(idx[j])), GF256.sub((byte)(idx[i]), (byte)(idx[j]))));
                }
            }
            f0 = GF256.add(f0, GF256.mul(li, points[i]));
        }
        return f0;
    }

}
