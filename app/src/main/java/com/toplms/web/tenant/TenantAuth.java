package com.toplms.web.tenant;

import com.toplms.master.tenant.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TenantAuth {
    @Autowired
    private TenantService tenantService;

//    @GetMapping("/login")
//    public String login() {
//        return "/login";
//    }
//    @GetMapping("/signup")
//    public String signup() {
//        return "/signup";
//    }
}
