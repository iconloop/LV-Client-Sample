package iconloop.lab.crypto.common;

import junit.framework.TestCase;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;

import java.math.BigInteger;

public class UtilsTest extends TestCase {

    public void testHash() {
        byte[] sha256Null = Hex.decode("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        byte[] sha256abc = Hex.decode("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        byte[] sha256448 = Hex.decode("248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1");
        byte[] sha256896 = Hex.decode("cf5b16a778af8380036ce59e7b0492370b249b11e8f07a51afac45037afee9d1");
        byte[] sha256aMillion = Hex.decode("cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0");

        byte[] sha3Null = Hex.decode("a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a");
        byte[] sha3abc = Hex.decode("3a985da74fe225b2045c172d6bd390bd855f086e3e9d525b46bfe24511431532");
        byte[] sha3448 = Hex.decode("41c0dba2a9d6240849100376a8235e2c82e1b9998a999e21db32dd97496d3376");
        byte[] sha3896 = Hex.decode("916f6061fe879741ca6469b43971dfdb28b1a32dc36cb3254e812be27aad1d18");
        byte[] sha3aMillion = Hex.decode("5c8875ae474a3634ba4fd55ec85bffd661f32aca75c6d699d0cdcb6c115891c1");

        byte[] message = new byte[0];
        byte[] digest = Utils.sha256Digest(message);
        Assert.assertArrayEquals(digest, sha256Null);
        digest = Utils.sha3Digest(message);
        Assert.assertArrayEquals(digest, sha3Null);

        message = "abc".getBytes();
        digest = Utils.sha256Digest(message);
        Assert.assertArrayEquals(digest, sha256abc);
        digest = Utils.sha3Digest(message);
        Assert.assertArrayEquals(digest, sha3abc);

        message = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes();
        digest = Utils.sha256Digest(message);
        Assert.assertArrayEquals(digest, sha256448);
        digest = Utils.sha3Digest(message);
        Assert.assertArrayEquals(digest, sha3448);

        message = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu".getBytes();
        digest = Utils.sha256Digest(message);
        Assert.assertArrayEquals(digest, sha256896);
        digest = Utils.sha3Digest(message);
        Assert.assertArrayEquals(digest, sha3896);

        byte[][] input = new byte[1000000][];
        for(int i=0; i<1000000; i++)
            input[i] = "a".getBytes();
        digest =  Utils.sha256Digest(input);
        Assert.assertArrayEquals(digest, sha256aMillion);
        digest = Utils.sha3Digest(input);
        Assert.assertArrayEquals(digest, sha3aMillion);
    }

    public void testConcat() {
        byte[][] inputs = new byte[10000][];
        for(int i=0; i<100; i++) {
            String tmp = "";
            for(int j=0; j<100; j++) {
                tmp = tmp.concat("a");
                inputs[i*100 +j] = tmp.getBytes();
            }
        }

        byte[] digest1 = Utils.sha256Digest(inputs);
        byte[] input = Utils.concat(inputs);
        byte[] digest2 = Utils.sha256Digest(input);
        Assert.assertArrayEquals(digest1, digest2);
    }

    public void testBigIntToUnsigned() {
        String a = "000102030405060708090a0b0c0d0e0f";
        String b = "f0e0d0c0b0a090807060504030201000";
        String c = "00f0e0d0c0b0a090807060504030201000";

        BigInteger a1 = new BigInteger(a, 16);
        byte[] a1U = Utils.bigIntToUnsigned(a1, a.length()/2);
        Assert.assertEquals(new BigInteger(a1U), a1);

        BigInteger b1 = new BigInteger(b, 16);
        BigInteger c1 = new BigInteger(Hex.decode(c));

        byte[] b1U = Utils.bigIntToUnsigned(b1, 32);
        byte[] c1U = Utils.bigIntToUnsigned(c1, 16);

        Assert.assertEquals(new BigInteger(b1U), new BigInteger(1, c1U));
    }
}
