package main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController {

    @RequestMapping("/admin")
    public String index() {
        return "index";
    }

    @RequestMapping("/statistics")
    public String statistics() {
        return "";
    }
}
