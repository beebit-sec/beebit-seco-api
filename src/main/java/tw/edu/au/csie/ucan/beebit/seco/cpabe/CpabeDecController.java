package tw.edu.au.csie.ucan.beebit.seco.cpabe;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import tw.edu.au.csie.ucan.beebit.cpabeJNI;
import tw.edu.au.csie.ucan.beebit.seco.Project;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cpabe")
public class CpabeDecController {

    @RequestMapping(value = "/dec/{type}", method={RequestMethod.POST, RequestMethod.GET})
    public String cpabeDec(@RequestParam(value="req",defaultValue="") String req,
                      @RequestParam(value="key",defaultValue="") String key,
                      @PathVariable String type) {

        String plain = null;

        JSONObject resp = new JSONObject();

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
            String owner = "";
            String user = "";
            String username = "";
            String Projectname = "";
            String pid = "";
            String puid = "";
            String data = "";

            JSONObject input = new JSONObject(req);

            switch(type) {
                case "file":  // File
                    //JSONObject dbjson = new JSONObject(userdao.key(APIKey));
                    //username = dbjson.getAsString("username");
                    //Projectname = dbjson.getAsString("project");
                    FileName = input.getString("filename");
                    owner = input.getString("owner");
                    user = input.getString("user");
                    System.out.println(username);
                    System.out.println(Projectname);
                    System.out.println(FileName);
                    System.out.println(owner);
                    System.out.println(user);
                    resp = decFile(key, username, owner, user, Projectname, FileName, resp);
                    break;

                case "data":  // data
                    pid  = input.getString("pid").trim();
                    puid  = input.getString("puid").trim();
                    data = input.getString("data").trim();

                    JdbcTemplate db = Utils.connectDb();

                    List<Project> projects = db.query(
                            String.format("select * from project where pid ='%s'", pid),
                            (rs, rowNum) -> new Project(rs.getString("uid"), rs.getString("pid"))
                    );
                    if (projects.isEmpty()) {
                        resp = Utils.createResponse(500, String.format("[ERROR] Project not exist"), Optional.empty());
                        return resp.toString();
                    }

                    String uid = projects.get(0).getUid().trim();
                    plain = decData(key, uid, pid, puid, data);
                    if(plain == null) {
                        resp = Utils.createResponse(500, String.format("[ERROR] CPABE decryption error"), Optional.empty());
                        return resp.toString();
                    }

                    break;

                default:
                    resp = Utils.createResponse(500, String.format("[ERROR] Type not support"), Optional.empty());
                    return resp.toString();
            }

            resp = Utils.createDataResponse(200, String.format("INFO] CPABE decryption success"), plain);
            return resp.toString();

        } catch (Exception e) {
            resp = Utils.createResponse(40, String.format("ERROR] JSON parse error"), Optional.empty());
            return resp.toString();
        }
    }

    JSONObject decFile(String key, String username, String owner, String user, String Projectname, String FileName, JSONObject resp) {
        String filename2 = null;
        //UserDao userdao = new UserDao();
        //if (userdao.key(APIKey) != "NotFound") {
            if (FileName.indexOf(".cpabe") == -1) {
                filename2 = FileName + ".cpabe";
            }
            System.out.println(filename2);
            File file = new File("/CPABE/" + username + "/" + Projectname + "/"
                    + owner + "/" + "Ciphertext/" + filename2);
            System.out.println(file + ", " + file.exists() + ", "
                    + file.isDirectory());

            if (file.exists()) {

                File file1 = new File("/CPABE/" + username + "/" + Projectname
                        + "/" + user + "/");
                System.out.println(file1 + ", " + file1.exists() + ", "
                        + file1.isDirectory());
                if (file1.exists() && file1.isDirectory()) {
                    String s;
                    try {
                        Process p;
                        p = Runtime.getRuntime().exec(
                                new String[] {
                                        "cp",
                                        "-a",
                                        "/CPABE/" + username + "/"
                                                + Projectname + "/" + owner
                                                + "/Ciphertext/" + filename2,
                                        "/CPABE/" + username + "/"
                                                + Projectname + "/" + user
                                                + "/Plaintext/" });
                        p.waitFor();
                        p = Runtime.getRuntime().exec(
                                new String[] {
                                        "cpabe-dec",
                                        "/CPABE/" + username + "/"
                                                + Projectname + "/public_key",
                                        "/CPABE/" + username + "/"
                                                + Projectname + "/" + user
                                                + "/" + user,
                                        "/CPABE/" + username + "/"
                                                + Projectname + "/" + user
                                                + "/Plaintext/" + filename2 });
                        p.waitFor();
                        // System.out.println("cpabe-dec","/CPABE/"+username+"/"+Projectname+"/public_key","/CPABE/"+username+"/"+Projectname+"/"+User2+"/"+User2,"/CPABE/"+username+"/"+Projectname+"/"+User2+"/Plaintext/"+filename2);
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(p.getInputStream()));
                        while ((s = br.readLine()) != null)
                            System.out.println("line: " + s);
                        p.waitFor();
                        System.out.println("exit: " + p.exitValue());
                        p.destroy();

                        if (p.exitValue() == 0) {
//                            File filePath = new File("/CPABE/" + username + "/" + Projectname
//                                    + "/" + user + "/Plaintext/" + FileName);// 檔案位置
//                            String fileName = filePath.getName();
//                            resp.setContentType("application/octet-stream");
//                            resp.setContentLength((int) filePath.length());
//                            resp.setHeader("Content-Disposition", "attachment; filename=\""
//                                    + fileName + "\"");
//                            byte[] buf = new byte[2048];// 1024~4096都可
//                            BufferedInputStream from = new BufferedInputStream(OutputStream to = resp.getOutputStream();
//                            int ln = from.read(buf);
//                            while (ln > 0) {
//                                to.write(buf);
//                                ln = from.read(buf);
//                            }
//                            to.flush();
//                            to.close();
//                            from.close();
//
//                            File file = new File("/CPABE/" + username + "/" + Projectname + "/"
//                                    + user + "/Plaintext/" + FileName);
//                            if (file.delete()) {
//                                System.out.println(file.getName() + " is deleted!");
//                            } else {
//                                System.out.println("Delete operation is failed.");
//                            }
                        } else {
                            resp.put("msg", "can't decrypt file");
                            resp.put("code", 502);// 失敗
                        }
                        return resp;
                    } catch (Exception e) {
                        System.out.println("Error: 3 " + e.toString());
                        return resp;
                    }
                } else {
                    // 沒有解密的目錄
                    try {
                        resp.put("msg","Not found directory");
                        resp.put("code", 446);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return resp;
                }
            } else {
                // 沒有加密過的檔案
                try {
                    resp.put("msg","Not found file");
                    resp.put("code", 445);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return resp;
            }

        //} else {
        //        resp.put("msg", "APIKEY not found");
        //        resp.put("code", 20); // key不符合
        //        return resp.toString();
        //}
    }

    String decData(String key, String uid, String pid, String puid, String data) throws JSONException {

        byte pt[];

        try {
            cpabeJNI bee = new cpabeJNI();
            byte[] ct = Base64.getDecoder().decode(data);
            String pk = String.format("%s/%s/%s/cpabe/pk", Utils.root, uid, pid).trim();
            String sk = String.format("%s/%s/%s/cpabe/sk_%s", Utils.root, uid, pid, puid).trim();
            pt = bee.dec(pk, sk, ct);
            if(pt == null)
                return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return Base64.getEncoder().encodeToString(pt);
    }
}