package tw.edu.au.csie.ucan.beebit.seco.cpabe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.edu.au.csie.ucan.beebit.cpabeJNI;
import tw.edu.au.csie.ucan.beebit.seco.Project;
import tw.edu.au.csie.ucan.beebit.seco.User;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cpabe")
public class CpabeKeygenController {

    @RequestMapping(value = "/keygen", method={RequestMethod.POST})
    public String cpabeKeygen(
            @RequestParam(value="req",defaultValue="") String req,
            @RequestParam(value="key",defaultValue="", required=false) String key) throws JSONException {

        JSONObject resp = new JSONObject();
        String puid;
        String pid;
        JSONArray attrs;

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, String.format("[ERROR] Parameter req is required"), Optional.empty());
            return resp.toString();
        }

        try {
            JSONObject input = new JSONObject(req);
            puid = input.getString("puid").trim();
            pid = input.getString("pid").trim();
            attrs = input.getJSONArray("attributes");

            JdbcTemplate db = Utils.connectDb();
            /*
            List<User> users = db.query(
                    String.format("select * from users where uid ='%s'", uid),
                    (rs, rowNum) -> new User(rs.getString("uid"))
            );
            if (users.isEmpty()) {
                resp = Utils.createResponse(500, String.format("[ERROR] User not exist"), Optional.empty());
                return resp.toString();
            }
            */
            List<Project> projects = db.query(
                    String.format("select * from project where pid ='%s'", pid),
                    (rs, rowNum) -> new Project(rs.getString("uid"), rs.getString("pid"))
            );
            if (projects.isEmpty()) {
                resp = Utils.createResponse(500, String.format("[ERROR] Project not exist"), Optional.empty());
                return resp.toString();
            }

            /*
            String uid = projects.get(0).getUid().trim();
            if(Utils.createFolder(String.format("%s/%s/%s/cpabe", Utils.root, uid, pid)) == false) {
                resp = Utils.createResponse(500, String.format("[ERROR] Cannot create algorigm (cpabe) dir"), Optional.empty());
                return resp.toString();
            }
            */

            String[] att = new String[attrs.length()];
            for(int i=0; i<att.length; i++) {
                att[i]=attrs.optString(i);
            }

            String uid = projects.get(0).getUid().trim();
            if(keygenProcess(key, uid, pid, puid, att) == false) {
                //Utils.deleteFolder(String.format("%s/%s/%s", Utils.root, uid, pid));
                resp = Utils.createResponse(500, String.format("[ERROR] CPABE keygen error"), Optional.empty());
                return resp.toString();
            }

        } catch (JSONException e) {
            Utils.createResponse(40, String.format("[ERROR] JSON parse error (%s)", e.toString()), Optional.empty());
            return resp.toString();
        }

        resp = Utils.createResponse(200, String.format("[INFO] CPABE keygen success"), Optional.empty());
        return resp.toString();
    }

    boolean keygenProcess(String key, String uid, String pid, String puid, String[] atts) throws JSONException {
        try {
            cpabeJNI bee = new cpabeJNI();
            String pk = String.format("%s/%s/%s/cpabe/pk", Utils.root, uid, pid) ;
            String mk = String.format("%s/%s/%s/cpabe/mk", Utils.root, uid, pid) ;
            String sk = String.format("%s/%s/%s/cpabe/sk_%s", Utils.root, uid, pid, puid) ;
            if(bee.keygen(pk,mk, sk, atts.length, atts) == -1)
                return false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }
}