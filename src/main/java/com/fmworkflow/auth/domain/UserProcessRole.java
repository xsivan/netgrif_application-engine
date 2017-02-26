package com.fmworkflow.auth.domain;

import javax.persistence.*;
import java.util.Set;

@Entity
public class UserProcessRole {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String roleId;
    @ManyToMany(mappedBy = "userProcessRoles")
    private Set<User> users;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}