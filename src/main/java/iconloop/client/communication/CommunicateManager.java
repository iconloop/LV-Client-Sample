package iconloop.client.communication;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

public class CommunicateManager extends Communicate {
    private String url = "http://127.0.0.1:8100/vault";

    public void requestIssueVC() {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "ISSUE_VC_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);

        JsonObject auth = new JsonObject();
        auth.addProperty("email", "a@bb.com");
        auth.addProperty("sms", "+821012345678");
        raw_token_request.addProperty("auth", auth.toString());

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
