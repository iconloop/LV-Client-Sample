package iconloop.client.communication;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import iconloop.client.util.Clue;
import java.nio.charset.Charset;



public class CommunicateStorage extends Communicate{
    private static final String url = "http://127.0.0.1:8100/vault";
    private static final String sharingInfo = "분한을 위한 데이터 (....!!)";
    private static final Clue clue = new Clue();

    public void requestToken() {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "TOKEN_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vC", "header.payload.signature");

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void requestStoreClue(String clue) {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "STORE_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vID", "vID");
        raw_token_request.addProperty("clue", clue);
        raw_token_request.addProperty("sequence", 0);

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void requestClue() {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "CLUE_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vID", "vID");

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
