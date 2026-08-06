package com.teamflowlite.domain.entity;

import com.teamflowlite.domain.enums.Role;
import com.teamflowlite.domain.enums.TaskStatus;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEAM_LEAD")
public class TeamLead extends Employee {

    public TeamLead() {
        setRole(Role.TEAM_LEAD);
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
        return from != TaskStatus.DONE || to == TaskStatus.DONE;
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
