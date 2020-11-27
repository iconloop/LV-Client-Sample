package iconloop.client.communication;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

public class Communicate {
    public String communicate(String url, JsonObject token_request) throws Exception{
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", token_request.toString());

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(httpPost);

        if (response.getStatusLine().getStatusCode() == 200) {
            ResponseHandler<String> handler = new BasicResponseHandler();
            return handler.handleResponse(response);
        } else {
            throw new Exception("response is error : " + response.getStatusLine().getStatusCode());
        }
    }
}
