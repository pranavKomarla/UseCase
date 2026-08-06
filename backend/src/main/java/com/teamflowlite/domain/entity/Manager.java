package com.teamflowlite.domain.entity;

import com.teamflowlite.domain.enums.Role;
import com.teamflowlite.domain.enums.TaskStatus;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MANAGER")
public class Manager extends Employee {

    public Manager() {
        setRole(Role.MANAGER);
    }

    @Override
    public boolean canCreateTask() {
        return true;
    }

    @Override
    public boolean canAssignTask() {
        return true;
    }

    @Override
    public boolean canMoveTaskStatus(TaskStatus from, TaskStatus to) {
        return true;
    }

    @Override
    public boolean canCloseSprint() {
        return true;
    }

    @Override
    public boolean canComment() {
        return true;
    }
}
