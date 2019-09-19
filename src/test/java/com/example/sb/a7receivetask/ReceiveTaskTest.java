package com.example.sb.a7receivetask;

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
 * receiveTask不需要人工干预，不需要审批，直接把当前的事情做完以后，流向下一个节点即可
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReceiveTaskTest {
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
				.addClasspathResource("processes/receivetask/receivetask.bpmn")//
				.deploy();
	}

	@Test
	public void testStartPI() {
		// <receiveTask类型的节点开启的实例不会进act_ru_task
		runtimeService.startProcessInstanceById("receive:1:4");
	}

	/**
	 * <receiveTask>可以直接发一个信号，就往下一节点前进
	 * ！如果下一节点是分路，则会在act_ru_execution表新创建两条记录，parentId为上一节点id
	 */
	@Test
	public void testNextNode() {
		/**
		 * ！因为不在act_ru_task存，无法complete任务
		 * 给当前的流程实例发一个信号：往下一个节点走
		 */
		//act_ru_task不存在，从act_ru_execution里找最新的
//		runtimeService.signal("1401");//5
		runtimeService.trigger("2502");// 6
	}
}
