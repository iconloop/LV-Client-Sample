package iconloop.lab.crypto;

import iconloop.lab.crypto.common.UtilsTest;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECDHUtilsTest;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtilsTest;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTest extends TestCase {

    public static void main (String[] args)  {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Crypto Research Tests");

        suite.addTestSuite(UtilsTest.class);
        suite.addTestSuite(ECUtilsTest.class);
        suite.addTestSuite(ECDHUtilsTest.class);

        return new CryptoTestSetup(suite);
    }

    static class CryptoTestSetup extends TestSetup {
        public CryptoTestSetup(Test test) {
            super(test);
        }

        protected void setUp() {
        }

        protected void tearDown() {
        }
    }
}
