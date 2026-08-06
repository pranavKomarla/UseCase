package com.teamflowlite.domain.entity;

import com.teamflowlite.domain.enums.Role;
import com.teamflowlite.domain.enums.TaskStatus;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DEVELOPER")
public class Developer extends Employee {

    public Developer() {
        setRole(Role.DEVELOPER);
    }

    @Override
    public boolean canCreateTask() {
        return true;
    }

    @Override
    public boolean canAssignTask() {
        return false;
    }

    @Override
    public boolean canMoveTaskStatus(TaskStatus from, TaskStatus to) {
        return from != TaskStatus.DONE;
    }

    @Override
    public boolean canCloseSprint() {
        return false;
    }

    @Override
    public boolean canComment() {
        return true;
    }
}
