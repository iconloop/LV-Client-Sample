package iconloop.lab.crypto.vault;

import com.google.gson.JsonObject;
import iconloop.lab.crypto.common.Utils;
import iconloop.lab.crypto.ec.bouncycastle.curve.ECUtils;
import iconloop.lab.crypto.jose.ECKey;
import iconloop.lab.crypto.jose.JoseHeader;
import iconloop.lab.crypto.vault.litevault.Client;
import iconloop.lab.crypto.vault.litevault.Manager;
import iconloop.lab.crypto.vault.litevault.Storage;
import iconloop.lab.crypto.vault.litevault.messages.*;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.util.HashMap;

public class LiteVaultProtocolTest {

    // Common Param
    public static final String CurveName = "P-256";
    public static final String JwsAlgorithm = JoseHeader.JWS_ALG_ES256;
    public static final String JweAlgorithm = JoseHeader.JWE_ALG_ECDH_ES_A128KW;
    public static final String EncAlgorithm = JoseHeader.JWE_ENC_A128GCM;

    // Common Info(Manager)
    public static JsonObject ManagerSignKey;
    public static String ManagerSignKeyId;
    public static JsonObject ManagerKmKey;
    public static String ManagerKmKeyId;

    // Client Option
    public static final int StorageNum = 3;
    public static final int ThresholdNum = 2;

