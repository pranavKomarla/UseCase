package com.teamflowlite.domain.policy;

import com.teamflowlite.domain.enums.TaskStatus;

public interface EmployeeFunctionality {

    boolean canCreateTask();

    boolean canAssignTask();

    boolean canMoveTaskStatus(TaskStatus from, TaskStatus to);

    boolean canCloseSprint();

    boolean canComment();
}
