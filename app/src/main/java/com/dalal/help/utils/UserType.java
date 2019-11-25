package com.dalal.help.utils;

public enum UserType {
    DONATOR("Donator"), DONOR("Donor"), ADMIN("Admin");
    private String value;

    UserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
