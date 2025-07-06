package org.crystallen.lc.enums;

public enum RoleEnum {
    ADMIN(1L,"admin"),
    EDITOR(2L,"editor"),
    VIEWER(3L,"viewer");

    private final Long type;
    private final String info;

    RoleEnum(Long type, String info) {
        this.type = type;
        this.info = info;
    }

    public Long getType() {
        return type;
    }

    public String getInfo() {
        return info;
    }
}
