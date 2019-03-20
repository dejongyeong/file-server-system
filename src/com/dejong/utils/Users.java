package com.dejong.utils;

import java.io.Serializable;

/**
 * Blueprint from which User are created, contains username and password.
 * Initialize Users object using constructor.
 *
 * @author De Jong on 20 March 2019
 *
 */

public class Users implements Serializable {

    private String username;
    private String password;

    public Users(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
