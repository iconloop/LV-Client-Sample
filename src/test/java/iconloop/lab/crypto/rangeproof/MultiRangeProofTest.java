package iconloop.lab.crypto.rangeproof;

import iconloop.lab.crypto.bulletproof.BulletProofException;
import iconloop.lab.crypto.bulletproof.BulletProofTuple;
import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.EC;

import java.math.BigInteger;

public class MultiRangeProofTest {


    public static final String Curve = "secp256k1";
    public static final int SecretBitLength = 32;
    public static final String PubString = "ICON_RANGE_PROOF";
    public static final int maxM = 4;

    public static void main(String[] ar) throws Exception {
        MultiRangeProof common = new MultiRangeProof(Curve, SecretBitLength, maxM, PubString);

        // CM01

        BigInteger secret01 = BigInteger.valueOf(20000101);
        BigInteger cmRand01 = Utils.getRandomInteger(256);
        EC.Point commit01 = common.commitment(secret01, cmRand01);

        // request Verifier
        BigInteger range01A = BigInteger.valueOf(19000101);
        BigInteger range01B = BigInteger.valueOf(20201231);

        System.out.println("*** Range1) " + range01A + " <= " + secret01 + " < " + range01B);
        System.out.println("    cm01       : " + commit01);
        System.out.println("    cmRandom01 : " + cmRand01.toString(16));

        // CM02
        BigInteger secret02 = BigInteger.valueOf(1000000);
        BigInteger cmRand02 = Utils.getRandomInteger(256);
        EC.Point commit02 = common.commitment(secret02, cmRand02);

        // request Verifier
        BigInteger range02A = BigInteger.valueOf(500000);
        BigInteger range02B = BigInteger.valueOf(1500000);

        System.out.println("*** Range2) " + range02A + " <= " + secret02 + " < " + range02B);
        System.out.println("    cm02       : " + commit02);
        System.out.println("    cmRandom02 : " + cmRand02.toString(16));

        SampleMessage message01 = new SampleMessage(1, commit01, secret01, cmRand01, range01A, range01B);
        SampleMessage message02 = new SampleMessage(2, commit02, secret02, cmRand02, range02A, range02B);

        SampleMessage[] samples = new SampleMessage[]{message01, message02};

        // generate Proof
        BulletProofTuple proof = prove(samples);
        System.out.println("");
        print(proof);
        System.out.println("----------------");

        // verify Proof
        int result = verify(proof, samples);

        System.out.println("\n### Case 1");
        System.out.println("    Result(OK)   : " + (result == 0 ? "success" : result));

        System.out.println("\n### Case 2(Proof Error, changed Secret)");
        SampleMessage message03 = new SampleMessage(2, commit02, BigInteger.valueOf(1500000), cmRand02, range02A, range02B);
        SampleMessage[] proofErrSamples = new SampleMessage[]{message01, message03};
        proof = prove(proofErrSamples);
        result = verify(proof, proofErrSamples);
        System.out.println("    Result(Fail) : " + (result == 0 ? "success" : result));

        System.out.println("\n### Case 3(Verify Error, changed Range)");
        proof = prove(samples);
        SampleMessage message04 = new SampleMessage(2, commit02, secret02, cmRand02, range01A, range01B);
        SampleMessage[] verifyErrSamples = new SampleMessage[]{message01, message04};
        result = verify(proof, verifyErrSamples);
        System.out.println("    Result(Fail) : " + (result == 0 ? "success" : result));

        System.out.println("\n### Case 4(Proof Only Secret01)");
        SampleMessage message05 = new SampleMessage(2, commit02, secret02, cmRand02, BigInteger.valueOf(0), BigInteger.valueOf(9999999));
        SampleMessage[] onlySecret1 = new SampleMessage[]{message01, message05};
        proof = prove(onlySecret1);
        result = verify(proof, onlySecret1);
        System.out.println("    Result(OK) : " + (result == 0 ? "success" : result));

        System.out.println("\n### Case 4(Proof Only Secret02)");
        SampleMessage message06 = new SampleMessage(1, commit01, secret01, cmRand01, BigInteger.valueOf(0), BigInteger.valueOf(99999999));
        SampleMessage[] onlySecret2 = new SampleMessage[]{message06, message02};
        proof = prove(onlySecret2);
        result = verify(proof, onlySecret2);
        System.out.println("    Result(OK) : " + (result == 0 ? "success" : result));
    }

    public static BulletProofTuple prove(SampleMessage[] samples) throws BulletProofException {
        MultiRangeProof prover = new MultiRangeProof(Curve, SecretBitLength, maxM, PubString);
        for(int i=0; i< samples.length; i++) {
            prover.addSecret(samples[i].secret, samples[i].cmRand, samples[i].rangeA, samples[i].rangeB);
        }

        return prover.generateProof();
    }

    public static int verify(BulletProofTuple proof, SampleMessage[] sample) throws BulletProofException {
        MultiRangeProof verifier = new MultiRangeProof(Curve, SecretBitLength, maxM, PubString);
        for(int i=0; i< sample.length; i++) {
            verifier.addProofs(sample[i].index, sample[i].cm, sample[i].rangeA, sample[i].rangeB);
        }
        return verifier.verify(proof);
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

    public static class SampleMessage {

        int index;
        EC.Point cm;
        BigInteger secret;
        BigInteger cmRand;
        BigInteger rangeA;
        BigInteger rangeB;
        BulletProofTuple proof;

        SampleMessage(int index, EC.Point cm, BigInteger secret, BigInteger cmRand, BigInteger rangeA, BigInteger rangeB) {
            this.index = index;
            this.cm = cm;
            this.secret = secret;
            this.cmRand = cmRand;
            this.rangeA = rangeA;
            this.rangeB = rangeB;
        }

        void setProof(BulletProofTuple proof) {
            this.proof = proof;
        }
    }
}
