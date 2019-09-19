package com.example.sb;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 流程实例 CRUD操作 pi=process instance
 * 1、启动流程实例
 * 	         根据pdid启动流程实例
 *     根据pdkey启动流程实例   默认的启动的是最高版本
 * 2、完成任务
 * 3、任务的查询
	 * 	1、根据任务的执行人查询任务
	 * 	2、可以根据任务查询任务的执行人
	 * 	3、查看历史任务
 * 4、怎么样查看流程实例是否结束
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class A3PITest {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;

	/**
	 * 启动流程实例涉及到的5张表 
	 * act_hi_actinst，节点表(记录流程实例通过的所有节点)
		 *  end_time_: 如果有值，说明该节点已经结束了
	 * act_hi_procinst，流程实例 起始 终止 记录表(记录起点 和 结束点)
		 * 	end_time_:如果该字段有值，说明这个流程实例已经结束了
		 *  end_act_id_:说明该流程实例是在哪个节点结束的
	 * act_hi_taskinst，流程实例历史任务表(记录流程实例通过的所有节点，除了起点 和 结束点)
		 *  end_time_:如果该字段有值，说明任务已经完成了
		 *  delete_reason:如果该值为completed,说明该任务处于完成的状态
	 * act_ru_execution 流程实例表(流程实例本身，流程进行时不停更新该表，当流程完成，该表清空)
	 * act_ru_task 任务表(总是记录流程当前执行到的最新节点，执行任务看他，当流程完成，该表清空)
	 */
	@Test
	public void testStartPI() {
		ProcessInstance pi = runtimeService.startProcessInstanceById("qingjia:1:4");// act_re_procdef，根据流程模板id启动流程
		System.out.println(pi.getId());
	}

	/**
	 * 查询所有的正在执行的流程实例
	 */
	@Test
	public void testQueryPI() {
		List<ProcessInstance> processInstances = runtimeService//
				.createProcessInstanceQuery()// act_ru_execution
				.list();
		for (ProcessInstance processInstance : processInstances) {
			System.out.println(processInstance.getActivityId());
			System.out.println(processInstance.getId());
		}
	}

	/**
	 * 查询当前正在执行的节点
	 */
	@Test
	public void testQueryActivity() {
		List<String> strings = runtimeService//
				.getActiveActivityIds("2501");
		for (String string : strings) {
			System.out.println(string);
		}
	}

	/**
	 * 获取当前的流程实例正在运行的节点的坐标
	 */
	@Test
	public void getPix() {
		// 5的方式
//		List<String> strings = runtimeService.getActiveActivityIds("2501");
//		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService
//				.getProcessDefinition("qingjia:1:4"); // processDefinitionEntity代表流程图对象的实体
//		for (String string : strings) {
//			// ActivityImpl代表流程图上的每一个节点
//			ActivityImpl activityImpl = processDefinitionEntity.findActivity(string);
//			// 获取到正在执行的流程节点的坐标
//			System.out.println(activityImpl.getHeight());
//			System.out.println(activityImpl.getWidth());
//			System.out.println(activityImpl.getX());
//			System.out.println(activityImpl.getY());
//		}
		// 6的方式
		// 线信息
		BpmnModel bpmnModel = repositoryService.getBpmnModel("qingjia:1:4");
		Map<String, List<GraphicInfo>> map = bpmnModel.getFlowLocationMap();
		for (String key : map.keySet()) {
			System.out.println(key); // 流程图xml中sequenceFlow的id
			List<GraphicInfo> gs = map.get(key); // 只所以
			for (GraphicInfo g : gs) {
				System.out.println("graphicInfo.getX() = " + g.getX());
				System.out.println("graphicInfo.getY() = " + g.getY());
				System.out.println("graphicInfo.getHeight() = " + g.getHeight());
				System.out.println("graphicInfo.getWidth() = " + g.getWidth());
			}
		}
		// 当前运行到的节点信息
		Task task = taskService.createTaskQuery().taskId("2505").singleResult();
		bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
		FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
		GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowElement.getId());
		System.out.println("graphicInfo.getX() = " + graphicInfo.getX());
		System.out.println("graphicInfo.getY() = " + graphicInfo.getY());
		System.out.println("graphicInfo.getHeight() = " + graphicInfo.getHeight());
		System.out.println("graphicInfo.getWidth() = " + graphicInfo.getWidth());
	}

	/**
	 * 查询所有的任务
	 */
	@Test
	public void testQueryAllTask() {
		List<Task> tasks = taskService//
				.createTaskQuery()// act_ru_task
				.list();
		for (Task task : tasks) {
			System.out.println(task.getId());
			System.out.println(task.getName());
		}
	}

	/**
	 * 完成任务 需要一个参数：taskId
	 */
	@Test
	public void testFinishTask() {
		taskService.complete("2505");
	}

	/**
	 * 根据任务的执行人查看任务
	 */
	@Test
	public void testQueryTaskByAssignee() {
		List<Task> tasks = taskService//
				.createTaskQuery()// act_ru_task
				.taskAssignee("张三")//
				.list();
		for (Task task : tasks) {
			System.out.println(task.getId());
			System.out.println(task.getName());
		}
	}

	/**
	 * 根据任务的执行人查看任务，并且按照时间的倒叙排序
	 */
	@Test
	public void testQueryTaskByAssigneeByTime_DESC() {
		List<Task> tasks = taskService//
				.createTaskQuery()//
				.taskAssignee("张三")//
				.orderByTaskCreateTime()// 根据完成任务时间
				.desc()// 倒序
				.list();
		for (Task task : tasks) {
			System.out.println(task.getId());
			System.out.println(task.getName());
		}
	}

	/**
	 * 根据piid判断流程实例是否结束
	 */
	@Test
	public void testQueryPIByPIID() {
		ProcessInstance pi = runtimeService//
				.createProcessInstanceQuery()//
				.processInstanceId("2501")//
				.singleResult();
		if (pi == null) {
			System.out.println("该流程实例已经结束了");
		} else {
			System.out.println("该流程实例正在执行中");
		}
	}

	/**
	 * 查询已经完成的任务
	 */
	@Test
	public void testQueryHistoryTask() {
		List<HistoricTaskInstance> historicTaskInstances = historyService//
				.createHistoricTaskInstanceQuery()//
				.finished()// 已经完成的
				.list();
		for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
			System.out.println(historicTaskInstance.getAssignee());
			System.out.println(historicTaskInstance.getName());
			System.out.println(historicTaskInstance.getId());
		}
	}

	/**
	 * 查询已经完成的activityimpl(节点实体)
	 */
	@Test
	public void testQueryHistoryActivityImpl() {
		List<HistoricActivityInstance> historicActivityInstances = historyService//
				.createHistoricActivityInstanceQuery()// act_hi_actinst
				.list();
		for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
			System.out.println(historicActivityInstance.getActivityName());
		}
	}
}
