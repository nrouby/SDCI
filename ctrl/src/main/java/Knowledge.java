import org.h2.tools.DeleteDbFiles;

import java.sql.*;
import java.util.*;

//

//* @author couedrao on 25/11/2019.

//* @project gctrl

//

//

//* 1)Standard data shared among the monitor analyze plan and Standard data shared among the monitor, analyze, plan and execute functions

//* 2)The shared knowledge includes data such as topology information, historical logs, metrics, symptoms and policies

//* 3)Created by the monitor part while execute part might update the knowledge

//*

class Knowledge {

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:~/test";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    static final int moving_wind = 10;
    static final int horizon = 3;
    static final String gw = "GW_I";
    static final double gw_lat_threshold = 20;

    private static final List<String> symptom = Arrays.asList("N/A", "NOK", "OK");
    private static final List<String> rfc = Arrays.asList("DoNotDoAnything", "DecreaseLatencyIn" + gw);
    private static final List<String> workflow_lists = Arrays.asList("UC1", "UC2/UC3");
    private static final List<String> plan = Arrays.asList("A", "B");
    private final Map<String, String> dropperinfo = new HashMap<>();
    private final Map<String, String> monitorinfo = new HashMap<>();
    private final String olddestip = "10.0.0.4";
    private final List<String> GFs = List.of("10.0.0.1", "10.0.0.2", "10.0.0.3");
    private String newdestip;
    private String olddropperip;
    private String oldmonitorip;
    private String problematicip = "N/A";

    void start() throws Exception {
        // delete the H2 database named 'test' in the user home directory
        DeleteDbFiles.execute("~", "test", true);
        Main.logger(this.getClass().getSimpleName(), "old database 'test' deleted");
        //Initialization of the Knowledge
        store_symptoms();
        store_rfcs();
        store_plans();
        store_execution_workflow();

        dropperinfo.put("name", "dropper");
        dropperinfo.put("image", "dropper:latest");
        dropperinfo.put("net", "(id=dropper-eth0,ip=10.0.0.21/24,port=5001)");
        dropperinfo.put("ip", "10.0.0.21");

        monitorinfo.put("name", "monitor");
        monitorinfo.put("image", "monitor:latest");
        monitorinfo.put("net", "(id=monitor-eth0,ip=10.0.0.22/24,port=5001)");
        monitorinfo.put("ip", "10.0.0.22");

        Main.logger(this.getClass().getSimpleName(), "Knowledge Starting");

    }

