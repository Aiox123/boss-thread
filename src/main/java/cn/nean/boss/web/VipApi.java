package cn.nean.boss.web;

import cn.nean.boss.service.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vip")
public class VipApi {


    @Autowired
    ThreadService threadService;

    /*
    *  开通Vip服务
    * */
    @GetMapping("/open/{userId}")
    public String openVip(@PathVariable Long userId){
        String email = "";
        threadService.sendEmail(email);
        return "success " + userId;
    }
}
