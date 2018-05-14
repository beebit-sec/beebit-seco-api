package tw.edu.au.csie.ucan.beebit.seco.user;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tw.edu.au.csie.ucan.beebit.seco.User;
import tw.edu.au.csie.ucan.beebit.seco.Utils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class RegisterController {

    @RequestMapping(value = "/register", method={RequestMethod.POST})
    public String register(
            @RequestParam(value="req",defaultValue="") String req, 
            @RequestParam(value="key",defaultValue="") String key) throws JSONException {

        JSONObject resp;// = new JSONObject();

        if(req.isEmpty()) {
            resp = Utils.createResponse(500, "[ERROR] Parameter req is required", Optional.empty());
            return resp.toString();
        }

        try {
            JSONObject input = new JSONObject(req);
            String uid = input.getString("uid").trim();

            JdbcTemplate db = Utils.connectDb();
            List<User> users = db.query(
                    "select * from users where uid ='" + uid + "'", 
                    (rs, rowNum) -> new User(rs.getString("uid"))
            );

            if (false == users.isEmpty()) {
                resp = Utils.createResponse(500, "[ERROR] User exist", Optional.empty());
                return resp.toString();
            }

            try {
                db.execute(String.format("insert into users (uid) values ('%s')", uid));
            } catch(Exception e) {
                resp = Utils.createResponse(500, String.format("[ERROR] SQL error (%s)", e.toString()), Optional.empty());
                return resp.toString();

            }

            if(false == Utils.createFolder(String.format("%s/%s", Utils.root, uid))) {
                db.execute(String.format("delete from users where uid='%s'", uid));
                resp = Utils.createResponse(500, String.format("[ERROR] Cannot create user directory"), Optional.empty());
                return resp.toString();

            }
        } catch (JSONException e) {
            resp = Utils.createResponse(40, String.format("[ERROR] JSON parse errot (%s)", e.toString()), Optional.empty());
            return resp.toString();
        }

        resp = Utils.createResponse(200, "[INFO] User created", Optional.empty());
        return resp.toString();
    }
}