import com.github.signaflo.math.operations.DoubleFunctions;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.forecast.Forecast;
import com.github.signaflo.timeseries.model.arima.Arima;
import com.github.signaflo.timeseries.model.arima.ArimaOrder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestWord;
import de.vandermeer.asciithemes.a7.A7_Grids;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.h2.util.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import java.sql.*;
import java.util.*;

//

//* @author couedrao on 25/11/2019.

//* @project gctrl

//

//


//* 1)Collects the details from the managed resources e g topology Collects the details from the managed resources e.g.  topology information, metrics (e.g. offered capacity and throughput), configuration property settings and so on.


//* 2)The monitor function aggregates,correlates and filters these details until it determines a symptom that needs to be analyzed.


//*


@SuppressWarnings({"SynchronizeOnNonFinalField"})
class Monitor {
    private static List<String> symptom;
    private static final int period = 2000;
    public String gw_current_SYMP = "N/A";
    private static final MANOAPI manoapi = new MANOAPI();
    private static final SDNCtrlAPI sdnctlrapi = new SDNCtrlAPI();
    private Map<String, Integer> oldCount = new HashMap<>();
    private boolean isOk = true;

    void start() {
        Main.logger(this.getClass().getSimpleName(), "Start monitoring of " + Knowledge.gw);

        Main.logger(this.getClass().getSimpleName(), "Deploying Monitor");
        String newdestip = manoapi.deploy_vnf(Main.shared_knowledge.getMonitorInfo());
        Main.shared_knowledge.setOldMonitorIp(newdestip);
        Main.logger(this.getClass().getSimpleName(), "Redirecting Traffic GFs to Monitor");
        for (String ip : Main.shared_knowledge.getGFs()) {
            String status = sdnctlrapi.redirect_traffic(Main.shared_knowledge.getOldMonitorIp(), ip, 100);
            Main.logger(this.getClass().getSimpleName(), status);
            oldCount.put(ip,0);
        }

        symptom = Main.shared_knowledge.get_symptoms();
        data_collector(); //in bg
    }

    //Data Collector
    private void data_collector() {
        new Thread(() -> {
            Main.logger(this.getClass().getSimpleName(), "Monitor : Launching data_collector thread");
            while (Main.run)
                try {
                    Thread.sleep(period);
                    Map<String, Integer> newCount = get_data();
                    String result = data_check(newCount);
                    if(result.equals("OK")){
                        if(!this.isOk){
                            Main.shared_knowledge.setProblematicIp("N/A");
                            update_symptom(symptom.get(2));
                            this.isOk = true;
                        }
                    } else {
                        if(this.isOk){
                            Main.shared_knowledge.setProblematicIp(result);
                            update_symptom(symptom.get(1));
                            this.isOk = false;
                        }
                    }
                    for (String ip : Main.shared_knowledge.getGFs()) {
                        this.oldCount.put(ip,newCount.get(ip));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        ).start();
    }

    //Check if there is an issue, returns problematic ip if there is
    private String data_check(Map<String, Integer> newCount){

        for (String ip : Main.shared_knowledge.getGFs()) {
            int value = newCount.get(ip) - this.oldCount.get(ip);
            if(value > 10){
                return ip;
            }
        }
        return "OK";
    }

    private Map<String, Integer> get_data() {
        //Call Sensors
        Map<String, Integer> newCount = new HashMap<>();
        try {
            Request request = new Request.Builder()
                    .url("http://10.0.0.22:8181/health")
                    .build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            Gson gson = new Gson();
            newCount = gson.fromJson(response.body().string(), new TypeToken<HashMap<String, Integer>>(){}.getType());
            Main.logger(this.getClass().getSimpleName(), "Monitoring Request Answer : " + newCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newCount;
    }

    private void update_symptom(String symptom) {

        synchronized (gw_current_SYMP) {
            gw_current_SYMP.notify();
            gw_current_SYMP = symptom;

        }
    }


}