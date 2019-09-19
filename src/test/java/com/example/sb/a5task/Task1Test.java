package com.example.sb.a5task;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ！通过文本方式打开 .bpmn 可以看到如下
 * <userTask id="申请请假" name="申请请假" activiti:assignee="#{applicator}"></userTask>
 * 如果一个任务节点的执行人是通过上面的形式赋值的，那么在进入该节点之前，必须给变量applicator赋值(通过流程变量)
 */
/**
 * 单向流程，.bpmn设置变量key，流程每步对key赋值确定执行人
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Task1Test {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;

	/**
	 * 部署带指定 执行人 的流程模板
	 */
	@Test
	public void testDeploy() {
		repositoryService.createDeployment()//
				.addClasspathResource("processes/task/task1.bpmn")//
				.deploy();
	}

	/**
	 * 在启动流程实例的时候，设置流程变量，给aplicator赋值
	 */
	@Test
	public void testStartPI() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("applicator", "张三");// key对应流程模板里写的#{applicator}
		runtimeService.startProcessInstanceById("myProcess:1:4", variables);// .bpmn设置了执行人，不在流程进入前设置流程变量会报错
	}

	/**
	 * 在完成请假申请任务的时候，设置流程变量
	 */
	@Test
	public void testFinishApplicatorTask() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("manager", "李四");
		taskService.complete("2506", variables);// .bpmn设置了执行人，不在流程进入前设置流程变量会报错
	}

	/**
	 * 在完成部门经理审批的任务的时候，设置流程变量，给总经理任务的执行人赋值
	 */
	@Test
	public void testFinishManagerTask() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("boss", "王五");
		taskService.complete("5003", variables);// .bpmn设置了执行人，不在流程进入前设置流程变量会报错
	}

	/**
	 * 完成总经理的任务
	 */
	@Test
	public void testFinishBossTask() {
		taskService.complete("7503");// 整个流程实例完成了，最后就不需要变量了
	}
}
