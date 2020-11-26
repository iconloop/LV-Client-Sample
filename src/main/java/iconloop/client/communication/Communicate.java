package iconloop.client.communication;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

public class Communicate {
    public String communicate(String url, JsonObject token_request) throws Exception{
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "TOKEN_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vC", "header.payload.signature");

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Authorization", token_request.toString());

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpGet);

        if (response.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            return handler.handleResponse(response);
        } else {
            return "response is error : " + response.getStatusLine().getStatusCode();
        }
    }
}
