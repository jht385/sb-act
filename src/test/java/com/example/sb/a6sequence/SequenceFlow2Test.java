package com.example.sb.a6sequence;

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

import com.example.sb.bean.Person;

/**
 * 分支路线，条件动态设置
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SequenceFlow2Test {
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
		repositoryService//
				.createDeployment()//
				.addClasspathResource("processes/sequence/sequenceflow2.bpmn")//
				.deploy();
	}

	@Test
	public void testStartPi() {
		runtimeService.startProcessInstanceById("sequence2:1:4");
	}

	@Test // 这个一个根据day和person.day决定路线的流程
	public void testFinishTask() {
		Map<String, Object> variables = new HashMap<String, Object>();
		/**
		 *  ！两个值都可以指定
		 *  ${day>=person.day},ut1->ut2
		 *  ${day<person.day},ut1->end
		 */
		variables.put("day", 2);// 条件day
		Person person = new Person();
		person.setDay(1);
		variables.put("person", person);// 条件person.day
		taskService.complete("10006", variables);
	}
}
