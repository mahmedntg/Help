package com.dalal.help.utils;

/**
 * Created by mhamedsayed on 3/8/2019.
 */

public enum RequestStatus {
    PENDING("Pending"), REJECTED("Rejected"),ACCEPTED("Accepted"),COMPLETED("Completed");
    private String value;

    RequestStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
