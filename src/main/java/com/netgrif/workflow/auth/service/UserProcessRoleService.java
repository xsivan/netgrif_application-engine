package com.netgrif.workflow.auth.service;

import com.netgrif.workflow.auth.domain.UserProcessRole;
import com.netgrif.workflow.auth.domain.repositories.UserProcessRoleRepository;
import com.netgrif.workflow.auth.service.interfaces.IUserProcessRoleService;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRole;
import com.netgrif.workflow.petrinet.domain.roles.ProcessRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service
public class UserProcessRoleService implements IUserProcessRoleService {

    @Autowired
    private UserProcessRoleRepository repository;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    private String DEFAULT_ROLE_ID;

    @Override
    public List<UserProcessRole> findAll() {
        return repository.findAll();
    }

    @Override
    public UserProcessRole findDefault() {
        UserProcessRole defaultRole = repository.findByRoleId(getDefaultRoleId());
        if (defaultRole == null) {
            defaultRole = repository.save(new UserProcessRole(getDefaultRoleId()));
        }
        return defaultRole;
    }

    @Override
    public List<UserProcessRole> saveRoles(Collection<ProcessRole> values, String netId) {
        List<UserProcessRole> userProcessRoles = new LinkedList<>();
        for (ProcessRole value : values) {
            UserProcessRole userRole = new UserProcessRole();
            userRole.setRoleId(value.getStringId());
            userRole.setNetId(netId);
            userProcessRoles.add(userRole);
        }
        return repository.save(userProcessRoles);
    }

    @Override
    public UserProcessRole findByRoleId(String roleId) {
        return repository.findByRoleId(roleId);
    }

    private String getDefaultRoleId() {
        if (DEFAULT_ROLE_ID == null) {
            ProcessRole role = processRoleRepository.findByName_DefaultValue(ProcessRole.DEFAULT_ROLE);
            if (role == null)
                throw new NullPointerException("Default process role not found");
            DEFAULT_ROLE_ID = role.getStringId();
        }
        return DEFAULT_ROLE_ID;
    }
}