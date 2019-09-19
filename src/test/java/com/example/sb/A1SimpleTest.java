package com.example.sb;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class A1SimpleTest {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;

	/**
	 * 部署流程图，产生实例模板<br>
	 * act_re_deployment 部署主表
	 * act_ge_bytearray 部署的文件，bpmn文件和png图片
	 * act_re_procdef 流程定义表，记录 当前执行到哪
	 */
	@Test
	public void deploy() {
		/**
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		processEngine.getRepositoryService() // 仓库服务
				.createDeployment() // 创建部署对象
				.addClasspathResource("qingjia.bpmn") // 其中一种部署方式，添加.bpmn
				.addClasspathResource("qingjia.png") // .png
				.deploy(); // 部署
		*/
		repositoryService.createDeployment().addClasspathResource("processes/qingjia.bpmn") // 其中一种部署方式，添加.bpmn
				.addClasspathResource("processes/qingjia.png") // .png
				.deploy(); // 部署
	}

	/**
	 * 启动流程实例 pi=process instance，启动实例后，生成任务
	 */
	@Test
	public void startPI() {
		/**
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		processEngine.getRuntimeService() // 得到流程服务
				.startProcessInstanceById("qingjia:1:4"); // 启动对应id的流程实例，act_re_procdef
		*/
		runtimeService.startProcessInstanceById("qingjia:1:4");
	}

	/**
	 * 完成任务，一个流程包含多个任务，一个任务就是一个节点
	 * 运行中的流程表:act_ru_*
	 * 流程结束后表:act_hi_*
	 */
	@Test
	public void finishTask() {
		/**
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		processEngine.getTaskService() // 得到任务服务
				.complete("2504"); // act_ru_task，完成对应任务id的任务，完成后，任务id会变化，再对应id完成即可一路执行下去
		*/
		taskService.complete("7502");
	}

	/**
	 * 根据张三查询任务
	 */
	@Test
	public void queryTask() {
		/**
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		List<Task> tasks = processEngine.getTaskService() // 得到任务服务
				.createTaskQuery() // 创建默认的任务查询
				.taskAssignee("张三") // 设置条件，Assignee等于"张三"，例子流程设置了Assignee，所以流程到这里是"张三"
				.list(); // 查询所有符合的
		for (Task task : tasks) {
			System.out.println(task.getName());
		}
		*/
		List<Task> tasks = taskService.createTaskQuery() // 创建默认的任务查询
				.taskAssignee("张三") // 设置条件，Assignee等于"张三"，例子流程设置了Assignee，所以流程到这里是"张三"
				.list(); // 查询所有符合的
		for (Task task : tasks) {
			System.out.println(task.getName());
		}
	}
}
