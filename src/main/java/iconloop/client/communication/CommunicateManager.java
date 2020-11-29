package iconloop.client.communication;

import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class CommunicateManager extends Communicate {
    private static final String url = "http://127.0.0.1:8000/vault";

    public String requestIssueVID() {
        JsonObject raw_token_request = new JsonObject();
        String result = "";

        raw_token_request.addProperty("type", "ISSUE_VID_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);

        JsonObject auth = new JsonObject();
        auth.addProperty("email", "a@bb.com");
        auth.addProperty("sms", "+821012345678");

        raw_token_request.add("auth", auth);

        try {
            prettyPrint(result = communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }

        JsonObject convertedObject = new Gson().fromJson(result, JsonObject.class);
        return convertedObject.get("vID").toString();
    }

    public void requestIssueVC(String vID) {
        JsonObject raw_token_request = new JsonObject();

        raw_token_request.addProperty("type", "ISSUE_VC_REQUEST");
        raw_token_request.addProperty("iat", 1606125053);
        raw_token_request.addProperty("vID", vID);

        try {
            prettyPrint(communicate(url, raw_token_request));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
