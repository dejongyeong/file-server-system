package com.dejong.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ServerUtilities {

    private static List<Users> users;

    //login
    public static String login(String username, String password) {
        users = SeedUsers.open();
        //check if user is logged in
        if(TrackLoginUsers.isLoggedIn(username)) {
            return "303: " + username + " is logged in"; //server response
        } //end if

        for(Users u: users) {
            if(username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                TrackLoginUsers.trackLoginUsers(new Users(username, password));
                return "301: " + username + " found and logged in."; //server response to client
            }
        } //end for loop
        return "302: Credentials entered incorrect or user not exist."; //server response to client
    } //end login

    //user register
    //references: https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java/3634879
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
                return "501: User created: " + username  + ". Please log in."; //server response to client
            } else {
                return "502: User already exist. Please logged in"; //server response to client
            } //end if
        } //end if
        return "502: User already exist. Please logged in."; //server response to client
    } //end register

    //logout user
    public static String logout(String username) {
        if(TrackLoginUsers.isLoggedIn(username)) {
            TrackLoginUsers.logout(username);
            System.out.println("401: User " + username + " logged out");
            return "401: User logged out successfully."; //server response to client
        } else {
            return "402: User " + username + " not logged in."; //server response to client
        } //end if
    } //end logout

    //upload
    //references: https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
    //references: https://www.baeldung.com/java-read-file
    //references: https://stackoverflow.com/questions/28977308/read-all-lines-with-bufferedreader
    public static String upload(String username, String filename) throws IOException {
        String path = "C:\\DC\\";
        File file = new File(path + filename);
        //check if user is logged in
        if(! TrackLoginUsers.isLoggedIn(username)) {
            return "602 User " + username + " not logged in. Please log in.";
        }
        //check if file exists
        if(! file.exists()) {
            return "603: File C:\\DC\\" + filename + " not found.";
        }
        //upload to user unique directory.
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String data = reader.lines().collect(Collectors.joining());  //read from file.
        String userPath = path + username + "\\" + filename; //user unique directory path.
        //create new files in user unique folder to represent as upload.
        Files.write(Paths.get(userPath), data.getBytes(StandardCharsets.UTF_8));
        return "601: File " + userPath + " uploaded."; //server response to client.
    } //end upload
}
