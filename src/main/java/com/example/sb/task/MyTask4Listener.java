package com.example.sb.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTask4Listener implements TaskListener {
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask delegateTask) {
		/**
		 *  组任务的候选人应该来自于数据库的某一张表
		 *  ！如果这有很多人，则需要设置多次，一般采用下面指定组的方式
		 */
		delegateTask.addCandidateUser("u1");

		/**
		 * 直接把一个组赋值给一个任务的候选人
		 * ！IdentityTest 添加组，人，组人关系
		 * ！这种方式act_hi_identitylink,act_ru_identitylink表只会存一个groupid
		 */
		delegateTask.addCandidateGroup("g1");// 参数为groupid
		
		// 实际应该是找到某个角色或部门的人，即先用identityService设置了group和部门与角色对应关系后，这里按部门角色id查询出数据addCandidate
		/**
		 * 如果需要spring容器对象，可以如下调用，直接注入不不管用
		 */
//		IdentityService identityService = ApplicationContextRegister.getBean(IdentityService.class);
//		Group group = identityService.createGroupQuery().groupId("g1").list().get(0);
//		delegateTask.addCandidateGroup(group.getId());
	}
}
