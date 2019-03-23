package com.dejong.utils;

import java.util.*;

/**
 * This module keep tracks of a list of logged in users.
 *
 * @author De Jong on 20 March 2019
 *
 */

public class TrackLoginUsers {
    //check if user is logged in
    public static boolean isLoggedIn(List<Users> users, String username) {
        boolean isLogin = false;
        for(Users u: users) {
            if(username.trim().equals(u.getUsername()))
                isLogin = true;
        }
        return isLogin;
    }

    //logout users
    public static List<Users> logout(List<Users> users, String username) {
        Users user = null;
        for(Users u: users) {
            if(username.equals(u.getUsername())) {
                user = new Users(u.getUsername(), u.getPassword());
                break;
            }
        }
        users.remove(user);
        return users;
    }
}
