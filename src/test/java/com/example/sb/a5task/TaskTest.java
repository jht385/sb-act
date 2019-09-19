package com.example.sb.a5task;

/**
 * 1、任务的执行者在一般情况下，应该是一个变量
 *     1、在进入该节点之前就能确定任务的执行者
 *     		<userTask id="申请请假" name="申请请假" activiti:assignee="#{applicator}"></userTask>
 *        那么 applicator这个变量需要通过流程变量的形式赋值，而且必须在该任务节点之前
 *     2、在进入该节点之后，才要确定节点的执行者是谁
 *        1、可以通过MyTaskListener的方式给任务赋值执行人
	 *          <userTask id="部门经理审批" name="部门经理审批">
			      <extensionElements>
			        <activiti:taskListener event="create" class="com.itheima10.activiti.task.MyTaskListener"></activiti:taskListener>
			      </extensionElements>
			    </userTask>
			           当进入上述的userTask节点之后，立刻执行MyTaskListener的notify方法
			      delegateTask.setAssignee(manager);就是给MyTaskListener所在的任务节点赋值任务的执行人
		      	说明：该类是由activiti内部调用的，不是由spring容器产生的,所以在notify方法中没有办法使用spring的声明式事务处理产生事务
		  2、可以通过代码的方式设置任务的执行人
		  		processEngine.getTaskService()
					.setAssignee(taskId, userId);
					通过该方式也可以给正在执行的任务赋值任务的执行人
 *     3、在进入该节点之后，有一堆人有执行该任务的权限，只要有一个人执行完毕以后，该任务就结束了，这样的任务为组任务
 *     		processEngine.getTaskService()
					.addCandidateUser(taskId, userId); //把一个用户添加到该任务的候选人中
			processEngine.getTaskService()
					.addCandidateGroup(taskId, groupId);//把一个组赋值给一个任务的候选人
 *     4、可以把一个任务分配给一个用户组,用户组的每一个成员都有权限执行该任务，但是只要有一个成员执行了该任务，该任务就结束了
 *     5、泳道(jbpm4的内容)
 * 2、任务的执行者固定在流程图中了
 */
public class TaskTest {

}
