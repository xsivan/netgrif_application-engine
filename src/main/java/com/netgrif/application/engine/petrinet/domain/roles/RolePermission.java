package com.netgrif.application.engine.petrinet.domain.roles;

public enum RolePermission {
    DELEGATE("delegate"),
    CANCEL("cancel"),
    ASSIGN("assign"),
    FINISH("finish"),
    SET("set"),
    VIEW("view"),
    OPEN_TASK_EVENT("open_task_event"),
    CLOSE_TASK_EVENT("close_task_event");

    private String value;

    RolePermission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    public boolean equal(String str) {
        return str != null && this.value.equalsIgnoreCase(str);
    }
}
