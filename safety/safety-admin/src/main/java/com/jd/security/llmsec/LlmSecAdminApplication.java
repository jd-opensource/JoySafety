package com.jd.security.llmsec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy(proxyTargetClass=true)
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class LlmSecAdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(LlmSecAdminApplication.class, args);
	}
}
