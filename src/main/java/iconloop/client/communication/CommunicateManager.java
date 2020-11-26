package iconloop.client.communication;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

public class CommunicateManager {
    private String url = "http://127.0.0.1:8100/vault";

    public void CommunicateSample() {

        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "TOKEN_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vC", "header.payload.signature");

        try {
            HttpGet httpGet = new HttpGet(this.url);
            httpGet.addHeader("Authorization", raw_token_request.toString());

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(httpGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);
                System.out.println(body);
            } else {
                System.out.println("response is error : " + response.getStatusLine().getStatusCode());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
