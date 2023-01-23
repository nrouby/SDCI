import okhttp3.*;

import java.util.List;

/**
 * @author couedrao on 27/11/2019.
 * @project gctrl
 */
class SDNCtrlAPI {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    String redirect_traffic(String dest, String origin, int priority) {
        String status = "OK";
        Main.logger(this.getClass().getSimpleName(), "destination = " + dest + "; origin = " + origin);

        try {
            String data = "{\"idle_timeout\": 0,\"hard_timeout\": 0,\"priority\": "+ priority +
                    ",\"match\":{\"eth_type\": 2048,\"ipv4_src\": \"" + origin +
                    "\" },\"actions\":[ {\"type\":\"SET_FIELD\",\"field\":\"ipv4_dst\",\"value\":\"" + dest +
                    "\" }, {\"type\":\"OUTPUT\",\"port\":\"CONTROLLER\" }] }";
            RequestBody body = RequestBody.create(data, JSON);
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:8080/stats/flowentry/add/")
                    .addHeader("Content-Type", "application/json")
                    .put(body)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            Main.logger(this.getClass().getSimpleName(), "Deployed Rule destination = " + dest + "; origin = " + origin);
            status = response.message();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }


}
