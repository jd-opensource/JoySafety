package com.jd.security.llmsec.contoller;

import com.jd.security.llmsec.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/basic")
public class HealthCheckController {
    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService; // 保证接口调用时配置已经正常加载了

    @GetMapping("/healthcheck")
    public Object healthcheck() {
        return "ok";
    }
}
