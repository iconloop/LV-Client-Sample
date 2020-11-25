package iconloop.lab.common;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class GcmTest {

    public static void main(String[] ar) throws Exception {
        byte[] plain = "abcdefghijklmnopqrstuvwxyz!@#$%^&*()".getBytes();
        byte[] keyBytes = Hex.decode("7118c892d4a19fcbf0471c2db1055f5a");
        byte[] iv = Hex.decode("b2334d3ec83fc7fdb8580186");
        byte[] aad = Hex.decode("65794a68624763694f694a46513052494c555654496977695a584272496a7037496d743065534936496b56444969776959334a32496a6f695543307a4f4451694c434a34496a6f6964554a764e4774495548633261324a71654456734d48687664334a6b5832395a656b4a7459586f7452307447576e553065454647526d746957576c585a335630525573326158564652484e524e6e644f5a45356e4d794973496e6b694f694a7a63444e774e564e4861467057517a4a6d5956683162556b745a546c4b56544a4e627a684c6347395a636b5a45636a563555453557644663305547644664317050655646555153314b5a47465a4f4852694e305577496e3073496d567559794936496b45784d6a6848513030694c434a72615751694f694a775a584a6c5a334a706269353062323972514852315932746962334a766457646f4c6d563459573177624755696651");

        plain = Hex.decode("20bdc7b6b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d20bdc7b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d20bdc7b6b1d5b22fbddc6d");
        keyBytes = Hex.decode("a54c87e4bfca6cd26dab454fc847c280");
        iv = Hex.decode("58bb75f483a17e13d7abc141");
        aad = Hex.decode("3e83e63d4ef2bc9f2777331547103b3450b969d06abbb2ef2d2a9848da72b142d3a33d4d744cea9314ade2e5a56d6fd16a31a6f128dca33ad08b4f7135c82a533fe94ebfd3c69a327926b54785d06c4be333faafb69dceb50b7f1e122f31ad0e299ac6fa9007a1c86560c0b6c704b5a77b7f74b71e7435e64a72fe738194212f229a4da2b4b96971fe61662667e6a48a88b6a22eb84112f62f29a16b35ddaa0e34879b780df179d001f571c8c52cbcdd960d5c7d85827ce48d6ef1141dc68a30a88748583c8c61769160ea8e420bef803cdb283a889d1f19eb9b856a");
        System.out.println(Hex.toHexString(plain));

        byte[] enc = encrypt(plain, keyBytes, iv, aad);
        byte[] dec = decrypt(enc, keyBytes, iv, aad);

        System.out.println(Arrays.areEqual(plain, dec));

////        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
//        KeyParameter keyParam = new KeyParameter(keyBytes);
//        ParametersWithIV param = new ParametersWithIV(keyParam, iv);
//        GCMBlockCipher encCipher = new GCMBlockCipher(new AESEngine());
//        encCipher.init(true, param);
//        encCipher.processAADBytes(aad, 0, aad.length);
//
//        int outLeng = encCipher.getOutputSize(plain.length);
//        byte[] enc = new byte[outLeng];
//
//        int len = encCipher.processBytes(plain, 0, plain.length, enc, 0);
//        len += encCipher.doFinal(enc, len);
//        System.out.println(enc.length + " : " + len);
//
////        byte[] mac = cipher.getMac();
//
//        System.out.println(Hex.toHexString(enc));
//
////        byte[] mac = encCipher.getMac();
////System.out.println(Hex.toHexString(mac));
//        byte[] data = new byte[plain.length];
//        System.arraycopy(enc, 0, data, 0, data.length);
//        byte[] tail = new byte[enc.length - plain.length];
//        System.arraycopy(enc, plain.length, tail, 0, tail.length);
//
//        System.out.println(Hex.toHexString(data));
//        System.out.println(Hex.toHexString(tail));


//        System.out.println(Hex.toHexString(mac));

//        byte[] fina = new byte[outLeng];
//        int len = cipher.doFinal(fina, 0);
//        System.out.println(Hex.toHexString(fina));
//        System.out.println(len);
    }

    public static byte[] encrypt(byte[] plain, byte[] key, byte[] iv, byte[] aad) throws InvalidCipherTextException {
        KeyParameter keyParam = new KeyParameter(key);
        ParametersWithIV param = new ParametersWithIV(keyParam, iv);

        GCMBlockCipher encCipher = new GCMBlockCipher(new AESEngine());
        encCipher.init(true, param);

        encCipher.processAADBytes(aad, 0, aad.length);

        int outLeng = encCipher.getOutputSize(plain.length);
        byte[] enc = new byte[outLeng];

        int len = encCipher.processBytes(plain, 0, plain.length, enc, 0);
System.out.println("Process : " + Hex.toHexString(enc));
        len += encCipher.doFinal(enc, len);

System.out.println("Enc     : " + Hex.toHexString(enc));

//        byte[] mac = encCipher.getMac();
//System.out.println(Hex.toHexString(mac));
        byte[] data = new byte[plain.length];
        System.arraycopy(enc, 0, data, 0, data.length);
        byte[] tail = new byte[enc.length - plain.length];
        System.arraycopy(enc, plain.length, tail, 0, tail.length);

        System.out.println(Hex.toHexString(data));
        System.out.println(Hex.toHexString(tail));

        return enc;
    }

    public static byte[] decrypt(byte[] enc, byte[] key, byte[] iv, byte[] aad) throws InvalidCipherTextException {
        KeyParameter keyParam = new KeyParameter(key);
        ParametersWithIV param = new ParametersWithIV(keyParam, iv);

        GCMBlockCipher decCipher = new GCMBlockCipher(new AESEngine());
        decCipher.init(false, param);

        decCipher.processAADBytes(aad, 0, aad.length);

        int outLeng = decCipher.getOutputSize(enc.length);
        byte[] dec = new byte[outLeng];

        int len = decCipher.processBytes(enc, 0, enc.length, dec, 0);
System.out.println("Process : " + Hex.toHexString(dec));
        len += decCipher.doFinal(dec, len);

System.out.println("Dec     : " + Hex.toHexString(dec));

//        byte[] mac = encCipher.getMac();
//System.out.println(Hex.toHexString(mac));
//        byte[] data = new byte[plain.length];
//        System.arraycopy(enc, 0, data, 0, data.length);
//        byte[] tail = new byte[enc.length - plain.length];
//        System.arraycopy(enc, plain.length, tail, 0, tail.length);
//
//        System.out.println(Hex.toHexString(data));
//        System.out.println(Hex.toHexString(tail));

        return dec;
    }

}
