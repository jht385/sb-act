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

/**
 * 分支路线，条件写死
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SequenceFlowTest {
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
				.addClasspathResource("processes/sequence/sequenceflow.bpmn")//
				.deploy();
	}

	@Test // 开启任务后即申请请假
	public void testStartPi() {
		runtimeService.startProcessInstanceById("sequenceflow:1:4");
	}

	@Test // 完成请假申请的任务，即在请假单上填写请假时间
	public void testFinisTask() {
		Map<String, Object> variables = new HashMap<String, Object>();
		// day控制之后的流程路线，如day==1返回"申请请假"，1<day<=3，to"结束"，3<day，to"总经理审批"
		// 可以先testFinisTask后再testFinisTask2查看act_ru_task的NAMA_得知当前怎么走的
		variables.put("day", 4);
		taskService.complete("15005", variables);
	}

	/**
	 * 完成部门经理审批的任务
	 */
	@Test
	public void testFinisTask2() {
		taskService.complete("20002");
	}

	/**
	 * ！不启动实例的情况下，查询分路
	 * 获取部门经理审批节点的出处有几个，每一个的名称
	 */
	@Test
	public void testGetOutGoing() {
		//5，6没去查对应api，详见A3PITest
//		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//		/**
//		 * ProcessDefinitionEntity 流程定义对象
//		 */
//		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processEngine.getRepositoryService()
//				.getProcessDefinition("sequenceflow:1:604");
//		/**
//		 * 根据流程定义对象中的某一个节点的id的值，就可以得到该节点的ActitityImpl(节点对象)
//		 */
//		ActivityImpl activityImpl = processDefinitionEntity.findActivity("部门经理审批");
//		/**
//		 * 该节点对象有多少sequenceFlow
//		 */
//		List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
//		for (PvmTransition pvmTransition : pvmTransitions) {
//			System.out.println(pvmTransition.getId());// 输出sequenceFlow的id的值
//			System.out.println(pvmTransition.getDestination());// 输出sequenceFlow的目标节点
//		}
	}
}
