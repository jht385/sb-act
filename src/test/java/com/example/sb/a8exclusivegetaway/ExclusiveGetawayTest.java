package com.example.sb.a8exclusivegetaway;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * exclusivegetaway 用于判断走哪个分路
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExclusiveGetawayTest {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;
	@Autowired
	IdentityService identityService;

	@Test
	public void testDeploy() {
		repositoryService.createDeployment()//
				.addClasspathResource("processes/exclusivegetaway/exclusivegetaway.bpmn")//
				.deploy();
	}

	@Test
	public void testStartPI() {
		runtimeService.startProcessInstanceById("exclusivegetaway:1:4");
	}

	@Test
	public void testFinishTask() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("day", 2); // exclusivegetaway满足分路条件则走分路，不满足走设置的默认分路
		taskService.complete("17503", map);
	}
}