    public static void main(String[] ar) throws Exception {

        // 0-1. Manager Setting
        System.out.println("0-1. Manager Setting");
        Manager manager = createManager();
        ManagerSignKey = manager.getSignKey();
        ManagerSignKeyId = manager.getSignKeyId();
        ManagerKmKey = manager.getKmKey();
        ManagerKmKeyId = manager.getKmKeyId();

        // 0-2. Storage Setting
        Storage[] storages = new Storage[StorageNum];
        System.out.println("\n0-2. Storage Setting");
        for(int i=0; i<StorageNum; i++) {
            storages[i] = createStorage();
        }

        // 0-3. Storage Registration(to Manager)
        System.out.println("\n0-3. Storage Registration(to Manager)");
        for(int i=0; i<StorageNum; i++) {
            manager.addStorage(storages[i].getStorageId(), storages[i].getStorageKmKey());
        }

        // 0-4. Client Setting(and Manager Key Registration)
        System.out.println("\n0-4. Client Setting(and Manager Key Registration)");
        Client client = createClient();



        // 1-1. Auth Request(Client to Manager)
        System.out.println("\n1-1. AuthRequest(Client)");
        System.out.println(" - Encrypt AuthRequest");
        String jweAuthReq = client.makeAuthRequest(makeIAT());
        System.out.println(" - AuthRequest     : " + jweAuthReq);
        System.out.println("        header     : " + printHeader(jweAuthReq));

        // 1-2. Auth Response(Manager to Client)
        System.out.println("\n1-2. AuthResponse(Manager)");
        System.out.println(" - Decrypt AuthRequest");
        AuthRequest authReq = manager.checkAuthRequest(jweAuthReq);
        System.out.println(" - Client Authentication");
        String clientVaultId = manager.clientAuth("client@email.com", "01012345678");
        long iat = makeIAT();
        long exp = (iat) + (60*60*24*10);
        System.out.println(" - Sign VC");
        String vc = manager.makeCredential(JwsAlgorithm, clientVaultId, iat, exp, authReq);
        System.out.println("   * VC            : " + vc);
        System.out.println("           header  : " + printHeader(vc));
        System.out.println("           payload : " + printPayload(vc));
        String jweAuthResp = manager.makeAuthResponse(authReq, iat, clientVaultId, vc);
        System.out.println(" - AuthResponse    : " + jweAuthResp);
        System.out.println("            header : " + printHeader(jweAuthResp));

        // 1-3. Verify Auth Response(Client)
        System.out.println("\n1-3. Verify AuthResponse(Client)");
        client.checkAuthResponse(jweAuthResp);


        // 2-1. Token Request(Client to Storages)
        System.out.println("\n2-1. Token Request(Client)");
        HashMap<String, JsonObject> cStorages = client.getStorages();
        HashMap<String, String> cTokenReqs = new HashMap<String, String>();
        for(String storageId : cStorages.keySet()) {
            System.out.println(" - Encrypt TokenRequest(For " + storageId + ")");
            ECKey storageKey = ECKey.parse(cStorages.get(storageId));
            String jweTokenReq = client.makeTokenRequest(storageId, storageKey, makeIAT());
            System.out.println("   * TokenRequest  : " + jweTokenReq);
            System.out.println("            header : " + printHeader(jweTokenReq));

            cTokenReqs.put(storageId, jweTokenReq);
        }

        // 2-2. Verify Token Request and Create Token Response(Storages)
        System.out.println("\n2-2. Verify Token Request and Create Token Response(Storages)");
        for (Storage storage : storages) {
            String storageId = storage.getStorageId();
            System.out.println(" [Storage(" + storageId + ")]");

            System.out.println(" - Decrypt Token Request");
            String jweTokenReq = cTokenReqs.get(storageId);
            TokenRequest tokenRequest = storage.checkTokenRequest(jweTokenReq);
            iat = makeIAT();

            System.out.println(" - Encrypt Token Response");
            String jweTokenResp = storage.makeTokenResponse(tokenRequest, iat);
            System.out.println("   * TokenResponse : " + jweTokenResp);
            System.out.println("            header : " + printHeader(jweTokenResp));

            System.out.println(" [Client(For : " + storageId + ")]");
            System.out.println(" - Decrypt Token Response");
            client.checkTokenResponse(storageId, jweTokenResp);
            System.out.println();
        }

        // 3. Data Sharing(Client)
        System.out.println("\n3. Data Sharing(Client)");
        String plainText = "Lite Vault Protocol Test !!!(한글도 포함)";
        System.out.println(" - Original Data   : " + plainText);
        String[] clues = client.share(StorageNum, ThresholdNum, plainText.getBytes(Charset.forName("UTF-8")));
        System.out.println(" - Encrypted Clues");
        for(String clue : clues)
            System.out.println("   *               : " + clue);

        // 4. Write Request(Client to Storages)
        System.out.println("\n3-1. Write Process(Client <> Storage)");
        iat = makeIAT();
        int seq = client.getSequence();
        client.setSequence(seq + 1);
        for(int i=0; i<storages.length; i++) {
            Storage storage = storages[i];
            String storageId = storage.getStorageId();
            System.out.println(" [Client(For " + storageId + ")]");
            System.out.println(" - Encrypt WriteRequest");
            String jweWriteReq = client.makeWriteRequest(storage.getStorageId(), iat, clues[i]);
            System.out.println("   * WriteRequest  : " + jweWriteReq);
            System.out.println("           header  : " + printHeader(jweWriteReq));

            System.out.println(" [Storage(" + storageId + ")]");
            System.out.println(" - Decrypt WriteRequest");
            String jweWriteResp = storage.processWrite(jweWriteReq, iat);
            System.out.println("   * WriteResponse : " + jweWriteResp);
            System.out.println("           header  : " + printHeader(jweWriteResp));
            System.out.println();
            WriteResponse response = client.checkWriteResponse(storageId, jweWriteResp);
            if(response.getError() != null)
                throw new LiteVaultException(response.getError());

        }

        // 5. Read Request(Client to Storage)
        System.out.println("\n3-2. Read Process(Client <> Storage)");
        iat = makeIAT();
        String[] encClues = new String[StorageNum];
        int rSeq = 0;
        for(int i=0; i<storages.length; i++) {
            Storage storage = storages[i];
            String storageId = storage.getStorageId();
            System.out.println(" [Client(For " + storageId + ")]");
            System.out.println(" - Encrypt ReadRequest");
            String jweReadReq = client.makeReadRequest(storage.getStorageId(), iat);
            System.out.println("   * ReadRequest   : " + jweReadReq);
            System.out.println("            header : " + printHeader(jweReadReq));

            System.out.println(" [Storage(" + storageId + ")]");
            System.out.println(" - Decrypt ReadRequest");
            String jweReadResp = storage.processRead(jweReadReq, iat);
            System.out.println("   * ReadResponse  :" + jweReadResp);
            System.out.println("            header : " + printHeader(jweReadResp));

            ReadResponse response = client.checkReadResponse(storageId, jweReadResp);
            if(response.getError() != null)
                throw new LiteVaultException(response.getError());
            String encClue = response.getData();
            System.out.println("   * Encrypted clue: " + encClue);
            encClues[i] = encClue;
            System.out.println();
        }

        // 6. Reconstruct Data(Client)
        System.out.println("\n4. Reconstruct Data(Client)");
        System.out.println(" - Encrypted Clues ");
        for(String encClue : encClues)
            System.out.println("   *               : " + encClue);
        byte[] reconstructed = client.reconstruct(StorageNum, ThresholdNum, encClues);
        System.out.println(" - Reconstructed   : " + new String(reconstructed, Charset.forName("UTF-8")));
        System.out.println("              hash : " + Hex.toHexString(reconstructed));
        System.out.println(" - Original        : " + plainText);
        System.out.println("              hash : " + Hex.toHexString(plainText.getBytes(Charset.forName("UTF-8"))));
        System.out.println(" - Result          : " + Arrays.areEqual(plainText.getBytes(Charset.forName("UTF-8")), reconstructed));
    }

