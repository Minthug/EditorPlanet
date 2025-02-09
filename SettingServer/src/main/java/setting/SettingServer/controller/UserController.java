package setting.SettingServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/")
    public String helloNotice() {
       return "login_form";
    }

    @GetMapping("/success")
    public String successLogin() {
        return "success_form";
    }
}
