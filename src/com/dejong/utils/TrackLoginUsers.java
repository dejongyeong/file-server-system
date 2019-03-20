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

    public static void trackLoginUsers(Users user) {
        loginUsers.add(user);
    }

    public static List<Users> getLoginUsers() {
        return loginUsers;
    }

}
