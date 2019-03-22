package com.dejong.client;

import com.dejong.utils.ClientUtilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This module contains presentation logic of a client
 * @author M. L. Liu
 */

public class Client {

    //keystore details
    static String keystoreFile = "public.jks";
    static String keyStorePwd = "ittralee";

    public static void main(String args[]) {
        //ssl communication
        System.setProperty("javax.net.ssl.keyStore", keystoreFile);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePwd);
        System.setProperty("javax.net.debug", "all");

        //variables
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String hostname = "localhost"; // default hostname
        String port = "7"; // default port
        boolean done = false;
        String serverResponse;
        String username;
        String password;

        try {

            System.out.println("Welcome to File Management System!!");
            ClientHelper helper = new ClientHelper(hostname, port);

            //program loop
            while(!done) {
                System.out.println("\n\n---------- Enter option -----------\n" +
                        "1. Login\n" + "2. Register\n" + "5. Logout\n" +
                        "6. Quit/Disconnect");
                String option = br.readLine();
                switch(option) {
                    case "1":
                        System.out.println("Prepare to log in");
                        System.out.println("Enter username: ");
                        username = br.readLine();
                        System.out.println("Enter password: ");
                        password = br.readLine();
                        if(username.trim().length() == 0 || password.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.login(username, password);
                        System.out.println(serverResponse);
                        break;
                    case "2":
                        System.out.println("Prepare to register");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter password");
                        password = br.readLine();
                        if(username.trim().length() == 0 || password.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.register(username, password);
                        System.out.println(serverResponse);
                        break;
                    case "5":
                        System.out.println("Prepare to logout");
                        System.out.println("Enter username");
                        username = br.readLine();
                        if(username.trim().length() == 0) {
                            throw new RuntimeException("Username and Password must not be empty.");
                        }
                        serverResponse = ClientUtilities.logout(username);
                        System.out.println(serverResponse);
                        break;
                    case "6":
                        System.out.println("Quitting");
                        helper.done();  //close socket
                        done = true;  //break loop
                        break;
                    default:
                        System.out.println("Invalid option! Please try again.");
                        break;
                } //end switch
            } //end while
        } catch(Exception ex) {
            ex.printStackTrace();
        } //end catch
    } //end main
} //end class
