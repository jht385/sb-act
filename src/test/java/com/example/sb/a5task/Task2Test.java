package com.example.sb.a5task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 通过任务监听器，监听进入任务动作，然后在监听器中实现对执行人的赋值
 * <userTask id="部门经理审批" name="部门经理审批">
      <extensionElements>
        <activiti:taskListener event="create" class="task.MyTaskListener"></activiti:taskListener>
      </extensionElements>
    </userTask>
    ！MyTaskListener是由activiti内部产生的，他不在spring容器中，如果要拿spring容器中对象，需要重新手动获取spring容器才能取得想要的Service
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class Task2Test {
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
				.addClasspathResource("processes/task/task2.bpmn")//
				.deploy();
	}

	/**
	 * 启动流程实例，并且给流程变量 applicator,manager赋值
	 */
	@Test
	public void testStartPI() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("applicator", "张三"); // 下个节点#{applicator}获取吊
		variables.put("manager", "李四");// 为节点二的监听器提供变量
		runtimeService.startProcessInstanceById("task2:1:4", variables);
	}

	/**
	 * 根据张三查询任务
	 */
	@Test
	public void testQueryTaskByAssignee() {
		List<Task> tasks = taskService//
				.createTaskQuery()//
				.taskAssignee("张三")// 查找执行人是"张三"的数据
				.orderByTaskCreateTime()//
				.desc()//
				.list();
		for (Task task : tasks) {
			System.out.println(task.getAssignee());
			System.out.println(task.getName());
		}
	}

	/**
	 * 任务二在MyTaskListener设置了执行人
	 * 		也可以不用TaskListener，如下：指定执行人
	 * 		processEngine.getTaskService().setAssignee(taskId, userId);
	 * 任务三在.bpmn里已经设置了执行人
	 */
	@Test
	public void testFinishTask() {
		taskService.complete("7502");
	}
}
