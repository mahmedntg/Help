package com.dalal.help.utils;

public class Request {
    private String name, description, status, userId, serviceType, key, userServiceType;
    private double amount;

    public Request() {
    }

    public Request(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Request(String name, double amount, String description, String status, String userId, String serviceType) {
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.serviceType = serviceType;
        this.userServiceType = userId + "__" + serviceType;
    }

    public Request(double amount, String description, String status, String userId, String serviceType) {
        this.amount = amount;
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.serviceType = serviceType;
        this.userServiceType = userId + "__" + serviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getUserServiceType() {
        return userServiceType;
    }

    public void setUserServiceType(String userServiceType) {
        this.userServiceType = userServiceType;
    }
}

