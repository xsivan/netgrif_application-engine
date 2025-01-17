package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.AnonymousUser;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.roles.RolePermission;
import com.netgrif.application.engine.petrinet.domain.throwable.IllegalTaskStateException;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskAuthorizationService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;

@Service
public class TaskAuthorizationService extends AbstractAuthorizationService implements ITaskAuthorizationService {

    @Autowired
    ITaskService taskService;

    @Override
    public Boolean userHasAtLeastOneRolePermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasAtLeastOneRolePermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasAtLeastOneRolePermission(IUser user, Task task, RolePermission... permissions) {
        if (task.getRoles() == null || task.getRoles().isEmpty())
            return null;

        Map<String, Boolean> aggregatePermissions = getAggregatePermissions(user, task.getRoles());

        for (RolePermission permission : permissions) {
            if (hasRestrictedPermission(aggregatePermissions.get(permission.toString()))) {
                return false;
            }
        }

        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(aggregatePermissions.get(permission.toString())));
    }

    @Override
    public Boolean userHasUserListPermission(LoggedUser loggedUser, String taskId, RolePermission... permissions) {
        return userHasUserListPermission(loggedUser.transformToUser(), taskService.findById(taskId), permissions);
    }

    @Override
    public Boolean userHasUserListPermission(IUser user, Task task, RolePermission... permissions) {
        if (task.getUserRefs() == null || task.getUserRefs().isEmpty())
            return null;

        if (!task.getUsers().containsKey(user.getStringId()))
            return null;

        Map<String, Boolean> userPermissions = task.getUsers().get(user.getStringId());

        for (RolePermission permission : permissions) {
            Boolean perm = userPermissions.get(permission.toString());
            if (hasRestrictedPermission(perm)) {
                return false;
            }
        }
        return Arrays.stream(permissions).anyMatch(permission -> hasPermission(userPermissions.get(permission.toString())));
    }

    @Override
    public boolean isAssignee(LoggedUser loggedUser, String taskId) {
        if (loggedUser.isAnonymous())
            return isAssignee(loggedUser.transformToAnonymousUser(), taskService.findById(taskId));
        else
            return isAssignee(loggedUser.transformToUser(), taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(IUser user, String taskId) {
        return isAssignee(user, taskService.findById(taskId));
    }

    @Override
    public boolean isAssignee(IUser user, Task task) {
        if (!isAssigned(task))
            return false;
        else
            return task.getUserId().equals(user.getStringId()) || user instanceof AnonymousUser;
    }

    private boolean isAssigned(String taskId) {
        return isAssigned(taskService.findById(taskId));
    }

    private boolean isAssigned(Task task) {
        return task.getUserId() != null;
    }

    @Override
    public boolean canCallAssign(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.ASSIGN);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.ASSIGN);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    /**
     *  Method will check if user have rights to perform 'opentask' event.
     *
     * @param loggedUser, represent actualy logged user or system to by verified
     * @param taskId, string identifikator o task on witch we want perform 'opentak' event
     *
     * @return 'true' if user have right or 'false'
     * */
    @Override
    public boolean canCallOpenTaskEvent(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.OPEN_TASK_EVENT);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.OPEN_TASK_EVENT);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    /**
     *  Method will check if user have rights to perform 'closetask' event.
     *
     * @param loggedUser, represent actualy logged user or system to by verified
     * @param taskId, string identifikator o task on witch we want perform 'closetask' event
     *
     * @return 'true' if user have right or 'false'
     * */
    @Override
    public boolean canCallCloseTaskEvent(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.CLOSE_TASK_EVENT);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.CLOSE_TASK_EVENT);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallDelegate(LoggedUser loggedUser, String taskId) {
        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.DELEGATE);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.DELEGATE);
        return loggedUser.isAdmin() || (userPerm == null ? (rolePerm != null && rolePerm) : userPerm);
    }

    @Override
    public boolean canCallFinish(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be finished, because it is not assigned!");

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.FINISH);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.FINISH);
        return loggedUser.isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId));
    }

    private boolean canAssignedCancel(IUser user, String taskId) {
        Task task = taskService.findById(taskId);
        if (!isAssigned(task) || !task.getUserId().equals(user.getStringId())) {
            return true;
        }
        return (task.getAssignedUserPolicy() == null || task.getAssignedUserPolicy().get("cancel") == null) || task.getAssignedUserPolicy().get("cancel");
    }

    @Override
    public boolean canCallCancel(LoggedUser loggedUser, String taskId) throws IllegalTaskStateException {
        if (!isAssigned(taskId))
            throw new IllegalTaskStateException("Task with ID '" + taskId + "' cannot be canceled, because it is not assigned!");

        Boolean rolePerm = userHasAtLeastOneRolePermission(loggedUser, taskId, RolePermission.CANCEL);
        Boolean userPerm = userHasUserListPermission(loggedUser, taskId, RolePermission.CANCEL);
        return loggedUser.isAdmin() || ((userPerm == null ? (rolePerm != null && rolePerm) : userPerm) && isAssignee(loggedUser, taskId)) && canAssignedCancel(loggedUser.transformToUser(), taskId);
    }

    @Override
    public boolean canCallSaveData(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }

    @Override
    public boolean canCallSaveFile(LoggedUser loggedUser, String taskId) {
        return loggedUser.isAdmin() || isAssignee(loggedUser, taskId);
    }
}
