package com.dejong.utils;

import java.util.List;

public class ServerUtilities {

    //login
    public static String login(String username, String password) {
        List<Users> users = SeedUsers.open();

        //check if user is logged in
        if(checkIsLoggedIn(username)) {
            return "303: " + username + " is logged in";
        }

        for(Users u: users) {
            if(username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                TrackLoginUsers.trackLoginUsers(new Users(username, password));
                return "301: " + username + " found and logged in."; //server response
            }
        }

        return "302: Credentials entered incorrect or user not exist."; //server response
    } //end login

    //check user is logged in
    public static boolean checkIsLoggedIn(String username) {
        List<Users> loggedInUsers = TrackLoginUsers.getLoginUsers();
        boolean loggedIn = false;
        for(Users u: loggedInUsers) {
            if(username.equals(u.getUsername())) {
                loggedIn = true;
            }
        }
        return loggedIn;
    }
}
