package com.example.sb.a5task;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单向流程，.bpmn指定监听器(MyTask4Listener)，监听器动态设置多个候选人，谁先认领即为执行人，就可以往下走流程
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Task4Test {
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
				.addClasspathResource("processes/task/task4.bpmn")//
				.deploy();
	}
	
	
	@Test // task4开启任务后调用监听器MyTask4Listener设置actUser，需要先为act准备好user和group，以及其对应关系
	public void testIdentity() {
		/**
		 * 对应act_id_group, act_id_info, act_id_membership, act_id_user
		 */
		// act添加了u1,u2-g1
//		Group group = new GroupEntity();//5
		Group group = identityService.newGroup("g1");//6，这里看需求，如根据角色或部门，则这里就可以设为其对应id
		group.setName("咨询组");
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		processEngine.getIdentityService()//
				.saveGroup(group);// act_id_group
//		User user1 = new UserEntity();//5
		User user1 = identityService.newUser("u1");//6，这里设用户id即可
		User user2 = identityService.newUser("u2");
		identityService.saveUser(user1);// act_id_user
		identityService.saveUser(user2);
		//identityService.createMembership(user1.getId(), group.getId());// act_id_membership
		identityService.createMembership(user2.getId(), group.getId());
	}

	@Test
	public void testDeleteGroup() {
		// 非级联删除有点坑
		identityService.deleteUser("u1");
		identityService.deleteUser("u2");
		identityService.deleteGroup("g1");
	}

	@Test // task4开启任务后调用监听器MyTask4Listener设置actUser
	public void testStartPI() {
		runtimeService.startProcessInstanceById("task4:1:4");
	}
	
	/**
	 *  根据候选人查询组任务，通过MyTask4Listener动态设置后
	 * 通过u1和g1对应的人都能找到任务
	 */
	@Test
	public void testQueryTaskByCandidate() {
		List<Task> tasks = taskService//
				.createTaskQuery()//
//				.taskCandidateUser("u1")// 候选人之一
//				.taskCandidateUser("u2")// 属于g1
				.taskCandidateGroup("g1")
				.list();
		for (Task task : tasks) {
			System.out.println(task.toString());
		}
	}

	/**
	 * 根据组任务查看任务的候选人
	 */
	@Test
	public void testQueryCandidateByTask() {
		List<IdentityLink> identityLinks = taskService//
				.getIdentityLinksForTask("7505"); // act_hi_identitylink,act_ru_identitylink都有
		for (IdentityLink identityLink : identityLinks) {
			System.out.println(identityLink.toString());
		}
	}

	/**
	 * 候选人认领任务
	 */
	@Test
	public void testClaimTask() {
		taskService.claim("7505", "u2");// 任一候选人认领任务，此时执行人就设置为第一个认领的候选人
	}

	/**
	 * 先认领的候选人变成执行人，然后继续走流程
	 */
	@Test
	public void testFinishTask() {
		taskService.complete("7505");
	}
}