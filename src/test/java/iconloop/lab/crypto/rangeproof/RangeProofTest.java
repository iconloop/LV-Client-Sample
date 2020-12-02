package iconloop.lab.crypto.rangeproof;

import iconloop.lab.crypto.bulletproof.BulletProofException;
import iconloop.lab.crypto.bulletproof.BulletProofTuple;
import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.EC;

import java.math.BigInteger;

public class RangeProofTest {

    public static final String Curve = "secp256k1";
    public static final int SecretBitLength = 32;
    public static final String PubString = "ICON_RANGE_PROOF";

    public static void main(String[] ar) throws Exception {
        System.out.println("Range Proof => secret in [rangeA, rangeB)");
        RangeProof common = new RangeProof(Curve, SecretBitLength, PubString);
        BigInteger cmRand = Utils.getRandomInteger(256);

        BigInteger secret = BigInteger.valueOf(20040503);
        EC.Point commit = common.commitment(secret, cmRand);
        System.out.println("cm       : " + commit);
        System.out.println("cmRandom : " + cmRand.toString(16));

        // request Verifier
        BigInteger rangeA = BigInteger.valueOf(20000101);
        BigInteger rangeB = BigInteger.valueOf(20201026);
        System.out.println("rangeA   : " + rangeA);
        System.out.println("secret   : " + secret);
        System.out.println("rangeB   : " + rangeB);

        // generate Proof
        BulletProofTuple proof = prove(secret, cmRand, rangeA, rangeB);
        print(proof);

        // verify Proof
        int result = verify(commit, proof, rangeA, rangeB);
        System.out.println("\n### Result : " + (result == 0 ? "success" : Integer.toHexString(result)));
    }

    private static BulletProofTuple prove(BigInteger secret, BigInteger cmRand, BigInteger rangeA, BigInteger rangeB) throws BulletProofException {
        RangeProof prover = new RangeProof(Curve, SecretBitLength, PubString);
        return prover.generateProof(secret, cmRand, rangeA, rangeB);
    }

    private static int verify(EC.Point commit, BulletProofTuple proof, BigInteger rangeA, BigInteger rangeB) throws BulletProofException {
        RangeProof verifier = new RangeProof(Curve, SecretBitLength, PubString);
        return verifier.verify(commit, proof, rangeA, rangeB);
    }

    public static void print(BulletProofTuple proof) {
        System.out.println("### ProofTuple ###");
        System.out.println(" - V =>");
        EC.Point[] V = proof.getPointArray(BulletProofTuple.Points_V);
        for(int i=0; i<V.length; i++){
            System.out.println("   V[" + i + "] : " + V[i]);
        }
        System.out.println(" - A    : " + proof.getPoint(BulletProofTuple.Point_A));
        System.out.println(" - S    : " + proof.getPoint(BulletProofTuple.Point_S));
        System.out.println(" - T1   : " + proof.getPoint(BulletProofTuple.Point_T1));
        System.out.println(" - T2   : " + proof.getPoint(BulletProofTuple.Point_T2));
        System.out.println(" - taux : " + proof.getScalar(BulletProofTuple.Scalar_Taux));
        System.out.println(" - mu   : " + proof.getScalar(BulletProofTuple.Scalar_Mu));
        System.out.println(" - L => ");
        EC.Point[] L = proof.getPointArray(BulletProofTuple.Points_L);
        for(int i=0; i<L.length; i++)
            System.out.println("   L[" + i + "] : " + L[i]);
        System.out.println(" - R => ");
        EC.Point[] R = proof.getPointArray(BulletProofTuple.Points_R);
        for(int i=0; i<R.length; i++)
            System.out.println("   R[" + i + "] : " + R[i]);
        System.out.println(" - a    : " + proof.getScalar(BulletProofTuple.Scalar_A));
        System.out.println(" - b    : " + proof.getScalar(BulletProofTuple.Scalar_B));
        System.out.println(" - t    : " + proof.getScalar(BulletProofTuple.Scalar_T));
    }
}
