package com.example.sb;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.HistoryService;
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
 * 流程变量
 * 在执行流程的过程中产生数据称为流程变量
 * 1、流程变量的生命周期
 * 	 就是流程实例
 * 2、流程变量和流程实例的关系
 * 流程变量必须和流程实例绑定在一起
 * 3、通过什么样的方法把一个流程变量存放在流程实例中
 * 4、通过什么样的方法把一个流程变量从流程实例中提取出来
 * 5、把一个对象放入到流程变量中，该对象必须实现对象的序列化
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class A4ValueTest {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;
	@Autowired
	HistoryService historyService;

	/**
	 * 涉及到的表
	 * act_hi_varinst：流程变量历史记录表（记录所有流程的变量）
	 * act_ru_variable:流程变量表（记录当前流程生命周期未结束时所有节点的变量），任务完成后清空
	 * act_ge_bytearray：当变量是复制类型时，会在该表创建记录用于关联
	 */
	@Test
	public void testStartPI() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("当启动流程实例的时候", "启动流程实例的流程变量");
		runtimeService.startProcessInstanceByKey("qingjia", variables); // 当启动流程实例的时候添加流程变量
	}

	/**
	 * 在完成任务的时候，设置流程变量
	 * 	！同key覆盖应该是
	 */
	@Test
	public void testFinishTask() {
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("请假天数", 3);
		taskService.complete("7506", variables); // 完成当前任务时提及变量
	}

	/**
	 * 在流程实例的任何时候(只要流程实例没有结束),设置流程变量
	 */
	@Test
	public void testSetVariablesWhenPI() {
		/**
		 * executionId 流程实例id
		 * variableName 变量的名称
		 * value 变量的值
		 */
		runtimeService.setVariable("7501", "aaa", "aaa");
	}
	
	/**
	 * 在流程实例的任何时候(只要流程实例没有结束),设置流程变量
	 * ！这里采用的是把业务对象直接写到表里
	 * ！类需要实现Serializable接口
	 */
	@Test
	public void testSetVariables_Object_WhenPI() {
		Person person = new Person();
		person.setPid(1L);
		person.setName("王二麻子");
		/**
		 * executionId 针对哪一个流程实例设置流程变量
		 * variableName 变量的名称
		 * value 变量的值
		 */
		runtimeService.setVariable("7501", "person", person);
		// ！这里当时想直接toString所有，就在保存到activiti库后，后重写了Person类中的toString，结果导出转换说不是一个类，所以要特别注意
		Person p = (Person) runtimeService.getVariable("7501", "person");
		if (p != null) {
			System.out.println(p.toString());
		}
	}

	/**
	 * 获取指定的流程实例下面的所有的流程变量
	 */
	@Test
	public void testGetVariables() {
		Map<String, Object> variables = runtimeService.getVariablesLocal("7501");
		for (Entry<String, Object> entry : variables.entrySet()) {
			if (entry.getValue() instanceof Person) {
				Person person = (Person) entry.getValue();
				System.out.println(person.toString());
			} else {
				System.out.println(entry.getKey() + "-" + entry.getValue());
			}
		}
	}
}
