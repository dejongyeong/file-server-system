package com.dejong.utils;

import java.util.*;

/**
 * This module keep tracks of a list of logged in users.
 *
 * @author De Jong on 20 March 2019
 *
 */

public class TrackLoginUsers {

    static List<Users> loginUsers = new ArrayList<>();

    //track logged in users
    public static void trackLoginUsers(Users user) {
        loginUsers.add(user);
    }

    //logout users
    public static void logout(String username) {
        Users user = null;
        for(Users u: loginUsers) {
            if(username.equals(u.getUsername())) {
                user = new Users(u.getUsername(), u.getPassword());
                break;
            }
        }
        loginUsers.remove(user);
    }

    //check if user is logged in
    public static boolean isLoggedIn(String username) {
        boolean isLogin = false;
        for(Users u: loginUsers) {
            if(username.trim().equals(u.getUsername()))
                isLogin = true;
        }
        return isLogin;
    }
}
