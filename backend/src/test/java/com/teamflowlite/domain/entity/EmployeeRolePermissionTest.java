package com.teamflowlite.domain.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.teamflowlite.domain.enums.Role;
import com.teamflowlite.domain.enums.TaskStatus;
import org.junit.jupiter.api.Test;

class EmployeeRolePermissionTest {

    @Test
    void developerHasLimitedPermissions() {
        Developer developer = new Developer();

        assertAll(
                () -> assertEquals(Role.DEVELOPER, developer.getRole()),
                () -> assertTrue(developer.canCreateTask()),
                () -> assertFalse(developer.canAssignTask()),
                () -> assertTrue(developer.canMoveTaskStatus(TaskStatus.TODO, TaskStatus.IN_PROGRESS)),
                () -> assertFalse(developer.canMoveTaskStatus(TaskStatus.DONE, TaskStatus.BLOCKED)),
                () -> assertFalse(developer.canCloseSprint()),
                () -> assertTrue(developer.canComment())
        );
    }

    @Test
    void managerHasBroadPermissions() {
        Manager manager = new Manager();

        assertAll(
                () -> assertEquals(Role.MANAGER, manager.getRole()),
                () -> assertTrue(manager.canCreateTask()),
                () -> assertTrue(manager.canAssignTask()),
                () -> assertTrue(manager.canMoveTaskStatus(TaskStatus.IN_REVIEW, TaskStatus.DONE)),
                () -> assertTrue(manager.canCloseSprint()),
                () -> assertTrue(manager.canComment())
        );
    }

    @Test
    void teamLeadCanMoveTaskStatusExceptFromDoneToNonDone() {
        TeamLead teamLead = new TeamLead();

        assertAll(
                () -> assertEquals(Role.TEAM_LEAD, teamLead.getRole()),
                () -> assertTrue(teamLead.canCreateTask()),
                () -> assertTrue(teamLead.canAssignTask()),
                () -> assertTrue(teamLead.canMoveTaskStatus(TaskStatus.TODO, TaskStatus.IN_PROGRESS)),
                () -> assertTrue(teamLead.canMoveTaskStatus(TaskStatus.DONE, TaskStatus.DONE)),
                () -> assertFalse(teamLead.canMoveTaskStatus(TaskStatus.DONE, TaskStatus.BLOCKED)),
                () -> assertTrue(teamLead.canCloseSprint()),
                () -> assertTrue(teamLead.canComment())
        );
    }
}
