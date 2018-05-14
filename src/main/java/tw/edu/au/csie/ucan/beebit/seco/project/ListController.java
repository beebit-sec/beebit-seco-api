package tw.edu.au.csie.ucan.beebit.seco.project;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.edu.au.csie.ucan.beebit.seco.Project;
import tw.edu.au.csie.ucan.beebit.seco.User;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/project")
public class ListController {

    @RequestMapping(value = "/list", method={RequestMethod.POST})
    public String projectCreate(
            @RequestParam(value="req",defaultValue="") String req,
            @RequestParam(value="key",defaultValue="") String key) throws JSONException {

        JSONObject resp;
        String uid;
        List<Project> projects;

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, String.format("[ERROR] Parameter req is required"), Optional.empty());
            return resp.toString();
        }

        try {
            JSONObject input = new JSONObject(req);
            uid = input.getString("uid").trim();

            JdbcTemplate db = Utils.connectDb();
            List<User> users = db.query(
                    String.format("select * from users where uid ='%s'", uid),
                    (rs, rowNum) -> new User(rs.getString("uid"))
            );

            if (users.isEmpty()) {
                resp = Utils.createResponse(500, String.format("[ERROR] User not exist"), Optional.empty());
                return resp.toString();
            }

            projects = db.query(
                    String.format("select * from project where uid ='%s'", uid),
                    (rs, rowNum) -> new Project(rs.getString("uid"), rs.getString("pid"))
            );

            if (projects.isEmpty()) {
                resp = Utils.createResponse(200, String.format("[INFO] No project created"), Optional.empty());
                return resp.toString();
            }

        } catch (JSONException e) {
            resp = Utils.createResponse(40, String.format("[ERROR] JSON parse error (%s)", e.toString()), Optional.empty());
            return resp.toString();
        }

        JSONArray pa = new JSONArray();
        for(Project p : projects) {
            pa.put(new JSONObject().put("name",p.getPid().trim()));
        }

        resp = Utils.createJsonResponse(200, String.format("[INFO] Projects listed"), pa);
        return resp.toString();
    }
}