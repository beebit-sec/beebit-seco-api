package tw.edu.au.csie.ucan.beebit.seco.project;

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
public class CreateController {

    @RequestMapping(value = "/create", method={RequestMethod.POST})
    public String projectCreate(
            @RequestParam(value="req",defaultValue="") String req,
            @RequestParam(value="key",defaultValue="") String key) throws JSONException {

        JSONObject resp;
        String uid;
        String pid;

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, String.format("[ERROR] Parameter req is required"), Optional.empty());
            return resp.toString();
        }

        try {
            JSONObject input = new JSONObject(req);
            pid = input.getString("pid").trim();
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

            List<Project> projects = db.query(
                    String.format("select * from project where pid ='%s'", pid),
                    (rs, rowNum) -> new Project(rs.getString("uid"), rs.getString("pid"))
            );

            if (!projects.isEmpty()) {
                resp = Utils.createResponse(500, String.format("[ERROR] Project exist"), Optional.empty());
                return resp.toString();
            }

            try {
                db.execute(String.format("insert into project (uid, pid) values ('%s','%s')", uid,  pid));
            } catch(Exception e) {
                resp = Utils.createResponse(500, String.format("[ERROR] SQL error (%s)", e.toString()), Optional.empty());
                return resp.toString();
            }

            if(!Utils.createFolder(String.format("%s/%s/%s", Utils.root, uid, pid))) {
                db.execute(String.format("delete from project where pid='%s'", uid));
                resp = Utils.createResponse(500, String.format("[ERROR] Cannot create project dir"), Optional.empty());
                return resp.toString();
            }

        } catch (JSONException e) {
            resp = Utils.createResponse(40, String.format("[ERROR] JSON parse error (%s)", e.toString()), Optional.empty());
            return resp.toString();
        }

        resp = Utils.createResponse(200, String.format("[INFO] Project (%s) created", pid), Optional.empty());
        return resp.toString();
    }
}