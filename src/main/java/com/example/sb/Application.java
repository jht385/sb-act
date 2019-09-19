package com.example.sb;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		SecurityAutoConfiguration.class })
public class Application { // 启动类
	// 初始化后备份一下数据库，方便后序还原看各个操作操作的是哪个表
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
}