    List<String> get_symptoms() {
        String gw_symp = gw + "_SYMP";

        Connection conn = getDBConnection();
        String SelectQuery = "select * from " + gw_symp;
        PreparedStatement select;
        List<String> r = null;
        try {
            select = conn.prepareStatement(SelectQuery);
            ResultSet rs = select.executeQuery();
            r = new ArrayList<>();
            while (rs.next()) {
                r.add(rs.getString("symptom"));
            }
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;

    }

    List<String> get_rfc() {
        String gw_rfc = gw + "_RFC";

        Connection conn = getDBConnection();
        String SelectQuery = "select * from " + gw_rfc;
        PreparedStatement select;
        List<String> r = null;
        try {
            select = conn.prepareStatement(SelectQuery);
            ResultSet rs = select.executeQuery();
            r = new ArrayList<>();
            while (rs.next()) {
                r.add(rs.getString("rfc"));
            }
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;

    }

    List<String> get_plans() {
        String gw_plan = gw + "_PLAN";

        Connection conn = getDBConnection();
        String SelectQuery = "select * from " + gw_plan;
        PreparedStatement select;
        List<String> r = null;
        try {
            select = conn.prepareStatement(SelectQuery);
            ResultSet rs = select.executeQuery();
            r = new ArrayList<>();
            while (rs.next()) {
                r.add(rs.getString("plan"));
            }
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;

    }

    List<String> get_worklow_lists() {
        String gw_execw = gw + "_EXECW";

        Connection conn = getDBConnection();
        String SelectQuery = "select * from " + gw_execw;
        PreparedStatement select;
        List<String> r = null;
        try {
            select = conn.prepareStatement(SelectQuery);
            ResultSet rs = select.executeQuery();
            r = new ArrayList<>();
            while (rs.next()) {
                r.add(rs.getString("workflow"));
            }
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;

    }

    ResultSet select_from_tab() {
        //Main.logger("Select the last " + n + " latencies");
        Connection conn = getDBConnection();
        String SelectQuery = "select TOP " + moving_wind + " * from " + Knowledge.gw + "_LAT" + " ORDER BY id DESC";
        //PreparedStatement select;
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            // select = conn.prepareStatement(SelectQuery);
            rs = stmt.executeQuery(SelectQuery);
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;


    }

    void create_lat_tab() {
        try (Connection conn = getDBConnection()) {
            Statement create;
            conn.setAutoCommit(false);
            create = conn.createStatement();
            create.execute("CREATE TABLE " + Knowledge.gw + "_LAT" + " (id timestamp primary key, latency double )");
            create.close();
            conn.commit();
        } catch (SQLException e) {
            System.out.println("Exception Message " + e.getLocalizedMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Main.logger(this.getClass().getSimpleName(), "... Database Created");

        }
    }

    private void store_plans() throws SQLException {
        String gw_plan = gw + "_PLAN";
        Connection conn = getDBConnection();
        Statement create;
        conn.setAutoCommit(false);
        create = conn.createStatement();
        create.execute("CREATE TABLE " + gw_plan + " (id int primary key, plan varchar(20) )");
        create.close();

        for (int i = 0; i < plan.size(); i++) {
            conn = getDBConnection();
            PreparedStatement insert;
            try {
                insert = conn.prepareStatement("INSERT INTO " + gw_plan + " (id, plan) values" + "(?,?)");
                insert.setInt(1, i + 1);
                insert.setString(2, plan.get(i));
                insert.executeUpdate();
                insert.close();
                conn.commit();
            } catch (SQLException e) {
                System.out.println("Exception Message " + e.getLocalizedMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.close();
            }
        }
    }

    private void store_rfcs() throws SQLException {
        String gw_rfc = gw + "_RFC";
        Connection conn = getDBConnection();
        Statement create;
        conn.setAutoCommit(false);
        create = conn.createStatement();
        create.execute("CREATE TABLE " + gw_rfc + " (id int primary key, rfc varchar(40) )");
        create.close();

        for (int i = 0; i < rfc.size(); i++) {
            conn = getDBConnection();
            PreparedStatement insert;
            try {
                insert = conn.prepareStatement("INSERT INTO " + gw_rfc + " (id, rfc) values" + "(?,?)");
                insert.setInt(1, i + 1);
                insert.setString(2, rfc.get(i));
                insert.executeUpdate();
                insert.close();
                conn.commit();
            } catch (SQLException e) {
                System.out.println("Exception Message " + e.getLocalizedMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.close();
            }
        }
    }

    private void store_execution_workflow() throws SQLException {
        String gw_execw = gw + "_EXECW";
        Connection conn = getDBConnection();
        Statement create;
        conn.setAutoCommit(false);
        create = conn.createStatement();
        create.execute("CREATE TABLE " + gw_execw + " (id int primary key, workflow varchar(50) )");
        create.close();

        for (int i = 0; i < workflow_lists.size(); i++) {
            conn = getDBConnection();
            PreparedStatement insert;
            try {
                insert = conn.prepareStatement("INSERT INTO " + gw_execw + " (id, workflow) values" + "(?,?)");
                insert.setInt(1, i + 1);
                insert.setString(2, workflow_lists.get(i));
                insert.executeUpdate();
                insert.close();
                conn.commit();
            } catch (SQLException e) {
                System.out.println("Exception Message " + e.getLocalizedMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.close();
            }
        }
    }

    private void store_symptoms() throws SQLException {
        String gw_symp = gw + "_SYMP";
        Connection conn = getDBConnection();
        Statement create;
        conn.setAutoCommit(false);
        create = conn.createStatement();
        create.execute("CREATE TABLE " + gw_symp + " (id int primary key, symptom varchar(5) )");
        create.close();

        for (int i = 0; i < symptom.size(); i++) {
            conn = getDBConnection();
            PreparedStatement insert;

            try {
                insert = conn.prepareStatement("INSERT INTO " + gw_symp + " (id, symptom) values" + "(?,?)");
                insert.setInt(1, i + 1);
                insert.setString(2, symptom.get(i));
                insert.executeUpdate();
                insert.close();
                conn.commit();
            } catch (SQLException e) {
                System.out.println("Exception Message " + e.getLocalizedMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.close();
            }
        }
    }

    private Connection getDBConnection() {
        // Main.logger("Connecting the database ...");
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try {
            return DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    public Map<String, String> getDropperInfo() {
        return dropperinfo;
    }

    public String getOlddestip() {
        return olddestip;
    }

    public String getNewdestip() {
        return newdestip;
    }

    public void setNewdestip(String newdestip) {
        this.newdestip = newdestip;
    }

    public String getOldDropperIp() {
        return olddropperip;
    }

    public void setOldDropperIp(String olddropperip) {
        this.olddropperip = olddropperip;
    }

    public String getOldMonitorIp() {
        return oldmonitorip;
    }

    public void setOldMonitorIp(String oldmonitorip) {
        this.oldmonitorip = oldmonitorip;
    }

    public String getProblematicIp() {
        return problematicip;
    }

    public void setProblematicIp(String problematicip) {
        this.problematicip = problematicip;
    }

    public Map<String, String> getMonitorInfo() {
        return monitorinfo;
    }

    public List<String> getGFs() {
        return GFs;
    }
}