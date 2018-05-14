package tw.edu.au.csie.ucan.beebit.seco.project;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import tw.edu.au.csie.ucan.beebit.cpabeJNI;
import tw.edu.au.csie.ucan.beebit.seco.Project;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/project")
public class AlgoInitController {

    @RequestMapping(value = "/init/{algo}", method={RequestMethod.POST})
    public String algoInit(
            @RequestParam(value="req",defaultValue="") String req,
            @RequestParam(value="key",defaultValue="", required=false) String key,
            @PathVariable String algo) throws JSONException {

        JSONObject resp;
        String pid;

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, String.format("[ERROR] Parameter req is required"), Optional.empty());
            return resp.toString();
        }

        //if(algo.isEmpty()) {
        //    resp = Utils.createResponse(500, String.format("[ERROR] Path parameter algo is required"), Optional.empty());
        //    return resp.toString();
        //}

        if(!Arrays.asList(Utils.algos).contains(algo)) {
            resp = Utils.createResponse(500, String.format("[ERROR] Algorithm %s not support", algo), Optional.empty());
            return resp.toString();
        }

        try {
            JSONObject input = new JSONObject(req);
            pid = input.getString("pid").trim();

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
            if(!Utils.createFolder(String.format("%s/%s/%s/%s", Utils.root, uid, pid, algo))) {
                resp = Utils.createResponse(500, String.format("[ERROR] Cannot create %s dir for project %s", algo, pid), Optional.empty());
                return resp.toString();
            }

            switch(algo) {
                case "cpabe":
                    if(cpabeInitProcess(key, uid, pid, algo) == false) {
                        Utils.deleteFolder(String.format("%s/%s/%s/%s", Utils.root, uid, pid, algo));
                        resp = Utils.createResponse(500, String.format("[ERROR] CPABE init failed"), Optional.empty());
                        return resp.toString();
                    }
                    break;
            }

        } catch (JSONException e) {
            resp = Utils.createResponse(40, String.format("[ERROR] JSON parse error (%s)", e.toString()), Optional.empty());
            return resp.toString();
        }

        resp = Utils.createResponse(200, String.format("[INFO] Algorithm %s init success for project %s", algo, pid), Optional.empty());
        return resp.toString();
    }

    boolean cpabeInitProcess(String key, String uid, String pid, String algo) throws JSONException {
        try {
            cpabeJNI bee = new cpabeJNI();
            String pk = String.format("%s/%s/%s/%s/pk", Utils.root, uid, pid, algo) ;
            String mk = String.format("%s/%s/%s/%s/mk", Utils.root, uid, pid, algo) ;
            if(bee.setup(pk,mk) == -1)  //fail
                return false;
        } catch(Exception e) {
            return false;
        }
        return true;
    }
}