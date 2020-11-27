package iconloop.client.communication;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class CommunicateStorage extends Communicate {
    private static final String url = "http://127.0.0.1:8100/vault";

    public void requestToken() {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "TOKEN_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vC", "header.payload.signature");

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestStoreClue(String clue, String vID) {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "STORE_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vID", vID);
        raw_token_request.addProperty("clue", clue);
        raw_token_request.addProperty("sequence", 0);

        try {
            System.out.println(communicate(url, raw_token_request));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String requestClue(String vID) {
        JsonObject raw_token_request = new JsonObject();
        raw_token_request.addProperty("type", "CLUE_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vID", vID);

        String result = "";
        try {
            result = communicate(url, raw_token_request);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject convertedObject = new Gson().fromJson(result, JsonObject.class);
        return convertedObject.get("clue").toString();
    }
}
