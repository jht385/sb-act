package com.example.sb.a5task;

import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单向流程，.bpmn指定 activiti:candidateUsers 多个候选人，谁先认领即为执行人，就可以往下走流程
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Task3Test {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;

	@Test
	public void testDeploy() {
		repositoryService//
				.createDeployment()//
				.addClasspathResource("processes/task/task3.bpmn")//
				.deploy();
	}

	/**
	 * 当启动完流程实例以后，进入了电脑维修的任务 
	 * <userTask id="电脑维修" name="电脑维修"
	 * 	activiti:candidateUsers="工程师1,工程师2,工程师3">
	 * </userTask>
	 * act_hi_identitylink(历史 候选人表)
	 * act_ru_identitylink(正在执行任务 候选人表)
	 * 	两张表都会添加两条所有候选人记录
	 *  	 taskId:工程师1
	 *  	 piid:工程师1
	 *  当任务走完，act_ru_identitylink会删除
	 */
	@Test
	public void testStartPI() {
		runtimeService.startProcessInstanceById("task3:1:4");
	}

	/**
	 *  根据候选人查询组任务
	 */
	@Test
	public void testQueryTaskByCandidate() {
		List<Task> tasks = taskService//
				.createTaskQuery()//
				.taskCandidateUser("工程师1")// 候选人之一
				.list();
		for (Task task : tasks) {
			System.out.println(task.getName());
		}
	}

	/**
	 * 根据组任务查看任务的候选人
	 */
	@Test
	public void testQueryCandidateByTask() {
		List<IdentityLink> identityLinks = taskService//
				.getIdentityLinksForTask("2505"); // act_hi_identitylink,act_ru_identitylink都有
		for (IdentityLink identityLink : identityLinks) {
			System.out.println(identityLink.getUserId());
		}
	}

	/**
	 * 候选人认领任务
	 */
	@Test
	public void testClaimTask() {
		//taskService.setAssignee("2505", "工程师1"); // 这个是设置，可以重复设置
		taskService.claim("2505", "工程师1");// 第一个人认领后不能重复认领
	}

	/**
	 * 先认领的候选人变成执行人，然后继续走流程
	 */
	@Test
	public void testFinishTask() {
		/**
		 *  测试来看activiti:candidateUsers并没有做限定作用，即非指定的那些用户认领了任务后也能完成任务
		 *  所以可能需要手动查询出当前任务的候选人，让候选人可见任务，避免其他人员认领并处理任务
		 */
		taskService.complete("2505");
	}
}
