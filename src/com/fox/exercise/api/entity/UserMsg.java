package com.fox.exercise.api.entity;

public class UserMsg {
    private String account;
    private String passwd;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    @Override
    public String toString() {
        return "UserMsg [account=" + account + ", passwd=" + passwd + "]";
    }

}
