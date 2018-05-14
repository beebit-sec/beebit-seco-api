package tw.edu.au.csie.ucan.beebit.seco;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RootController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/{page}")
    public String pages(@PathVariable String page) {
            System.out.print(page);
            return page;
    }
}
