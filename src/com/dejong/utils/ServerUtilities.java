package com.dejong.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ServerUtilities {

    private static List<Users> users;

    //login
    public static String login(String username, String password) {
        users = SeedUsers.open();

        //check if user is logged in
        if(TrackLoginUsers.isLoggedIn(username)) {
            return "303: " + username + " is logged in";
        }

        for(Users u: users) {
            if(username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                TrackLoginUsers.trackLoginUsers(new Users(username, password));
                return "301: " + username + " found and logged in."; //server response
            }
        } //end for loop

        return "302: Credentials entered incorrect or user not exist."; //server response
    } //end login

    //user register
    public static String register(String username, String password) throws IOException {
        String path = "C://DC//"; //main path to store unique folder
        File dir = new File(path + username);

        if(!dir.exists()) {
            //create new folder
            if(dir.mkdirs()) {
                //save user into current list
                users = SeedUsers.open();
                users.add(new Users(username, password));

                //output to .dat file
                SeedUsers.save(users);

                System.out.println(dir.toString() + " has been created");
                return "501: User created: " + username  + ". Please log in.";
            } else {
                return "502: User already exist. Please logged in";
            } //end if
        } //end if

        return "502: User already exist. Please logged in.";
    } //end register

    //logout user
    public static String logout(String username) {
        if(TrackLoginUsers.isLoggedIn(username)) {
            TrackLoginUsers.logout(username);
            System.out.println("401: User " + username + " logged out");
            return "401: User logged out successfully.";
        } else {
            return "402: User " + username + " not logged in.";
        } //end if
    } //end logout
}
