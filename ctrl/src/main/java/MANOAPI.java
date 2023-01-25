
import okhttp3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


/**
 * @author couedrao on 27/11/2019.
 * @project gctrl
 */
class MANOAPI {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    String deploy_vnf(Map<String, String> vnfinfos) {
        String ip = vnfinfos.get("ip");
        Main.logger(this.getClass().getSimpleName(), "Deploying VNF " + vnfinfos.get("name") + " : ");

        //printing
        for (Entry<String, String> e : vnfinfos.entrySet()) {
            Main.logger(this.getClass().getSimpleName(), "\t" + e.getKey() + " : " + e.getValue());
        }

        try {
            String data = "{\"image\":\"" + vnfinfos.get("image") + "\", \"network\":" + vnfinfos.get("net") + "}";
            String adress = "http://127.0.0.1:5001/restapi/compute/dc1/" + vnfinfos.get("name");
            RequestBody body = RequestBody.create(data, JSON);
            Request request = new Request.Builder()
                    .url(adress)
                    .addHeader("Content-Type", "application/json")
                    .put(body)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Main.logger(this.getClass().getSimpleName(), adress +" "+data);
            Response response = client.newCall(request).execute();
            Main.logger(this.getClass().getSimpleName(), "Deployed VNF " + vnfinfos.get("name") + " : \n " + response.body().string() +"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ip;
    }
}
