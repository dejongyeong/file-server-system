package com.dejong.utils;

import java.util.*;

/**
 * This module keep tracks of a list of logged in users.
 *
 * @author De Jong on 20 March 2019
 *
 */

public class TrackLoginUsers {

    private static List<Users> loginUsers = new ArrayList<>();

    //track logged in users
    public static void trackLoginUsers(Users user) {
        loginUsers.add(user);
    }

    //retrieve a list of logged in users
    public static List<Users> getLoginUsers() {
        return loginUsers;
    }

    //logout users
    public static void logout(String username) {
        loginUsers.remove(username);
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
