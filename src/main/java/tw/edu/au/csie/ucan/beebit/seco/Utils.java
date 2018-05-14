package tw.edu.au.csie.ucan.beebit.seco;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.File;
import java.util.Optional;

public class Utils {

    public static String root = "./seco";
    public static String[] algos = {"cpabe"};

    static String db_host = "jdbc:postgresql://127.0.0.1/beebit";
    static String db_user = "db_user";
    static String db_pwd = "db_pwd";

    public static JdbcTemplate connectDb() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.postgresql.Driver.class);
        dataSource.setUrl(db_host);
        dataSource.setUsername(db_user);
        dataSource.setPassword(db_pwd);
        JdbcTemplate db = new JdbcTemplate(dataSource);
        return db;
    }

    public static JSONObject createJsonResponse(int code, String msg, JSONArray data) {
        JSONObject resp = new JSONObject();
        resp.put("code", code);
        resp.put("msg", msg);
        resp.put("data", data);
        return resp;
    }

    public static JSONObject createDataResponse(int code, String msg, String data) {
        JSONObject resp = new JSONObject();
        resp.put("code", code);
        resp.put("msg", msg);
        resp.put("data", data);
        return resp;
    }

    public static JSONObject createResponse(int code, String msg, Optional<String> data) {
        JSONObject resp = new JSONObject();
        resp.put("code", code);
        resp.put("msg", msg);
        if(data.isPresent()) {
            resp.put("data", data.toString());
        }
        return resp;
    }

    public static boolean createFolder(String path) {

        boolean result = false;

        try {
            File theDir = new File(path);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            result = true;
        } catch(SecurityException se) {
            se.printStackTrace();
        }
        return result;
    }

    public static boolean deleteFolder(String path) {

        boolean result = false;

        try {
            File theDir = new File(path);
            // if the directory does not exist, create it
            if (theDir.exists()) {
                theDir.delete();
                result = true;
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        }
        return result;
    }
}