    private static Manager createManager() throws Exception {
        ECKey managerSignKey = generateECKey(CurveName);
        String managerSignKeyId = makeID(managerSignKey);
        ECKey managerKmKey = generateECKey(CurveName);
        String managerKmKeyId = makeID(managerKmKey);
        System.out.println(" - Manager(" + managerSignKeyId + ") Signing Key : " + managerSignKey.toJsonObject(true));
        System.out.println(" - Manager(" + managerKmKeyId + ") KM Key      : " + managerKmKey.toJsonObject(true));
        return new Manager(EncAlgorithm, managerSignKeyId, managerSignKey, managerKmKeyId, managerKmKey);
    }

    private static Storage createStorage() throws Exception {
        ECKey storageKey = generateECKey(CurveName);
        String storageId = makeID(storageKey);
        System.out.println(" - Storage(" + storageId + ") KM Key : " + storageKey.toJsonObject(true));
        return new Storage(storageId, EncAlgorithm, storageKey, ManagerSignKeyId, ManagerSignKey, ManagerKmKeyId, ManagerKmKey);
    }

    private static Client createClient() throws Exception {
        ECKey clientKey = generateECKey(CurveName);
        System.out.println(" - Client KM Key : " + clientKey.toJsonObject(true));
        return new Client(JweAlgorithm, EncAlgorithm, clientKey, ManagerSignKeyId, ManagerSignKey, ManagerKmKeyId, ManagerKmKey);
    }

    // manager, storage ID
    private static String makeID(ECKey key) {
        byte[] point = ECUtils.toEncodedPointFromBCEDPublicKey(key.getPublicKey(), false);
        return Hex.toHexString(Utils.sha256Digest(point)).substring(0, 16);
    }

    private static ECKey generateECKey(String curveName) throws Exception {
        KeyPair mSignPair = ECUtils.generateKeyPair(curveName);
        return new ECKey(curveName, mSignPair);
    }

    private static long makeIAT() {
        return System.currentTimeMillis()/1000;
    }

    private static String printHeader(String jwtObject) {
        int index = jwtObject.indexOf(".");
        String header = jwtObject.substring(0, index);
        return new String(Utils.decodeFromBase64UrlSafeString(header));
    }

    private static String printPayload(String jwtObject) {
        String[] parts = jwtObject.split("\\.");
        return new String(Utils.decodeFromBase64UrlSafeString(parts[1]));
    }
}
