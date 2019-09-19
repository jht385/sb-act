package com.example.sb;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 流程定义 流程模板 pd=process definition<br>
 * 1、把流程图部署到activiti的引擎中 几种方式 classpath inputstream zipinputstream<br>
 * 2、对流程图进行删除 重点<br>
 * 3、获取到流程图和bpmn文件 重点<br>
 * 4、查询 了解 查询部署 查询流程定义<br>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class A2PDTest {
	@Autowired
	RepositoryService repositoryService;
	@Autowired
	RuntimeService runtimeService;
	@Autowired
	TaskService taskService;

	/**
	 * 部署流程涉及的表
	 * act_re_deployment 存储了部署的动作，记录一次流程部署
	 * act_ge_bytearray 存储bpmn文件和png图片二进制表，记录流程部署相关的资源文件
		 * 	name_:存储该文件的路径名称 
		 *  deploymentid_id_:部署表的ID
		 *  byte_:存放值(bpmn和png)
	 * act_re_procdef 流程定义表，即 流程模板
		 *  id: 由${name}:${version}:随机数 确定唯一的流程(！重点 deploymentID)
		 * 	name_: 流程定义名称
		 * 	key_: 流程定义名称
		 *  version_: 某一个流程定义的版本
		 *  deployment_id_:部署表的ID
	 * 说明：
	 * 1、根据deploymentID-->查询图片和bpmn文件
	 * 2、根据deploymentID-->查询流程定义
	 * 3、只要流程名称不变，部署一次，版本号加1，pdid就发生变化，生成了一个新的deploymentID
	 * 4、所以deploymentID和pdid是一一对应的关系
	 */
	@Test
	public void testDeployFromClasspath() {
		repositoryService//
				.createDeployment()//
				.addClasspathResource("processes/qingjia.bpmn")//
				.addClasspathResource("processes/qingjia.png")//
				.deploy();
	}

	/**
	 * 通过inputStream的方式部署
	 */
	@Test
	public void testDeployFromInputStream() {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("processes/qingjia.bpmn");
		repositoryService//
				.createDeployment()//
				.addInputStream("qingjia.bpmn", inputStream)//
				.deploy();
	}

	/**
	 * zip方式，这个zip就是把qingjia.bpmn，qingjia.png打包的zip
	 */
	@Test
	public void testDeployFromZip() {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("processes/qingjia.zip");
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		repositoryService//
				.createDeployment()//
				.addZipInputStream(zipInputStream)//
				.deploy();
	}

	/**
	 * 查询流程模板，导出文件
	 */
	@Test
	public void testShowImage() throws Exception {
		InputStream inputStream = repositoryService.getProcessDiagram("qingjia:1:4"); // 根据流程模板id拿流程模板图片
		OutputStream outputStream = new FileOutputStream("e:/processimg0.png");
		for (int b = -1; (b = inputStream.read()) != -1;) {
			outputStream.write(b);
		}
		inputStream.close();
		outputStream.close();

		inputStream = repositoryService.getResourceAsStream("1", "qingjia.png"); // 得到流程部署id那对应资源
		outputStream = new FileOutputStream("e:/processimg.png");
		for (int b = -1; (b = inputStream.read()) != -1;) {
			outputStream.write(b);
		}
		inputStream.close();
		outputStream.close();

		inputStream = repositoryService.getProcessModel("qingjia:1:4"); // 根据流程模板id拿流程模板文件
		outputStream = new FileOutputStream("e:/processimg.bpmn");
		for (int b = -1; (b = inputStream.read()) != -1;) {
			outputStream.write(b);
		}
		inputStream.close();
		outputStream.close();
	}

	/**
	 * 查询流程部署表
	 */
	@Test
	public void testQueryDeploy() {
		List<Deployment> deployments = repositoryService.createDeploymentQuery()//
				.list();
		for (Deployment deployment : deployments) {
			System.out.println(deployment.getId() + "-" + deployment.getDeploymentTime());
		}
	}

	/**
	 * 查询流程部署表，带条件
	 */
	@Test
	public void testQueryDeployById() {
		Deployment deployment = repositoryService//
				.createDeploymentQuery()//
				.deploymentId("1")//
				.singleResult();
		System.out.println(deployment.getId() + "-" + deployment.getDeploymentTime());
	}

	/**
	 * 查询流程模板表
	 */
	@Test
	public void testQueryPD() {
		List<ProcessDefinition> processDefinitions = repositoryService//
				.createProcessDefinitionQuery()//
				.list();
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println(processDefinition.getKey());
			System.out.println(processDefinition.getId());
			System.out.println(processDefinition.getVersion());
		}
	}

	/**
	 * 查询流程模板表，排序条件
	 */
	@Test
	public void testQueryPDByVersion() {
		List<ProcessDefinition> processDefinitions = repositoryService//
				.createProcessDefinitionQuery()// 查的act_re_procdef表，数据多
				.orderByProcessDefinitionVersion()// 添加排序条件
				.desc()// 倒序
				.list();
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println(processDefinition.getKey());
			System.out.println(processDefinition.getId());
			System.out.println(processDefinition.getVersion());
		}
	}

	/**
	 * 删除流程部署
	 */
	@Test
	public void testDelete() {
		// repositoryService.deleteDeployment("1");//删除流程，默认不级联删除流程实例
		repositoryService.deleteDeployment("1", true);// 删除流程，级联删除流程实例
	}
}
