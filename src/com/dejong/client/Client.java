package com.dejong.client;

import com.dejong.utils.ClientUtilities;
import com.dejong.utils.TrackLoginUsers;

import java.io.BufferedReader;
import java.io.IOException;
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
        String filename;
        String filetype;
        String file;

        try {

            System.out.println("Welcome to File Management System!!");
            ClientHelper helper = new ClientHelper(hostname, port);

            //program loop
            while(!done) {
                System.out.println("\n\n---------- Enter option -----------\n" +
                        "1. Login\n" + "2. Register\n" + "3. Upload\n" + "4. Download\n" +
                        "5. Logout\n" + "6. Quit/Disconnect");
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
                    case "3":
                        System.out.println("Prepare to upload - Ensure file is in C:\\DC\\");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter filename");
                        filename = br.readLine();
                        filetype = filetype(); //prompt user for file format
                        if(username.trim().length() == 0 || filename.trim().length() == 0 || filetype.trim().length() == 0) {
                            throw new RuntimeException("Username, filename and file format must not be empty.");
                        }
                        file = filename + filetype;
                        System.out.println("File to upload: " + file);
                        serverResponse = ClientUtilities.upload(username, file);
                        System.out.println(serverResponse);
                        break;
                    case "4":
                        System.out.println("Prepare to download - Ensure file is in C:\\DC\\username");
                        System.out.println("Enter username");
                        username = br.readLine();
                        System.out.println("Enter filename");
                        filename = br.readLine();
                        filetype = filetype();
                        if(username.trim().length() == 0 || filename.trim().length() == 0 || filetype.trim().length() == 0) {
                            throw new RuntimeException("Username, filename and file format must not be empty.");
                        }
                        file = filename + filetype;
                        System.out.println("File to download: " + file);
                        serverResponse = ClientUtilities.download(username, file);
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

    //prompt user for file type
    private static String filetype() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String filetype = "";
        System.out.println("File Format Option:\n1. .txt\n2. .docx");
        String option = br.readLine();
        switch(option) {
            case "1":
                filetype = ".txt";
                break;
            case "2":
                filetype = ".docx";
                break;
            default:
                System.out.println("Invalid option! Please try again.");
                break;
        } //end switch
        return filetype;
    }
} //end class
