package com.dejong.utils;

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerUtilities {

    private static List<Users> users;
    private static List<Users> loginUsers = new ArrayList<>();

    //login
    public static String login(String username, String password) {
        users = SeedUsers.open();
        //check if user is logged in
        if(TrackLoginUsers.isLoggedIn(loginUsers, username)) {
            return "303: " + username + " is already logged in"; //server response
        } //end if

        for(Users u: users) {
            if(username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                loginUsers.add(new Users(username, password));
                return "301: " + username + " found and logged in."; //server response to client
            }
        } //end for loop
        return "302: Credentials entered incorrect or user not exist."; //server response to client
    } //end login

    //user register
    //references: https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java/3634879
    public static String register(String username, String password) throws IOException {
        String path = "C:\\DC\\"; //main path to store unique folder
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
        if(! TrackLoginUsers.isLoggedIn(loginUsers, username)) {
            System.out.println("User " + username + " not logged in");
            return "402: User " + username + " not logged in."; //server response to client
        }
        loginUsers = TrackLoginUsers.logout(loginUsers, username);
        System.out.println("User " + username + " logged out");
        return "401: User logged out successfully."; //server response to client
    } //end logout

    //upload
    //references: https://howtodoinjava.com/java/io/how-to-check-if-file-exists-in-java/
    //references: https://www.baeldung.com/java-read-file
    //references: https://howtodoinjava.com/java/io/how-to-read-file-content-into-byte-array-in-java/
    public static String upload(String username, String filename) throws IOException {
        String path = "C:\\DC\\";
        //check if user is logged in
        if(! TrackLoginUsers.isLoggedIn(loginUsers, username)) {
            return "602 User " + username + " not logged in. Please log in.";
        }
        //check if file exists
        if(! new File(path + filename).exists()) {
            return "603: File C:\\DC\\" + filename + " not found.";
        }
        //read from file and upload to user unique directory.
        Path filePath = Paths.get(path + filename);
        byte[] content = Files.readAllBytes(filePath);
        String data = new String(content);
        String userPath = path + username + "\\" + filename; //user unique directory path.
        //create new files in user unique folder to represent as upload.
        Files.write(Paths.get(userPath), data.getBytes(StandardCharsets.UTF_8));
        return "601: File " + userPath + " uploaded."; //server response to client.
    } //end upload

    //download
    public static String download(String username, String filename) throws IOException {
        String path = "C:\\DC\\" + username + "\\";
        File downloadFolder = new File(path + "download");
        //check if user is logged in
        if(! TrackLoginUsers.isLoggedIn(loginUsers, username)) {
            return "702 User " + username + " not logged in. Please log in.";
        }
        //check if file exists in user home directory
        if(! new File(path + filename).exists()) {
            return "703: File " + path + filename + " not found.";
        }
        //check if download folder exists in user home directory
        if(! downloadFolder.exists()) {
            downloadFolder.mkdirs(); //create download directory.
        }
        //read from file in home directory and store into download directory
        Path filePath = Paths.get(path + filename);
        byte[] content = Files.readAllBytes(filePath);
        String data = new String(content);
        String downloadPath = path + "download" + "\\" + filename; //user download folder
        //create new files in download folder to represent as download
        Files.write(Paths.get(downloadPath), data.getBytes(StandardCharsets.UTF_8));
        return "701: File " + path + "\\" + filename + " downloaded to download folder.";
    } //end download

} // end class
