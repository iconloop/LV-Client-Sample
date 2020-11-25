package iconloop.lab.crypto.rangeproof;

import iconloop.lab.crypto.bulletproof.BulletProofException;
import iconloop.lab.crypto.bulletproof.BulletProofTuple;
import iconloop.lab.crypto.ec.bouncycastle.curve.EC;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class RandomTest {

    public static final Random rng = new SecureRandom();

    public static void main(String[] args) throws BulletProofException {
        String curve = "secp256k1";
        String strPubString = "ICON_RANGE_PROOF";
        int secretBitLength = 64;
        int iteration = 10;

        randomTest(curve, secretBitLength, iteration, strPubString);
    }

    /* A random test succeeds means that expected result equals to test result.
     *  - expected result: derived from random range and random secret.
     *  - test result: result of verify function.
     * */
    public static void randomTest(String curveName, int secretBitLen, int iteration, String strPubString) throws BulletProofException {
        int numOfSuccess = 0;
        int numOfFail = 0;
        long tPTime = 0;
        long tVTime = 0;
        long minPTime = 10000;
        long minVTime = 10000;

        RangeProof verifier = new RangeProof(curveName, secretBitLen, strPubString);
        RangeProof prover = new RangeProof(curveName, secretBitLen, strPubString);

        BigInteger cmRand = BigInteger.valueOf(1000);

        for (int i = 0; i < iteration; i++) {
            BigInteger secret = new BigInteger(secretBitLen, rng).abs();
            BigInteger range_a = new BigInteger(secretBitLen, rng).abs();
            BigInteger range_b = new BigInteger(secretBitLen, rng).abs();

            if (range_a.compareTo(range_b) == 1) {
                BigInteger tmp = range_b;
                range_b = range_a;
                range_a = tmp;
            }
            int expectedResult = 1;//secret.compareTo(range_a) * (range_b.compareTo(secret));
            if( (secret.compareTo(range_a) * (range_b.compareTo(secret)) > 0 ) )
                expectedResult = 0;


            long pStart = System.currentTimeMillis();
            EC.Point cm = prover.commitment(secret, cmRand);

            BulletProofTuple proof = prover.generateProof(secret, cmRand, range_a, range_b);

            long pEnd = System.currentTimeMillis();

            long vStart = System.currentTimeMillis();
            int testResult = verifier.verify(cm, proof, range_a, range_b);
            long vEnd = System.currentTimeMillis();

            if (testResult == 0)
                numOfSuccess++;
            else
                numOfFail++;

            System.out.printf("\n*******************************\n");
            System.out.printf("iter: %d \n", i);
            System.out.printf("range_a: %16x\n", range_a);
            System.out.printf("secret : %16x\n", secret);
            System.out.printf("range_b: %16x\n", range_b);
            System.out.printf(" - result: %b\n", expectedResult == 0);
            if((pEnd-pStart)<minPTime)
                minPTime = pEnd-pStart;
            if((vEnd-vStart)<minVTime)
                minVTime = vEnd-vStart;
            tPTime += howLong(pStart, pEnd);
            tVTime += howLong(vStart, vEnd);
            if ( (expectedResult - testResult) > 0) {
                throw new RuntimeException("[Debug] Some Trial fail");
            }

        }

        System.out.println("\n\n"+iteration+" tests succeed");
        System.out.println(" - Expected true: "+ numOfSuccess);
        System.out.println(" - Expected false: "+ numOfFail);
        System.out.printf(" - Average Time: P(%.1f), V(%.1f)\n", tPTime/(float)iteration, tVTime/(float)iteration);
        System.out.printf(" -     Min Time: P(%d), V(%d)", minPTime, minVTime);

    }

    public static long howLong(long start, long end) {
        System.out.printf(" - time: %4d ms\n", end-start);
        return end - start;
    }
}
