package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;

import java.util.List;
import java.util.Set;

public interface IProcessRoleService {
    boolean assignRolesToUser(Long userId, Set<String> roleIds);

    List<ProcessRole> findAll(String netId);
}