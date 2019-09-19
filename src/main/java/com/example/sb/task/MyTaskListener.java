package com.example.sb.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener implements TaskListener {
	private static final long serialVersionUID = 1830618442071409874L;

	@Override
	public void notify(DelegateTask delegateTask) {
		/**
		 * 如果需要spring容器对象，可以如下调用，直接注入不不管用
		 */
		//TaskService taskService = ApplicationContextRegister.getBean(TaskService.class);

		String manager = delegateTask.getVariable("manager").toString();
		delegateTask.setAssignee(manager);
	}
}
