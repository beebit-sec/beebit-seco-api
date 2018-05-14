package tw.edu.au.csie.ucan.beebit.seco.cpabe;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import tw.edu.au.csie.ucan.beebit.cpabeJNI;
import tw.edu.au.csie.ucan.beebit.seco.Project;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.io.File;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cpabe")
public class CpabeEncController {

    @RequestMapping(value = "/enc/{type}", method={RequestMethod.POST})
    public String cpabeEnc(@RequestParam(value="req",defaultValue="") String req,
                      @RequestParam(value="key",defaultValue="") String key,
                      @PathVariable String type) throws JSONException {

        JSONObject resp = new JSONObject();
        String cipher = null;

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, String.format("[ERROR] Parameter req is required"), Optional.empty());
            return resp.toString();
        }

        if(!type.equals("data") && !type.equals("file")) {
            resp = Utils.createResponse(500, String.format("[ERROR] Type not support"), Optional.empty());
            return resp.toString();
        }

        try {
            String FileName = "";
            String username = "";
            String Projectname = "";
            String policy = "";
            String Keyname = "";
            String pid = "";
            String data = null;

            JSONObject input = new JSONObject(req);
            policy = input.getString("policy").trim();

            switch(type) {
                case "file":  // File
                    //JSONObject dbjson = new JSONObject(userdao.key(APIKey));
                    //username = dbjson.getAsString("username");
                    //Projectname = dbjson.getAsString("project");
                    FileName = input.getString("filename");
                    Keyname = input.getString("user");
                    // TEST
                    System.out.println(FileName);
                    System.out.println(username);
                    System.out.println(Projectname);
                    resp = encFile(key, username, Projectname, Keyname, FileName, policy, resp);
                    break;

                case "data":  // Data
                    pid = input.getString("pid").trim();
                    data = input.getString("data").trim();

                    JdbcTemplate db = Utils.connectDb();
                    List<Project> projects = db.query(
                            String.format("select * from project where pid ='%s'", pid),
                            (rs, rowNum) -> new Project(rs.getString("uid"), rs.getString("pid"))
                    );

                    if (projects.isEmpty()) {
                        resp = Utils.createResponse(500, String.format("[ERROR] Project not Exist"), Optional.empty());
                        return resp.toString();
                    }

                    String uid = projects.get(0).getUid().trim();
                    cipher = encData(key, uid, pid, data, policy);
                    if(cipher == null) {
                        resp = Utils.createResponse(500, String.format("[ERROR] CPABE encryption error"), Optional.empty());
                        return resp.toString();
                    }
                    break;

                default:
                    resp = Utils.createResponse(500, String.format("[ERROR] Type not support"), Optional.empty());
                    return resp.toString();
            }
            //return resp.toString();
        } catch (JSONException e) {
            resp = Utils.createResponse(40, String.format("[ERROR] JSON parse error"), Optional.empty());
            return resp.toString();
        }

        resp = Utils.createDataResponse(200, String.format("[INFO] CPABE encryption success"), cipher);
        return resp.toString();
    }

    JSONObject encFile(String key, String username, String Projectname, String Keyname, String FileName, String policy, JSONObject resp) {
        //UserDao userdao = new UserDao();
        //if (userdao.key(key) != "NotFound") {
            // do check file 有的話在加密 else json　回錯
            Process p;
            File file = new File("/CPABE/" + username + "/" + Projectname + "/"
                    + Keyname + "/" + "Ciphertext/" + FileName);
            System.out.println(file.exists() + "/CPABE/" + username + "/"
                    + Projectname + "/" + Keyname + "/" + "Ciphertext/"
                    + FileName);
            if (file.exists()) {
                try {
                    //p = Runtime.getRuntime().exec("echo cpabe-enc /CPABE/ucan/hihihifortest/public_key /CPABE/ucan/hihihifortest/jj/Ciphertext/this.jpg 'girl and (boy or college)' >/CPABE/KEY.log");
                    p = Runtime.getRuntime().exec(
                            new String[] {
                                    "cpabe-enc",
                                    "/CPABE/" + username + "/" + Projectname
                                            + "/public_key",
                                    "/CPABE/" + username + "/" + Projectname
                                            + "/" + Keyname + "/Ciphertext/"
                                            + FileName, policy });
                    p.waitFor();
                    System.out.println("cpabe-enc" + "/CPABE/" + username + "/"
                            + Projectname + "/public_key" + "/CPABE/"
                            + username + "/" + Projectname + "/" + Keyname
                            + "/Ciphertext/" + FileName + policy);
                    System.out.println("exit: " + p.exitValue());
                    p.destroy();

                    if (p.exitValue() == 0) {
                        resp.put("msg", "encrypt success");
                        resp.put("code", 200);
                        return resp;
                    } else {
                        resp.put("msg", "encrypt failed");
                        resp.put("code", 110);
                        return resp;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    return resp;
                }
            } else {
                try {
                    resp.put("msg", "File not found");
                    resp.put("code", 445);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return resp;
            }
        //} else {  // api key not found
        //    try {
        //        obj.put("Message","APIKEY not foundl");
        //        obj.put("code", 20);
        //        response.getWriter().write(obj.toString());
        //    } catch (JSONException e) {
        //        // TODO Auto-generated catch block
        //        System.out.println("ERROR:2 " + e.toString());
        //    }
        //    return;
        //}
    }

    String encData(String key, String uid, String pid, String data, String policy) throws JSONException {

        byte[] ct;

        System.out.print(data.length()+"\n");

        try {
            System.out.print(Base64.getDecoder().decode(data).length+"\n");
            cpabeJNI bee = new cpabeJNI();
            ct = bee.enc(String.format("%s/%s/%s/cpabe/pk", Utils.root, uid, pid), Base64.getDecoder().decode(data), policy);
            System.out.print(ct.length+"\n");
            if(ct == null)
                return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return Base64.getEncoder().encodeToString(ct);
    }
}