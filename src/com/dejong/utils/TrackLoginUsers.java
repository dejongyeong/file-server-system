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
        List<Users> updates = users;
        int index = 0;
        for(Users u: users) {
            if(! username.equals(u.getUsername())) {
                index ++;
            }
            break;
        }
        updates.remove(index);  //remove users from list of logged in users
        return updates;
    }
}
